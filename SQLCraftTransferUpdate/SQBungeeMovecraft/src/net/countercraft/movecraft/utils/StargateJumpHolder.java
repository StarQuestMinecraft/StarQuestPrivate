package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.entity.Player;

public class StargateJumpHolder {
	public Craft c;
	public String server;
	public int x, y, z;
	public Player p;
	
	public StargateJumpHolder(Player p, Craft c, String server, int x, int y, int z){
		this.c = c;
		this.server = server;
		this.x = x;
		this.y = y;
		this.z = z;
		this.p = p;
	}
}
