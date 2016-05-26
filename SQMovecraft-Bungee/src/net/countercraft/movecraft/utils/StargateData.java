package net.countercraft.movecraft.utils;

import org.bukkit.Location;

public class StargateData {
	public int orientation; // 0 = north/south, 1 = east/west
	public Location startLocation; //location of stargate, calculate its hitbox based on this and orientation
	public Location targetLocation; // place to send ship, NOT the center of the target stargate
	public String targetServer; // Server to send the ship to
	
	public StargateData(int orientation, Location start, Location target, String server){
		this.orientation = orientation;
		startLocation = start;
		targetLocation = target;
		targetServer = server;
	}
}
