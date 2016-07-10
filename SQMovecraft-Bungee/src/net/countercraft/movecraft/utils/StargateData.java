package net.countercraft.movecraft.utils;

import org.bukkit.Location;

public class StargateData {
	private int orientation; // 0 = north/south, 1 = east/west
	private Location startLocation; //location of stargate, calculate its hitbox based on this and orientation
	private Location targetLocation; // place to send ship, NOT the center of the target stargate
	private String targetServer; // Server to send the ship to
	
	public StargateData(int orientation, Location start, Location target, String server){
		this.setOrientation(orientation);
		setStartLocation(start);
		setTargetLocation(target);
		setTargetServer(server);
	}

	public Location getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}

	public Location getTargetLocation() {
		return targetLocation;
	}

	public void setTargetLocation(Location targetLocation) {
		this.targetLocation = targetLocation;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public String getTargetServer() {
		return targetServer;
	}

	public void setTargetServer(String targetServer) {
		this.targetServer = targetServer;
	}
}
