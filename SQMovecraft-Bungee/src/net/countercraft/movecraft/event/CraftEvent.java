package net.countercraft.movecraft.event;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CraftEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private Craft craft;
	private boolean cancelled = false;
	
	public CraftEvent(Craft c){
		craft = c;
	}
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	public Craft getCraft(){
		return craft;
	}
	public boolean call(){
		Bukkit.getServer().getPluginManager().callEvent(this);
		return !cancelled;
	}
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean arg0) {
		cancelled = arg0;
	}
}
