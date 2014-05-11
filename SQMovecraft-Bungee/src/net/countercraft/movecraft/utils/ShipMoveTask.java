package net.countercraft.movecraft.utils;

import java.util.HashMap;
import java.util.Map;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ShipMoveTask extends BukkitRunnable{
	Craft c;
	Player p;

	
	private static final Map<Player, Long> timeMap = new HashMap<Player, Long>();
	
	//created when a ship is piloted and cancelled when the ship is released.
	public ShipMoveTask(Craft craft, Player player){
		c = craft;
		p = player;
		runTaskTimer(Movecraft.getInstance(), 1L, 1L);
	}
	
	@Override
	public void run() {
		if(c.isAutopiloting){
			c.translate(c.vX, 0, c.vZ);
		} else {
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
					
					if(c.sneakPressed){
						AccelerationUtils.checkAndIncreaseVelocity(c);
					} else {
						AccelerationUtils.checkAndDecrementVelocity(c);
						if(c.getVelocity() <= 0){
							this.cancel();
							c.setVelocity(0);
						}
					}
					
					dx *= c.getVelocity();
					dz *= c.getVelocity();
					
	        		if(c.getType().isGroundVehicle()){
	        			dy = (CarUtils.getNewdY(c, dx, dz));
	        		}
		
					c.translate(dx, dy, dz);
					timeMap.put(p, System.currentTimeMillis());
					return;
				}
				return;
			}
		}
	}
}
