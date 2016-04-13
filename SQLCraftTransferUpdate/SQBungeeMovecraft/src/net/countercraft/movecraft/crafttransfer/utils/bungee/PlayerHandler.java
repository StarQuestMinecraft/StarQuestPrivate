package net.countercraft.movecraft.crafttransfer.utils.bungee;

import org.bukkit.event.Listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerHandler implements Listener {
	private HashMap<String, String> playerTeleportQueue = new HashMap<String, String>();
	private HashMap<String, String> passengerPilotMap = new HashMap<String, String>();
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		String player = event.getPlayer().getName();
		if(getPlayerTeleportQueue().containsKey(player)) {
			sendPlayerTeleportPacket(player, Bukkit.getServerName(), getPlayerTeleportQueue().get(player), getPassengerPilotMap().get(player));
		}
	}
	//Maps player to teleport location
	public HashMap<String, String> getPlayerTeleportQueue() {
		return playerTeleportQueue;
	}
	//Maps player to pilot
	public HashMap<String, String> getPassengerPilotMap() {
		return passengerPilotMap;
	}
	//Indicates that player has been teleported
	private void sendPlayerTeleportPacket(String player, String currentServer, String location, String pilot) {
		BungeeHandler.sendPlayerTeleportPacket(player, currentServer, location, pilot);
		System.out.println("Successfully sent player teleport packet for player " + player);
	}
}
