package net.countercraft.movecraft.database;

import org.bukkit.Location;

public interface StarshipDatabase {
	public StarshipData getStarshipByLocation(Location l);
	public void removeStarshipAtLocation(Location l);
	public void saveStarshipDataAtLocation(Location l, StarshipData d);
}
