package net.countercraft.movecraft.projectile;

import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileFlyTask extends BukkitRunnable{
	Projectile myTorpedo;
	public ProjectileFlyTask(Projectile projectile){
		myTorpedo = projectile;
	}
	
	public void run(){
		myTorpedo.taskMove();
	}
}
