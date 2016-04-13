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
	
	public SerializableLocation(Location l) {
		worldName = l.getWorld().getName();
		x = l.getX();
		y = l.getY();
		z = l.getZ();
	}
	private SerializableLocation(SerializableLocation l) {
		worldName = l.getWorldName();
		x = l.getX();
		y = l.getY();
		z = l.getZ();
	}
	public SerializableLocation(String aWorldName, double xValue, double yValue, double zValue) {
		worldName = aWorldName;
		x = xValue;
		y = yValue;
		z = zValue;
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
}
