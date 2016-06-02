package net.countercraft.movecraft.crafttransfer.utils.bungee;

import org.bukkit.event.Listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;

public class PlayerHandler implements Listener {
	private HashMap<String, SerializableLocation> playerTeleportQueue;
	private HashMap<String, String> passengerPilotMap;
	public PlayerHandler() {
		playerTeleportQueue = new HashMap<String, SerializableLocation>();
		passengerPilotMap = new HashMap<String, String>();
	}
	//Called on receiving end. Will teleport players to appropriate location after serverjump and remove them from queue appropriately
	public void onPlayerJoin(PlayerJoinEvent event) {
		System.out.println("In onPlayerLogin");
		String player = event.getPlayer().getName();
		if(getPlayerTeleportQueue().containsKey(player)) {
			System.out.println("Player is in teleport queue");
			teleportPlayer(event.getPlayer(), getPlayerTeleportQueue().get(player), getPassengerPilotMap().get(player));
		}
	}
	//Maps player to teleport location
	public HashMap<String, SerializableLocation> getPlayerTeleportQueue() {
		return playerTeleportQueue;
	}
	//Maps player to pilot
	public HashMap<String, String> getPassengerPilotMap() {
		return passengerPilotMap;
	}
	public void addPlayerToTeleportQueue(String player, SerializableLocation signLocation) {
		getPlayerTeleportQueue().put(player, signLocation);
		System.out.println("Added player " + player + " to teleport queue");
	}
	public void addPlayerToPassengerMap(String player, String pilot) {
		getPassengerPilotMap().put(player, pilot);
		System.out.println("Added player " + player + " to passenger map");
	}
	//teleports player and removes them from queues. Tells sending server to remove craft if this was the last player onboard
	private void teleportPlayer(Player player, SerializableLocation signLocation, String pilot) {
		Location l = signLocation.getLocation();
		System.out.println(l.getX());
		System.out.println(l.getY());
		System.out.println(l.getZ());
		System.out.println(player.getName());
		player.teleport(l);
		//If no more players left to teleport
		if(!(getPassengerPilotMap().containsValue(pilot))) {
			BungeeHandler.sendCraftRemovePacket(pilot);
			System.out.println("Successfully sent CraftRemovePacket for pilot " + pilot);
		}
	}
}
