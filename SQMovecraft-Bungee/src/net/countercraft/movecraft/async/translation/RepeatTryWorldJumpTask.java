package net.countercraft.movecraft.async.translation;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RepeatTryWorldJumpTask extends BukkitRunnable{
	Craft c;
	Player pilot;
	Location locto;
	boolean isEnteringPlanet;
	String targetWorldName;
	
	public RepeatTryWorldJumpTask(Craft c, Player pilot, Location locto){
		this.c = c;
		this.pilot = pilot;
		this.locto = locto;
	}
	@Override
	public void run(){
		if (!c.processing.get()){
			boolean success = false;
			while(!success){
				success = TeleportTask.worldJump(pilot, c, locto);
				locto = new Location(locto.getWorld(), locto.getX() + 50, locto.getY(), locto.getZ());
			}
			c.shipAttemptingTeleport = false;
			this.cancel();
		}
	}
}
