package net.countercraft.movecraft.crafttransfer.utils.bungee;

import org.bukkit.event.Listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.PlayerTransferData;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.MovecraftLocation;

public class PlayerHandler implements Listener {
	private HashMap<String, SerializableLocation> playerTeleportQueue;
	private HashMap<String, PlayerTransferData> playerDataMap;
	private HashMap<String, String> passengerPilotMap;
	private HashMap<String, String> pilotCraftTypeMap;
	public PlayerHandler() {
		playerTeleportQueue = new HashMap<String, SerializableLocation>();
		passengerPilotMap = new HashMap<String, String>();
		playerDataMap = new HashMap<String, PlayerTransferData>();
		pilotCraftTypeMap = new HashMap<String, String>();
	}
	//Called on receiving end. Will teleport players to appropriate location after serverjump and remove them from queue appropriately
	public void onPlayerJoin(final PlayerJoinEvent event) {
		System.out.println("In onPlayerLogin");
		final String player = event.getPlayer().getName();
		final boolean isPilot = getPassengerPilotMap().containsKey(player);
		if(getPlayerTeleportQueue().containsKey(player)) {
			System.out.println("Player is in teleport queue");
			Bukkit.getScheduler().runTaskLater(Movecraft.getInstance(), new Runnable() {
				@Override
				public void run() {
					PlayerTransferData data = getPlayerDataMap().get(player);
					teleportPlayer(event.getPlayer(), getPlayerTeleportQueue().get(player), getPassengerPilotMap().get(player));
					data.unpack(event.getPlayer());
					getPlayerTeleportQueue().remove(player);
					getPlayerDataMap().remove(player);
					//if pilot, detect craft
					if(isPilot) {
						detectCraft(event.getPlayer(), getPilotCraftTypeMap().get(player));
						getPilotCraftTypeMap().remove(player);
					}
				}
			}, 6L);
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
	
	public HashMap<String, PlayerTransferData> getPlayerDataMap() {
		return playerDataMap;
	}
	private HashMap<String, String> getPilotCraftTypeMap() {
		return pilotCraftTypeMap;
	}
	public void addPlayerToTeleportQueue(String player, SerializableLocation signLocation) {
		getPlayerTeleportQueue().put(player, signLocation);
		System.out.println("Added player " + player + " to teleport queue");
	}
	public void addPlayerToPassengerMap(String player, String pilot) {
		getPassengerPilotMap().put(player, pilot);
		System.out.println("Added player " + player + " to passenger map");
	}
	public void addPlayerToDataMap(String player, PlayerTransferData data) {
		getPlayerDataMap().put(player, data);
		System.out.println("Added player " + player + " to data map");
	}
	public void addPlayerToCraftTypeMap(String player, String craftType) {
		getPilotCraftTypeMap().put(player, craftType);
	}
	//teleports player and removes them from queues. Tells sending server to remove craft if this was the last player onboard
	private void teleportPlayer(Player player, SerializableLocation signLocation, String pilot) {
		Location l = signLocation.getLocation();
		System.out.println(l.getX());
		System.out.println(l.getY());
		System.out.println(l.getZ());
		System.out.println(player.getName());
		player.teleport(l);
		getPassengerPilotMap().remove(player.getName());
		//If no more players left to teleport
		if(!(getPassengerPilotMap().containsValue(pilot))) {
			BungeeHandler.sendCraftRemovePacket(pilot);
			System.out.println("Successfully sent CraftRemovePacket for pilot " + pilot);
		}
	}
	private void detectCraft(Player player, String craftType) {
		CraftType type = InteractListener.getCraftTypeFromString(craftType);
		System.out.println("Craft type: " + craftType);
		Craft c = new Craft(type , player.getWorld());
		MovecraftLocation l = new MovecraftLocation((int) player.getLocation().getX(), (int) player.getLocation().getY(), (int) player.getLocation().getZ());
		c.detect(player, l);
	}
}