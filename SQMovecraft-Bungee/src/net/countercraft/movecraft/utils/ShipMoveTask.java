package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ShipMoveTask extends BukkitRunnable{
	Craft c;
	Player p;
	
	//created when a ship is piloted and cancelled when the ship is released.
	public ShipMoveTask(Craft craft, Player player){
		c = craft;
		p = player;
		runTaskTimer(Movecraft.getInstance(), 1L, c.getType().getTickCooldown() / 4);
	}
	
	@Override
	public void run() {
		if(c.isAutopiloting){
			if(c.getW().getEnvironment() == Environment.THE_END){
				WarpUtils.translate(c, c.vX, 0, c.vZ);
			} else {
				c.translate(c.vX, 0, c.vZ);
			}
		} else {
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
						System.out.println(c.getVelocity());
					} else {
						AccelerationUtils.checkAndDecrementVelocity(c);
					}
					
					dx *= c.getVelocity();
					dy *= c.getVelocity();
					dz *= c.getVelocity();
					
	        		if(c.getType().isGroundVehicle()){
	        			dy = (CarUtils.getNewdY(c, dx, dz));
	        		}
	        		if(!(dx==0 && dy==0 && dz==0)){
	        			if(c.getW().getEnvironment() == Environment.THE_END){
	        				WarpUtils.translate(c, dx, dy, dz);
	        			} else {
	        				c.translate(dx, dy, dz);
	        			}
	        		}
					return;
				}
				return;
			}
		}
	}
}
