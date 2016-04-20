package net.countercraft.movecraft.task;

import java.io.IOException;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bungee.BungeeCraftSender;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.event.CraftServerJumpEvent;
import net.countercraft.movecraft.slip.WarpUtils;
import net.countercraft.movecraft.utils.PlayerFlightUtil;

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
	
	public static void createServerJumpTask(final Player p, final Craft c, final String server, final int x, final int y, final int z){
		c.setProcessingTeleport(true);
		CraftServerJumpEvent event = new CraftServerJumpEvent(c, Bukkit.getServerName(), server);
		if(event.call()){
			Bukkit.getServer().getScheduler().runTask(Movecraft.getInstance(), new Runnable(){
				public void run(){
					RepeatTryServerJumpTask t = new RepeatTryServerJumpTask(p, c, server, x, y, z);
					t.runTaskTimer(Movecraft.getInstance(), 0, 1);
				}
			});
		} else {
			p.sendMessage("You cannot enter this world.");
		}
	}
	public static void createServerJumpTask(final Player p, final Craft c, final String server, final String world, final int x, final int y, final int z){
		Bukkit.getServer().getScheduler().runTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				RepeatTryServerJumpTask t = new RepeatTryServerJumpTask(p, c, server, world, x, y, z);
				t.runTaskTimer(Movecraft.getInstance(), 0, 1);
			}
		});
	}
	
	public RepeatTryServerJumpTask(Player p, Craft c, String server, int x, int y, int z){
		this.c = c;
		this.server = server;
		this.world = server;
		this.x = x;
		this.y = y;
		this.z = z;
		this.p = p;
		for(UUID u : c.playersRidingShip){
			Player plr = Movecraft.getPlayer(u);
			PlayerFlightUtil.beginTeleportFlying(plr);
		}
	}
	
	public RepeatTryServerJumpTask(Player p, Craft c, String server, String world, int x, int y, int z){
		this.c = c;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.p = p;
		for(UUID u : c.playersRidingShip){
			Player plr = Movecraft.getPlayer(u);
			PlayerFlightUtil.beginTeleportFlying(plr);
		}
	}
	
	@Override 
	public void run(){
		if (!c.isProcessing() ){
			cancel();
			if(c.pilot != null) c.pilot.sendMessage("Preparing serverjump...");
			Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				public void run(){
					try {
						BungeeCraftSender.sendCraft(p, server, world, x, y, z, c);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 20L);
		}
	}
}
