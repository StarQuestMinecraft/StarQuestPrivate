package net.countercraft.movecraft.event;

import org.bukkit.entity.Player;

import net.countercraft.movecraft.database.StarshipData;

public class CraftSignBreakEvent extends CraftEvent{

	boolean breakerOwner;
	boolean cooledDown;
	StarshipData data;
	Player player;
	public CraftSignBreakEvent(StarshipData data, boolean breakerOwner, boolean cooledDown, Player breaker) {
		super(null);
		this.data = data;
		this.breakerOwner = breakerOwner;
		this.cooledDown = cooledDown;
		player = breaker;
		// TODO Auto-generated constructor stub
	}
	
	public boolean isBrokenByOwner(){
		return breakerOwner;
	}
	
	public boolean isCooledDown(){
		return cooledDown;
	}
	
	public StarshipData getData(){
		return data;
	}
	
	public Player getPlayer(){
		return player;
	}
}
