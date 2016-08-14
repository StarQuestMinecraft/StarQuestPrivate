package net.countercraft.movecraft.task;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.craft.Craft;

import net.countercraft.movecraft.crafttransfer.utils.transfer.BungeeCraftSender;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RepeatTryServerJumpTask extends BukkitRunnable{
	
	Craft c;
	SerializableLocation destinationLocation;
	
	public static void createServerJumpTask(final Craft craft, final SerializableLocation location) {
		System.out.println("In createServerJumpTask for pilot " + craft.getPilot());
		Bukkit.getServer().getScheduler().runTask(Movecraft.getInstance(), new Runnable() {
			@Override
			public void run() {
				RepeatTryServerJumpTask t = new RepeatTryServerJumpTask(craft, location);
				t.runTaskTimer(Movecraft.getInstance(), 0, 2);
			}
		});
	}
	private RepeatTryServerJumpTask(Craft craft, SerializableLocation location) {
		c = craft;
		destinationLocation = location;
	}
	@Override 
	public void run(){
		if (!c.isProcessing() ){
			cancel();
			if(c.pilot != null) c.pilot.sendMessage("Preparing serverjump...");
			Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				@Override
				public void run(){
					BungeeCraftSender.sendCraft(destinationLocation, c);
				}
			}, 20L);
		}
	}
}