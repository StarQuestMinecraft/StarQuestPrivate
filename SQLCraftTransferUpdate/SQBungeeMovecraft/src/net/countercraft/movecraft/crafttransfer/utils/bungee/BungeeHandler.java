package net.countercraft.movecraft.crafttransfer.utils.bungee;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.entity.Player;

import net.homeip.hall.sqnetevents.networking.Sender;
import net.homeip.hall.sqnetevents.SQNetEvents;
import net.homeip.hall.sqnetevents.packet.ReceivedDataEvent;
import net.homeip.hall.sqnetevents.packet.Data;
import net.homeip.hall.sqnetevents.packet.EventPacket;

import me.redepicness.socketmessenger.bukkit.SocketAPI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;

import net.countercraft.movecraft.crafttransfer.utils.transfer.BungeeCraftConstructor;
import net.countercraft.movecraft.crafttransfer.utils.transfer.BungeeCraftReceiver;

public class BungeeHandler implements Listener {
	private static PlayerHandler playerHandler;
	//called by SocketMessenger when a message is received
	@EventHandler
	public static void onReceivedData(ReceivedDataEvent event) {
		Data data = event.getData();
		String packetType = data.getString("Packet");
		//indicates that craft is on DB, ready to be received
		if(packetType.equals("CraftSpawnPacket")) {
			String targetServer = data.getString("TargetServer");
			String pilot = data.getString("Pilot");
			//If this is the correct server
			if(Bukkit.getWorld(targetServer) != null) {
				BungeeCraftReceiver.receiveCraft(pilot);
				System.out.println("Successfully dispatched CraftSpawnPacket to " + targetServer + " for pilot " + pilot);
			}
		}
		//indicates that craft has been received, ready to connect players
		else if(packetType.equals("ConnectPlayerPacket")) {
			String targetServer = data.getString("TargetServer");
			String currentServer = data.getString("CurrentServer");
			String location = data.getString("Location");
			String player = data.getString("Player");
			String pilot = data.getString("Pilot");
			//if this is the correct server
			if(Bukkit.getWorld(currentServer) != null) {
				SocketAPI.connectPlayerToServer(player, targetServer);
				playerHandler.getPlayerTeleportQueue().put(player, location);
				playerHandler.getPassengerPilotMap().put(player, pilot);
				System.out.println("Successfully dispatched ConnectPlayerPacket to " + targetServer + " for  player " + player);
			}
		}
		//indicates that player has been connected, ready for teleport
		else if(packetType.equals("PlayerTeleportPacket")) {
			String targetServer  = data.getString("CurrentServer");
			String player = data.getString("Player");
			String location = data.getString("Location");
			String pilot = data.getString("Pilot");
			//If this is the correct server
			if(Bukkit.getWorld(targetServer) != null) {
				Player p = Bukkit.getPlayer(player);
				World w = Bukkit.getWorld(targetServer);
				double x = Double.parseDouble(location.split(",")[0]);
				double y = Double.parseDouble(location.split(",")[1]);
				double z = Double.parseDouble(location.split(",")[2]);
				Location l = new Location(w, x, y, z);
				p.teleport(l);
				playerHandler.getPlayerTeleportQueue().remove(player);
				playerHandler.getPassengerPilotMap().remove(player);
				System.out.println("Successfully dispatched PlayerTeleportPacket to " + targetServer + " for player " + player);
				//If no more players left to teleport
				if((!playerHandler.getPlayerTeleportQueue().containsValue(location))) {
					sendCraftRemovePacket(pilot);
					System.out.println("Successfully sent CraftRemovePacket for pilot " + pilot);
				}
			}
		}
		//indicates that all players have been teleported, ready for craft removal
		else if(packetType.equals("CraftRemovePacket")) {
			String pilot = data.getString("Pilot");
			String oldServer = data.getString("OldServer");
			if(Bukkit.getWorld(oldServer) != null) {
				BungeeCraftConstructor.findAndRemove(pilot);
			}
			System.out.println("Successfully dispatched CraftRemovePacket to " + oldServer + " for pilot " + pilot);
		}
	}
	//triggers target server to receive the craft in transit
	public static void sendCraftSpawnPacket(String pilot, String targetServer) {
		Data data = new Data();
		data.addString("Packet", "CraftSpawnPacket");
		data.addString("Pilot", pilot);
		data.addString("TargetServer", targetServer);
		System.out.println("Senders: " + SQNetEvents.getInstance().getSenders());
		for(Sender sender : SQNetEvents.getInstance().getSenders()) {
			sender.send(new EventPacket(new ReceivedDataEvent(data)));
			System.out.println("Writing CraftSpawnPacket to sender");
		}
	}
	//connects player to server
	public static void sendConnectPlayerPacket(String player, String oldServer, SerializableLocation location, String pilot) {
		Data data = new Data();
		String signLocationString = location.getX() + "," + location.getY() + "," + location.getZ();
		data.addString("CurrentServer", oldServer);
		data.addString("TargetServer", location.getWorldName());
		data.addString("Player", player);
		data.addString("Location", signLocationString);
		data.addString("Pilot", pilot);
		data.addString("Packet", "ConnectPlayerPacket");
		for(Sender sender : SQNetEvents.getInstance().getSenders()) {
			sender.send(new EventPacket(new ReceivedDataEvent(data)));
		}
	}
	//called when player logs onto the server, triggers teleport to the craft
	public static void sendPlayerTeleportPacket(String player, String targetServer, String location, String pilot) {
		Data data = new Data();
		data.addString("Player", player);
		data.addString("CurrentServer", targetServer);
		data.addString("Location", location);
		data.addString("Pilot", pilot);
		data.addString("Packet", "PlayerTeleportPacket");
		for(Sender sender : SQNetEvents.getInstance().getSenders()) {
			sender.send(new EventPacket(new ReceivedDataEvent(data)));
		}
	}
	//Called after all players have been successfully teleported, triggers removal of original craft copy
	public static void sendCraftRemovePacket(String pilot) {
		Data data = new Data();
		String oldServer = Movecraft.getInstance().getSQLDatabase().readData(pilot).getOldServer();
		data.addString("Pilot", pilot);
		data.addString("OldServer", oldServer);
		data.addString("Packet", "CraftRemovePacket");
		for(Sender sender : SQNetEvents.getInstance().getSenders()) {
			sender.send(new EventPacket(new ReceivedDataEvent(data)));
		}
	}
}
