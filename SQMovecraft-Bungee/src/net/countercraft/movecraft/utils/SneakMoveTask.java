package net.countercraft.movecraft.utils;

import java.util.HashMap;
import java.util.Map;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SneakMoveTask extends BukkitRunnable{
	Craft c;
	Player p;
	private static final Map<Player, Long> timeMap = new HashMap<Player, Long>();
	
	public SneakMoveTask(Craft craft, Player player){
		c = craft;
		p = player;
	}
	
	@Override
	public void run() {
		Long time = timeMap.get(p);
		if (time != null) {
			long ticksElapsed = (System.currentTimeMillis() - time) / 50;
			if (Math.abs(ticksElapsed) < c.getType().getTickCooldown()) {
				return;
			}
		}
		if((!c.processing.get()) && (!c.shipAttemptingTeleport)){
			if (MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
	
				float rotation = (float) Math.PI * p.getLocation().getYaw() / 180f;
	
				float nx = -(float) Math.sin(rotation);
				float nz = (float) Math.cos(rotation);
	
				int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
				int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
				int dy;
	
				float pf = p.getLocation().getPitch();
	
				dy = -(Math.abs(pf) >= 25 ? 1 : 0) * (int) Math.signum(pf);
	
				if (Math.abs(p.getLocation().getPitch()) >= 75) {
					dx = 0;
					dz = 0;
				}
	
				c.translate(dx, dy, dz);
				timeMap.put(p, System.currentTimeMillis());
				return;
			}
			return;
		}
	}
}
