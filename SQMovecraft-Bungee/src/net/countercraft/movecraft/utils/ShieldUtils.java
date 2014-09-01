package net.countercraft.movecraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.craft.Craft;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShieldUtils {
	
	private static WorldGuardPlugin wg = getWorldGuard();
	
	public static void deployShield(Craft ship){
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for(MovecraftLocation l : ship.getBlockList()){
			if(l.getY() < minY) minY = l.getY();
			if(l.getY() > maxY) maxY = l.getY();
			if(l.getX() > maxX) maxX = l.getX();
			if(l.getZ() > maxZ) minY = l.getY();
		}
		BlockVector min = new BlockVector(ship.getMinX(), minY, ship.getMinZ());
		BlockVector max = new BlockVector(maxX, maxY, maxZ);
		ProtectedRegion reg = new ProtectedCuboidRegion(createRName(ship), min, max);
		wg.getRegionManager(ship.getW()).addRegion(reg);
		
	}
	
	public static void removeShield(Craft ship){
		String rname = createRName(ship);
		ProtectedRegion r = wg.getRegionManager(ship.getW()).getRegionExact(createRName(ship));
		if(r != null){
			wg.getRegionManager(ship.getW()).removeRegion(rname);
		}
	}
	
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	private static String createRName(Craft ship){
		return (ship.pilot.getName() + "-shielded-ship");
	}
}
