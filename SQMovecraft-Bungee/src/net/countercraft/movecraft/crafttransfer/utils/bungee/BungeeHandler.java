package net.countercraft.movecraft.crafttransfer.utils.bungee;

import org.bukkit.Bukkit;
import net.homeip.hall.sqnetevents.SQNetEvents;
import net.homeip.hall.sqnetevents.packet.ReceivedDataEvent;
import net.homeip.hall.sqnetevents.packet.Data;
import net.homeip.hall.sqnetevents.packet.EventPacket;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.PlayerTransferData;
import net.countercraft.movecraft.crafttransfer.utils.transfer.BungeeCraftConstructor;
import net.countercraft.movecraft.crafttransfer.utils.transfer.BungeeCraftReceiver;

public class BungeeHandler implements Listener {
	private static PlayerHandler playerHandler = new PlayerHandler();
	//called by SQNetEvents when a message is received
	@EventHandler
	public void onReceivedData(ReceivedDataEvent event) {
		System.out.println("ReceivedDataEvent successfully fired and received. Processing in BungeeHandler.");
		Data data = event.getData();
		String packetType = data.getString("Packet");
		System.out.println("PacketType: " + packetType);
		//indicates that craft is on DB, ready to be received. Executed by receiving end
		if(packetType.equals("CraftSpawnPacket")) {
			String targetServer = data.getString("TargetServer");
			final String pilot = data.getString("Pilot");
			//If this is the correct server
			if(Bukkit.getWorld(targetServer) != null) {
				Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
					public void run() {
						BungeeCraftReceiver.receiveCraft(pilot);
					}
				});
				System.out.println("Successfully dispatched CraftSpawnPacket to " + targetServer + " for pilot " + pilot);
			}
		}
		//indicates that craft has been received, ready to connect players. Executed by sending end
		else if(packetType.equals("ConnectPlayerPacket")) {
			final String targetServer = data.getString("TargetServer");
			String currentServer = data.getString("CurrentServer");
			final String player = data.getString("Player");
			//if this is the correct server
			if(Bukkit.getWorld(currentServer) != null) {
				Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
					public void run() {
						//Sends plugin message to connect player to server
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(targetServer);
						Bukkit.getPlayer(player).sendPluginMessage(Movecraft.getInstance(), "BungeeCord", out.toByteArray());
					}
				});
				System.out.println("Successfully dispatched ConnectPlayerPacket to " + currentServer + " for  player " + player);
			}
		}
		//indicates that all players have been teleported, ready for craft removal. Executed by sending end
		else if(packetType.equals("CraftRemovePacket")) {
			final String pilot = data.getString("Pilot");
			String oldServer = data.getString("OldServer");
			if(Bukkit.getWorld(oldServer) != null) {
				Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
					public void run() {
						BungeeCraftConstructor.findAndRemove(pilot);
					}
				});
			}
			System.out.println("Successfully dispatched CraftRemovePacket to " + oldServer + " for pilot " + pilot);
		}
	}
	//triggers target server to receive the craft in transit
	public static void sendCraftSpawnPacket(final String pilot, final String targetServer) {
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
			public void run() {
				Data data = new Data();
				data.addString("Packet", "CraftSpawnPacket");
				data.addString("Pilot", pilot);
				data.addString("TargetServer", targetServer);
				System.out.println("Senders: " + SQNetEvents.getInstance().getClients());
				SQNetEvents.getInstance().send(new EventPacket(new ReceivedDataEvent(data), targetServer), targetServer);
			}
		});
		System.out.println("Writing CraftSpawnPacket to sender");
	}
	//Called by receiving end. Tells sending end to connect player, and adds player to teleport queues.
	public static void sendConnectPlayerPacket(final PlayerTransferData playerData, final String oldServer, final SerializableLocation signLocation, final String pilot, final String craftType) {
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
			public void run() {
				System.out.println("Pilot: " + pilot);
				System.out.println("Player: " + playerData.getPlayer());
				System.out.println("In sendConnectPlayerPacket()");
				//sends packet
				Data data = new Data();
				String signLocationString = signLocation.toString();
				System.out.println("SignLocationString: " + signLocationString);
				data.addString("CurrentServer", oldServer);
				data.addString("TargetServer", signLocation.getWorldName());
				data.addString("Player", playerData.getPlayer());
				data.addString("Location", signLocationString);
				data.addString("Pilot", pilot);
				data.addString("Packet", "ConnectPlayerPacket");
				System.out.println("Old server: " + oldServer);
				SQNetEvents.getInstance().send(new EventPacket(new ReceivedDataEvent(data), oldServer), oldServer);
				//adds player to queues
				SerializableLocation l = new SerializableLocation(signLocation);
				l.offsetCoordinatesBy(playerData.getRelativeX(), playerData.getRelativeY(), playerData.getRelativeZ());
				SerializableLocation destinationLocation = new SerializableLocation(l.getWorldName(), l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw());
				playerHandler.addPlayerToTeleportQueue(playerData.getPlayer(), destinationLocation);
				playerHandler.addPlayerToPassengerMap(playerData.getPlayer(), pilot);
				playerHandler.addPlayerToDataMap(playerData.getPlayer(), playerData);
				if(playerData.getPlayer().equals(pilot)) {
					playerHandler.addPlayerToCraftTypeMap(playerData.getPlayer(), craftType);
				}
			}
		});
		System.out.println("Writing ConnectPlayerPacket to sender");
	}
	//Called by receiving after all players have been successfully teleported, triggers removal of original craft copy
	public static void sendCraftRemovePacket(final String pilot) {
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
			public void run() {
				Data data = new Data();
				String oldServer = Movecraft.getInstance().getSQLDatabase().readData(pilot).getOldServer();
				data.addString("Pilot", pilot);
				data.addString("OldServer", oldServer);
				data.addString("Packet", "CraftRemovePacket");
				SQNetEvents.getInstance().send(new EventPacket(new ReceivedDataEvent(data), oldServer), oldServer);
			}
		});
		System.out.println("Writing CraftRemovePacket to sender");
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		playerHandler.onPlayerJoin(event);
	}
}