package net.countercraft.movecraft.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CraftProjectileDetonateEvent extends CraftEvent{

	Player plr;
	Block explode;
	
	public CraftProjectileDetonateEvent(Player shooter, Block explosion) {
		super(null);
		plr = shooter;
		explode = explosion;
		// TODO Auto-generated constructor stub
	}

	public Player getShooter(){
		return plr;
	}
	
	public Block getExplosionBlock(){
		return explode;
	}
	
}
