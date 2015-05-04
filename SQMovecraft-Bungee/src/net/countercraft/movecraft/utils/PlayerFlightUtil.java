package net.countercraft.movecraft.utils;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerFlightUtil {
	
	private static HashSet<UUID> flyingPlayers = new HashSet<UUID>();
	
	public static void beginShipFlying(Player p){
		flyingPlayers.add(p.getUniqueId());
		p.setAllowFlight(true);
		p.setFlying(true);
	}
	
	public static void endShipFlying(Player p){
		flyingPlayers.remove(p.getUniqueId());
		if(p.getGameMode() == GameMode.SURVIVAL){
			p.setAllowFlight(false);
		}
		p.setFlying(false);
	}
	
	public static boolean isShipFlying(UUID u){
		return flyingPlayers.contains(u);
	}
	
	public static boolean isShipFlying(Player p){
		return isShipFlying(p.getUniqueId());
	}
}
