package net.countercraft.movecraft.bungee;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable {
    private double x,y,z;
    private float yaw, pitch;
    private String world;
    public SerializableLocation(String world, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        yaw = 0;
        pitch = 0;
    }
    public SerializableLocation(String world, double x, double y, double z, float yaw, float pitch) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.world = world;
    	this.yaw = yaw;
    	this.pitch = pitch;
    }
    public String getWorldName() {
    	return world;
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
    public float getYaw() {
    	return yaw;
    }
    public float getPitch() {
    	return pitch;
    }
}
