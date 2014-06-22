package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Torpedo;

import org.bukkit.scheduler.BukkitRunnable;

public class TorpedoFlyTask extends BukkitRunnable{
	Torpedo myTorpedo;
	public TorpedoFlyTask(Torpedo t){
		myTorpedo = t;
	}
	
	public void run(){
		myTorpedo.move();
	}
}
