package net.countercraft.movecraft.event;

import org.bukkit.entity.Player;

import net.countercraft.movecraft.database.StarshipData;

public class CraftSignBreakEvent extends CraftEvent{

	boolean breakerOwner;
	boolean withinCooldown;
	StarshipData data;
	Player player;
	public CraftSignBreakEvent(StarshipData data, boolean breakerOwner, boolean isWithinKillCooldown, Player breaker) {
		super(null);
		this.data = data;
		this.breakerOwner = breakerOwner;
		this.withinCooldown = isWithinKillCooldown;
		player = breaker;
		// TODO Auto-generated constructor stub
	}
	
	public boolean isBrokenByOwner(){
		return breakerOwner;
	}
	
	public boolean isWithinKillCooldown(){
		return withinCooldown;
	}
	
	public StarshipData getData(){
		return data;
	}
	
	public Player getPlayer(){
		return player;
	}
}
