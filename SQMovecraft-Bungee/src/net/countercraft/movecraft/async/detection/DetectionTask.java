/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.async.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DetectionTask extends AsyncTask {
	protected final MovecraftLocation startLocation;
	protected final Integer minSize;
	protected final Integer maxSize;
	protected Integer maxX;
	protected Integer maxY;
	protected Integer maxZ;
	protected Integer minY;
	protected final Stack<MovecraftLocation> blockStack = new Stack<MovecraftLocation>();
	protected final HashSet<MovecraftLocation> blockList = new HashSet<MovecraftLocation>();
	protected final HashSet<MovecraftLocation> visited = new HashSet<MovecraftLocation>();
	protected final HashMap<Integer, Integer> blockTypeCount = new HashMap<Integer, Integer>();
	protected final ArrayList<MovecraftLocation> signLocations = new ArrayList<MovecraftLocation>();
	protected final DetectionTaskData data;

	public DetectionTask(Craft c, MovecraftLocation startLocation, int minSize, int maxSize, Integer[] allowedBlocks, Integer[] forbiddenBlocks, String player, World w) {
		super(c);
		this.startLocation = startLocation;
		this.minSize = minSize;
		this.maxSize = maxSize;
		data = new DetectionTaskData(w, player, allowedBlocks, forbiddenBlocks, startLocation);
	}

	@Override
	public void excecute() {

		blockStack.push(startLocation);

		do {
			detectSurrounding(blockStack.pop());
		} while (!blockStack.isEmpty());

		if (data.failed()) {
			return;
		}

		if (isWithinLimit(blockList.size(), minSize, maxSize)) {

			data.setBlockList(finaliseBlockList(blockList, minY, maxY));
			data.setSignLocations(signLocations);
			@SuppressWarnings("unchecked")
			HashMap<Integer, ArrayList<Double>> flyBlocks = (HashMap<Integer, ArrayList<Double>>) getCraft().getType().getFlyBlocks().clone();

			if (confirmStructureRequirements(flyBlocks, blockTypeCount)) {

				data.setHitBox(BoundingBoxUtils.formBoundingBox(data.getBlockList(), data.getMinX(), maxX, data.getMinZ(), maxZ));

				Craft c = getCraft();
				// detect bedspawns, may be an expensive operation?
				try{
				c.bedspawnsLock.acquire();
				for (Bedspawn b : Bedspawn.loadBedspawnList(new MovecraftLocation(c.getMinX(), 0, c.getMinZ()), c.getW().getName())) {
					MovecraftLocation loc = new MovecraftLocation(b.x, b.y, b.z);
					if (MathUtils.playerIsWithinBoundingPolygon(data.getHitBox(), data.getMinX(), data.getMinZ(), loc)) {
						c.playersWithBedspawnsOnShip.add(b.player);
					}
				}
				c.bedspawnsLock.release();
				
				
				// add any players to the ship that should be on it
				c.playersRidingLock.acquire();
				for (Player plr : data.getWorld().getPlayers()) {
					if (MathUtils.playerIsWithinBoundingPolygon(data.getHitBox(), data.getMinX(), data.getMinZ(), MathUtils.bukkit2MovecraftLoc(plr.getLocation()))) {
						if (!c.playersRidingShip.contains(plr.getUniqueId())) {
							c.playersRidingShip.add(plr.getUniqueId());
							plr.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
						}
					}
				}
				c.playersRidingLock.release();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void detectBlock(int x, int y, int z) {

		MovecraftLocation workingLocation = new MovecraftLocation(x, y, z);

		if (notVisited(workingLocation, visited)) {

			int testID = data.getWorld().getBlockTypeIdAt(x, y, z);

			if (isForbiddenBlock(testID)) {

				fail(String.format(I18nSupport.getInternationalisedString("Detection - Forbidden block found")));

			} else if (isAllowedBlock(testID)) {

				addToBlockList(workingLocation);
				addToBlockCount(testID);

				if (isWithinLimit(blockList.size(), 0, maxSize)) {
					//piston head block
					if (testID != 34)
						addToDetectionStack(workingLocation);
					//piston base block
					if (testID == 33) {
						int dval = data.getWorld().getBlockAt(x, y, z).getData();
						if (dval == 0) {
							// this is a landing gear block
							Block blk = data.getWorld().getBlockAt(x, y, z).getRelative(BlockFace.DOWN);
							if (blk.getType() == Material.AIR) {
								// the landing gear is retracted
								MovecraftLocation pistonAirBlock = MathUtils.bukkit2MovecraftLoc(blk.getLocation());
								addToBlockList(pistonAirBlock);
								// make it landing gear even if it's retracted
								addToBlockCount(34);
								calculateBounds(pistonAirBlock);
							}
						}
					}

					calculateBounds(workingLocation);
					//sign blocks
					if (testID == 63 || testID == 68) {
						signLocations.add(workingLocation);
					}
				}
			}
		}
	}

	private boolean isAllowedBlock(int test) {

		for (int i : data.getAllowedBlocks()) {
			if (i == test) {
				return true;
			}
		}

		return false;
	}

	private boolean isForbiddenBlock(int test) {

		for (int i : data.getForbiddenBlocks()) {
			if (i == test) {
				return true;
			}
		}

		return false;
	}

	public DetectionTaskData getData() {
		return data;
	}

	private boolean notVisited(MovecraftLocation l, HashSet<MovecraftLocation> locations) {
		if (locations.contains(l)) {
			return false;
		} else {
			locations.add(l);
			return true;
		}
	}

	private void addToBlockList(MovecraftLocation l) {
		blockList.add(l);
	}

	private void addToDetectionStack(MovecraftLocation l) {
		blockStack.push(l);
	}

	protected void addToBlockCount(int id) {
		Integer count = blockTypeCount.get(id);

		if (count == null) {
			count = 0;
		}

		blockTypeCount.put(id, count + 1);
	}

	private void detectSurrounding(MovecraftLocation l) {
		int x = l.getX();
		int y = l.getY();
		int z = l.getZ();

		for (int xMod = -1; xMod < 2; xMod += 2) {

			for (int yMod = -1; yMod < 2; yMod++) {

				detectBlock(x + xMod, y + yMod, z);

			}

		}

		for (int zMod = -1; zMod < 2; zMod += 2) {

			for (int yMod = -1; yMod < 2; yMod++) {

				detectBlock(x, y + yMod, z + zMod);

			}

		}

		for (int yMod = -1; yMod < 2; yMod += 2) {

			detectBlock(x, y + yMod, z);

		}

	}

	protected void calculateBounds(MovecraftLocation l) {
		if (maxX == null || l.getX() > maxX) {
			maxX = l.getX();
		}
		if (maxY == null || l.getY() > maxY) {
			maxY = l.getY();
		}
		if (maxZ == null || l.getZ() > maxZ) {
			maxZ = l.getZ();
		}
		if (data.getMinX() == null || l.getX() < data.getMinX()) {
			data.setMinX(l.getX());
		}
		if (minY == null || l.getY() < minY) {
			minY = l.getY();
		}
		if (data.getMinZ() == null || l.getZ() < data.getMinZ()) {
			data.setMinZ(l.getZ());
		}
	}

	protected boolean isWithinLimit(int size, int min, int max) {
		if (size < min) {
			fail(String.format(I18nSupport.getInternationalisedString("Detection - Craft too small"), min));
			return false;
		} else if (size > max) {
			fail(String.format(I18nSupport.getInternationalisedString("Detection - Craft too large"), max));
			return false;
		} else {
			return true;
		}

	}

	private MovecraftLocation[] finaliseBlockList( HashSet<MovecraftLocation> blockSet, int minY, int maxY) {
		
		/*ArrayList<MovecraftLocation> finalList = new ArrayList<MovecraftLocation>();
		for(int posY = this.minY; posY <= this.maxY; posY++){
			for(MovecraftLocation loc : blockSet){
				if(loc.getY() == posY){
					finalList.add(loc);
				}
			}
		}
		MovecraftLocation[] retval = finalList.toArray(new MovecraftLocation[finalList.size()]);
		return retval;*/
		
		return blockSet.toArray(new MovecraftLocation[blockSet.size()]);

  	}

	protected boolean confirmStructureRequirements(HashMap<Integer, ArrayList<Double>> flyBlocks, HashMap<Integer, Integer> countData) {
		for (Integer i : flyBlocks.keySet()) {
			Integer numberOfBlocks = countData.get(i);

			if (numberOfBlocks == null) {
				numberOfBlocks = 0;
			}

			float blockPercentage = (((float) numberOfBlocks / data.getBlockList().length) * 100);
			Double minPercentage = flyBlocks.get(i).get(0);
			Double maxPercentage = flyBlocks.get(i).get(1);

			if (blockPercentage < minPercentage) {

				fail(String.format(I18nSupport.getInternationalisedString("Detection - Failed - Not enough flyblock"), i, minPercentage, blockPercentage));
				return false;

			} else if (blockPercentage > maxPercentage) {

				fail(String.format(I18nSupport.getInternationalisedString("Detection - Failed - Too much flyblock"), i, maxPercentage, blockPercentage));
				return false;

			}
		}

		return true;
	}

	protected void fail(String message) {
		data.setFailed(true);
		data.setFailMessage(message);
	}
}
