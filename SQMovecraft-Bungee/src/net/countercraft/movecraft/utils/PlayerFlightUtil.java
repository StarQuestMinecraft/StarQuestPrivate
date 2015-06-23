package net.countercraft.movecraft.utils;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerFlightUtil {
	
	private static HashSet<UUID> shipFlyingPlayers = new HashSet<UUID>();
	private static HashSet<UUID> teleportFlyingPlayers = new HashSet<UUID>();
	
	public static void beginShipFlying(Player p){
		shipFlyingPlayers.add(p.getUniqueId());
		p.setAllowFlight(true);
		p.setFlying(true);
	}
	
	public static void beginTeleportFlying(Player p){
		teleportFlyingPlayers.add(p.getUniqueId());
		System.out.println("TeleportFlyingPlayers (begin): " + teleportFlyingPlayers.size());
		System.out.println("shipFlyingPlayers: " + shipFlyingPlayers.size());
		p.setAllowFlight(true);
		p.setFlying(true);
	}
	
	public static void endShipFlying(Player p){
		shipFlyingPlayers.remove(p.getUniqueId());
		if(!teleportFlyingPlayers.contains(p.getUniqueId())){
			if(p.getGameMode() == GameMode.SURVIVAL){
				p.setAllowFlight(false);
			}
			p.setFlying(false);
		}
	}
	
	public static void endTeleportFlying(Player p){
		System.out.println("TeleportFlyingPlayers (end): " + teleportFlyingPlayers.size());
		System.out.println("shipFlyingPlayers: " + shipFlyingPlayers.size());
		teleportFlyingPlayers.remove(p.getUniqueId());
		if(!shipFlyingPlayers.contains(p.getUniqueId())){
			if(p.getGameMode() == GameMode.SURVIVAL){
				p.setAllowFlight(false);
			}
			p.setFlying(false);
		}
	}
	
	public static void removeFlightUnlessAllowed(Player p){
		if(!p.getAllowFlight() && !p.isFlying()) return;
		if(!isShipFlying(p) && !isTeleportFlying(p)){
			p.setAllowFlight(false);
			p.setFlying(false);
		}
	}
	
	public static boolean isShipFlying(UUID u){
		return shipFlyingPlayers.contains(u);
	}
	
	public static boolean isShipFlying(Player p){
		return isShipFlying(p.getUniqueId());
	}
	
	public static boolean isTeleportFlying(UUID u){
		return teleportFlyingPlayers.contains(u);
	}
	
	public static boolean isTeleportFlying(Player p){
		return teleportFlyingPlayers.contains(p.getUniqueId());
	}
	
}
