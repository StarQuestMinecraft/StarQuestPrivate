package net.countercraft.movecraft.async.detection;

import java.util.ArrayList;
import java.util.HashMap;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.database.StarshipData;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RedetectTask extends DetectionTask{
	
	public RedetectTask(Craft c, MovecraftLocation startLocation, int minSize, int maxSize, Integer[] allowedBlocks, Integer[] forbiddenBlocks, String player, World w) {
		super(c, startLocation, minSize, maxSize, allowedBlocks, forbiddenBlocks, player, w);
	}
	
	
	@Override
	public void excecute() {
		
		StarshipData d = Movecraft.getInstance().getStarshipDatabase().getStarshipByLocation(new Location(data.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()));
		if(d == null){
			fail("This ship is not saved in the database, perhaps it is a new ship? Try left-clicking the sign with a ship controller to detect the ship.");
			return;
		}
		SaveableBlock[] savedList = d.getBlockList();
		
		World w = Bukkit.getWorld(savedList[0].getWorld());
		MovecraftLocation[] blockList = new MovecraftLocation[savedList.length];
		
		for(int i = 0; i < savedList.length; i++){
			SaveableBlock b = savedList[i];
			int id = w.getBlockTypeIdAt(b.getX(), b.getY(), b.getZ());
			if(!b.matchesTypeData(w)){
				if(isButtonCase(id, b.type)){
					fail("This ship has been modified since it was last piloted; Block at " + b.getX() + ", " + b.getY() + ", " + b.getZ()
							+ " has id " + Material.getMaterial(id).toString().toLowerCase() + ", expected ID " + Material.getMaterial(b.type).toString().toLowerCase()
							+ ". If this is intentional, Redetect your ship by left-clicking this sign with a ship controller."
							+ ChatColor.RED + " It looks like you may have left a docking tube switched and that's why you can't redetect.");
				} else {
				fail("This ship has been modified since it was last piloted; Block at " + b.getX() + ", " + b.getY() + ", " + b.getZ()
						+ " has id " + Material.getMaterial(id).toString().toLowerCase() + ", expected ID " + Material.getMaterial(b.type).toString().toLowerCase() + ". If this is intentional, Redetect your ship by left-clicking this sign with a ship controller.");
				}
				return;
			}
			MovecraftLocation loc = b.toMovecraftLocation();
			blockList[i] = loc;
			if (b.type == 63 || b.type == 68) {
				signLocations.add(blockList[i]);
			}
			addToBlockCount(b.type);
			calculateBounds(loc);
		}


		data.setBlockList(blockList);
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
			}catch(Exception e){
				e.printStackTrace();
			}
			c.bedspawnsLock.release();
		}
	}


	private boolean isButtonCase(int id, int type) {
		if(id == 77 || id == 20){
			if(type == 77 || type == 20){
				return true;
			}
		}
		return false;
	}
}
