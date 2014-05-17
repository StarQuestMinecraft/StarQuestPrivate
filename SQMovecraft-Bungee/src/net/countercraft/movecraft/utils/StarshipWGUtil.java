package net.countercraft.movecraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

public class StarshipWGUtil {
	
	static WorldGuardPlugin wg;
	
	static{
		 wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	}
	public static void createShipWG(Craft c){
		BlockVector[] regInfo = getHitBoxMinMax(c);
		final String pilotName = c.pilot.getName();
		final ProtectedCuboidRegion r = new ProtectedCuboidRegion(pilotName + "_starship", regInfo[0], regInfo[1]);
		final RegionManager mgr = wg.getRegionManager(c.getW());
		ApplicableRegionSet regions = mgr.getApplicableRegions(r);
		if(regions.size() > 0){
			c.pilot.sendMessage("WARNING: shield cannot activate, there is another shield overlapping yours!");
		} else {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				public void run(){
					mgr.addRegion(r);
					try {
						mgr.save();
					} catch (ProtectionDatabaseException e) {
						e.printStackTrace();
					}
					Player plr = Bukkit.getServer().getPlayer(pilotName);
					if(plr != null){
						plr.sendMessage("Your starship shield is now activated.");
					}
				}
			}, 6000L );
			
			c.pilot.sendMessage("Your ship is now charging its shield. It will take 5 minute to come online.");
			c.pilot.sendMessage("Your ship will be unprotected during this time!");
		}
	}
	
	public static void removeShipWG(Craft c){
		final RegionManager mgr = wg.getRegionManager(c.getW());
		if(mgr.hasRegion(c.pilot.getName() + "_starship")){
			mgr.removeRegion(c.pilot.getName() + "_starship");
			try {
				mgr.save();
			} catch (ProtectionDatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c.pilot.sendMessage("Your starship shield has vanished, ships cannot be shielded while moving.");
		}
	}
	
	private static BlockVector[] getHitBoxMinMax(Craft c){
		
		int[][][] box = c.getHitBox();
		
		int minX = c.getMinX();
		int minY = Integer.MAX_VALUE;
		int minZ = c.getMinZ();
		
		int maxX = minX + box.length;
		int maxY = 0;
		int maxZ = Integer.MIN_VALUE;
			
		for(int x = 0; x < box.length; x++){
			if(box[x].length + minZ > maxZ ){
				maxX = box[x].length;
			}
			for(int z = 0; z < box[x].length; z++){
				//hitbox min at point
				if(box[x][z][0] < minY){
					minY = box[x][z][0];
				}
				
				if(box[x][z][1] > maxY){
					maxY = box[x][z][1];
				}
			}
		}
		
		return new BlockVector[]{new BlockVector(minX, minY, minZ), new BlockVector(maxX, maxY, maxZ)};
	}
}
