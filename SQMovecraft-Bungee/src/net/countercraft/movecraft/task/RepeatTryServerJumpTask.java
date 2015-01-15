package net.countercraft.movecraft.task;

import java.io.IOException;

import net.countercraft.movecraft.bungee.BungeeCraftSender;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.slip.WarpUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RepeatTryServerJumpTask extends BukkitRunnable{
	Craft c;
	String server;
	String world;
	int x;
	int y;
	int z;
	Player p;
	
	public RepeatTryServerJumpTask(Player p, Craft c, String server, int x, int y, int z){
		this.c = c;
		this.server = server;
		this.world = server;
		this.x = x;
		this.y = y;
		this.z = z;
		this.p = p;
			p.setAllowFlight(true);
			p.setFlySpeed(0F);
			p.setFlying(true);
	}
	
	public RepeatTryServerJumpTask(Player p, Craft c, String server, String world, int x, int y, int z){
		this.c = c;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.p = p;
			p.setAllowFlight(true);
			p.setFlySpeed(0F);
			p.setFlying(true);
	}
	
	@Override 
	public void run(){
		if (!c.isProcessing() ){
			try {
				BungeeCraftSender.sendCraft(p, server, world, x, y, z, c);
				cancel();
			} catch (IOException e) {
				e.printStackTrace();
				cancel();
			}
		}
	}
}
