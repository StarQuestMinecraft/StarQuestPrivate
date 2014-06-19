package net.countercraft.movecraft.utils.mechanism;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BorderUtils {
	
	public static int WORLD_RADIUS = 2000;
	public static int PADDING = 20;
	private static JavaPlugin worldBorderPlugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("WorldBorder");
	
	static{
		World w = Bukkit.getServer().getWorld(Bukkit.getServerName());
		if(w == null){
			System.out.println("MOVECRAFT ERROR: No world found with name == server name!");
		} else {
			WORLD_RADIUS = getRadius(w.getName());
		}
	}
	public static boolean isWithinBorder(int x, int z){
		
		return locIsWithin(x, z, WORLD_RADIUS);
	}
	
	private static int getRadius(String name) {
		return (worldBorderPlugin.getConfig().getInt("worlds." + name + ".radiusX"));
	}
	public static boolean isWithinBorderIncludePadding(int x, int z){
		return locIsWithin(x, z, WORLD_RADIUS - PADDING);
	}
	public static boolean isWithinBorderIncludePadding(int x, int z, int padding){
		return locIsWithin(x, z, WORLD_RADIUS - padding);
	}
	
	public static boolean locIsWithin(int x, int z, int radius){
		return (radius * radius) > (x * x + z * z);
	}
}