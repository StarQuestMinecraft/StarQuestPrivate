package net.countercraft.movecraft.redline;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;

public class RedlineUtils {
	
	private static DynmapAPI api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("Dynmap");
	MarkerAPI mapi = api.getMarkerAPI();
	
	public static void initializeJumpWithShift(Craft c, int x, int y, int z){
		double dist = Math.sqrt(x * x + y * y + z * z);
		long secondsWarmup = (int) (c.getType().getSpeed(c.pilot) * dist);
		c.pilot.sendMessage("Initializing slipdrive warmup. For distance " + (int) dist + ", warmup will take " + secondsWarmup + " seconds for this vessel.");
		c.pilot.sendMessage("Your ship will not be able to move during this time.");
		c.setProcessingTeleport(true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				
			}
		}, secondsWarmup);
	}
	public static void executeJump(Craft c, RedlineJump r){
		c.translate(r.dx, r.dy, r.dz);
	}
	
	public static void displayJumpToMap(Player p, Location from, Location to){
		command("dmarker addcorner " + from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + " " + p.getWorld().getName());
		command("dmarker addcorner " + to.getBlockX() + " " + to.getBlockY() + " " + to.getBlockZ() + " " + p.getWorld().getName());
		command("dmarker addline id:" + createWarpLineId(p) + " " + createWarpLineName(p));
	}
	
	public static void removeJumpFromMap(Player p){
		command("dmarker deleteline id:" + createWarpLineId(p));
	}
	
	private static void command(String s){
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
	}
	
	private static String createWarpLineId(Player p){
		return p.getName() + "-slipjump";
	}
	
	private static String createWarpLineName(Player p){
		return p.getName() + "'s Slipdrive Trajectory";
	}
}
