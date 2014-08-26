package net.countercraft.movecraft.database;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Location;

public interface StarshipDatabase {
	public StarshipData getStarshipByLocation(Location l);
	public void removeStarshipAtLocation(Location l);
	public void saveStarshipAtLocation(Craft c);
}
