package net.countercraft.movecraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
	public static boolean isWithinBorder(Location l){
		
		return locIsWithin(l, WORLD_RADIUS);
	}
	
	private static int getRadius(String name) {
		return (worldBorderPlugin.getConfig().getInt("worlds." + name + ".x"));
	}
	public static boolean isWithinBorderIncludePadding(Location l){
		return locIsWithin(l, WORLD_RADIUS - PADDING);
	}
	
	public static boolean locIsWithin(Location l, int radius){
		return (radius * radius) > (l.getX() * (l.getX()) + l.getZ() * l.getZ());
	}
}