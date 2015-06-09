package net.countercraft.movecraft.event;

import org.bukkit.entity.Player;

import net.countercraft.movecraft.craft.Craft;

public class CraftGunsHitEvent extends CraftEvent{

	Player shooter;
	Player plr;
	
	
	public CraftGunsHitEvent(Craft c, Player shooter, Player plr) {
		super(c);
	}
	
	public Player getShooter(){
		return shooter;
	}
	
	public Player getHit(){
		return plr;
	}
}
