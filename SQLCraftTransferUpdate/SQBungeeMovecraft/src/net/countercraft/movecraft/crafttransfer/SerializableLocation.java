package net.countercraft.movecraft.crafttransfer;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableLocation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String worldName;
	private double x;
	private double y;
	private double z;
	private double pitch;
	private double yaw;
	
	public SerializableLocation(Location l) {
		worldName = l.getWorld().getName();
		x = l.getX();
		y = l.getY();
		z = l.getZ();
		pitch = l.getPitch();
		yaw = l.getYaw();
	}
	public SerializableLocation(SerializableLocation l) {
		worldName = l.getWorldName();
		x = l.getX();
		y = l.getY();
		z = l.getZ();
		pitch = l.getPitch();
		yaw = l.getYaw();
	}
	public SerializableLocation(String aWorldName, double xValue, double yValue, double zValue) {
		worldName = aWorldName;
		x = xValue;
		y = yValue;
		z = zValue;
	}
	public SerializableLocation(String aWorldName, double xValue, double yValue, double zValue, double pitchValue, double yawValue) {
		worldName = aWorldName;
		x = xValue;
		y = yValue;
		z = zValue;
		pitch = pitchValue;
		yaw = yawValue;
	}
	public String getWorldName() {
		return worldName;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}
	public double getPitch() {
		return pitch;
	}
	public double getYaw() {
		return yaw;
	}
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldName), x, y, z);
	}
	public void offsetCoordinatesBy(double xValue, double yValue, double zValue) {
		x += xValue;
		y += yValue;
		z += zValue;
	}
	public void setCoordinates(double xValue, double yValue, double zValue) {
		x = xValue;
		y = yValue;
		z = zValue;
	}
	//returns shallow copy
	public SerializableLocation copy() {
		return new SerializableLocation(this);
	}
	@Override
	public String toString() {
		String locationString = worldName + "," + Double.toString(x) + "," + Double.toString(y) + "," + Double.toString(z);
		return locationString;
	}
	public static SerializableLocation fromString(String locationString) {
		String[] values = locationString.split(",");
		double x = Double.parseDouble(values[0]);
		double y = Double.parseDouble(values[1]);
		double z = Double.parseDouble(values[2]);
		String worldName = values[3];
		return new SerializableLocation(worldName, x, y, z);
	}
}
