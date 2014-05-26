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

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.Torpedo;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.modules.ModuleHandler;
import net.countercraft.movecraft.modules.ModuleType;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;
import net.countercraft.movecraft.utils.SignUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class DetectionTask extends AsyncTask {
	private final MovecraftLocation startLocation;
	private final Integer minSize;
	private final Integer maxSize;
	private Integer maxX;
	private Integer maxY;
	private Integer maxZ;
	private Integer minY;
	private final Stack<MovecraftLocation> blockStack = new Stack<MovecraftLocation>();
	private final HashSet<MovecraftLocation> blockList = new HashSet<MovecraftLocation>();
	private final HashSet<MovecraftLocation> visited = new HashSet<MovecraftLocation>();
	private final HashMap<Integer, Integer> blockTypeCount = new HashMap<Integer, Integer>();
	private final ArrayList<MovecraftLocation> signLocations = new ArrayList<MovecraftLocation>();
	private final ArrayList<MovecraftLocation> spongeLocations = new ArrayList<MovecraftLocation>();
	private final DetectionTaskData data;

	public DetectionTask(Craft c, MovecraftLocation startLocation, int minSize, int maxSize, Integer[] allowedBlocks, Integer[] forbiddenBlocks, String player, World w) {
		super(c);
		this.startLocation = startLocation;
		this.minSize = minSize;
		this.maxSize = maxSize;
		data = new DetectionTaskData(w, player, allowedBlocks, forbiddenBlocks);
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

			data.setBlockList(finaliseBlockList(blockList));
			data.setSignLocations(signLocations);
			data.setSpongeLocations(spongeLocations);
			@SuppressWarnings("unchecked")
			HashMap<Integer, ArrayList<Double>> flyBlocks = (HashMap<Integer, ArrayList<Double>>) getCraft().getType().getFlyBlocks().clone();

			if (confirmStructureRequirements(flyBlocks, blockTypeCount)) {

				data.setHitBox(BoundingBoxUtils.formBoundingBox(data.getBlockList(), data.getMinX(), maxX, data.getMinZ(), maxZ));

				Craft c = getCraft();
				// detect bedspawns, may be an expensive operation?
				for (Bedspawn b : Bedspawn.loadBedspawnList(new MovecraftLocation(c.getMinX(), 0, c.getMinZ()), c.getW().getName())) {
					MovecraftLocation loc = new MovecraftLocation(b.x, b.y, b.z);
					if (MathUtils.playerIsWithinBoundingPolygon(data.getHitBox(), data.getMinX(), data.getMinZ(), loc)) {
						c.playersWithBedSpawnsOnShip.add(b.player);
					}
				}

				// add any players to the ship that should be on it
				for (Player plr : data.getWorld().getPlayers()) {
					if (MathUtils.playerIsWithinBoundingPolygon(data.getHitBox(), data.getMinX(), data.getMinZ(), MathUtils.bukkit2MovecraftLoc(plr.getLocation()))) {
						c.playersRiding.add(plr);
						plr.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
					}
				}
			}
		}
		// detect ship signs
		boolean foundMainSign = false;
		for (MovecraftLocation l : data.getSignLocations()) {
			Location loc = new Location(getCraft().getW(), l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) loc.getBlock().getState();
			String line1 = s.getLine(0);
			// check for [private] signs
			if (line1.equals("[private]")) {
				if (!Movecraft.signContainsPlayername(s, data.getPlayername())) {
					data.setFailed(true);
					Movecraft.getInstance().getServer().getPlayer(data.getPlayername()).sendMessage("You are attatched to a door that is locked to someone besides you.");
				}
			}
			if(line1.equals("\\  ||  /")){
				getCraft().facingDirection = SignUtils.getFacingBlockFace(s);
				continue;
			}
			// check each sign to see if it's a craft sign
			boolean isCraftType = (InteractListener.getCraftTypeFromString(line1) != null);
			if (isCraftType) {
				// special rules for carriers!
				if (getCraft().getType().equals(InteractListener.getCraftTypeFromString("Carrier")) || getCraft().getType().equals(InteractListener.getCraftTypeFromString("Flagship"))) {
					if (!Movecraft.signContainsPlayername(s, data.getPlayername())) {
						data.setFailed(true);
						Movecraft.getInstance().getServer().getPlayer(data.getPlayername()).sendMessage("Your ship seems to be attatched to another ship that isn't yours.");
					}

					// other ships
				} else {

					// don't count the main sign as a different ship.
					if (!foundMainSign) {
						if (getCraft().getType().equals(InteractListener.getCraftTypeFromString(line1))) {
							if (Movecraft.signContainsPlayername(s, data.getPlayername())) {
								foundMainSign = true;
							}
						}
					} else {
						data.setFailed(true);
						Movecraft.getInstance().getServer().getPlayer(data.getPlayername()).sendMessage("Your ship seems to have more than one ship sign, or is attatched to another ship.");
						break;
					}
				}
			}
		}
		
		//detect mechanisms
		int numcannons = 0;
		
		BlockFace right = SignUtils.rotate(getCraft().facingDirection, Rotation.CLOCKWISE);
		BlockFace left = SignUtils.rotate(getCraft().facingDirection, Rotation.ANTICLOCKWISE);
		BlockFace back = SignUtils.rotate(left, Rotation.ANTICLOCKWISE);
		
		for(MovecraftLocation l : spongeLocations){
			
			Block b = getCraft().getW().getBlockAt(l.getX(), l.getY(), l.getZ());
			
			ModuleType t = ModuleHandler.detectModule(b, getCraft().facingDirection, left, right, back);
			if(t == ModuleType.CANNON) numcannons++;
			if(t == ModuleType.ENGINE_SMALL) getCraft().getType().addEngineSmall();
			if(t == ModuleType.ENGINE_MEDIUM) getCraft().getType().addEngineMedium();
			if(t == ModuleType.ENGINE_LARGE) getCraft().getType().addEngineLarge();
		}
		if(numcannons > getCraft().getType().getAllowedCannons()){
			data.setFailed(true);
			Movecraft.getInstance().getServer().getPlayer(data.getPlayername()).sendMessage("Your ship has too many cannons!");
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

					if (testID != 34)
						addToDetectionStack(workingLocation);

					calculateBounds(workingLocation);

					if (testID == 63 || testID == 68) {
						signLocations.add(workingLocation);
					}
					
					if(testID == 19){
						spongeLocations.add(workingLocation);
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

	private void addToBlockCount(int id) {
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

	private void calculateBounds(MovecraftLocation l) {
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

	private boolean isWithinLimit(int size, int min, int max) {
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

	private MovecraftLocation[] finaliseBlockList(HashSet<MovecraftLocation> blockSet) {
		return blockSet.toArray(new MovecraftLocation[1]);
	}

	private boolean confirmStructureRequirements(HashMap<Integer, ArrayList<Double>> flyBlocks, HashMap<Integer, Integer> countData) {
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

	private void fail(String message) {
		data.setFailed(true);
		data.setFailMessage(message);
	}
}
