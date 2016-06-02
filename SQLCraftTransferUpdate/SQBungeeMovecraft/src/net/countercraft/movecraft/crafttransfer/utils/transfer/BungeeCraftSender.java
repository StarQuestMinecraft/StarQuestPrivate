package net.countercraft.movecraft.crafttransfer.utils.transfer;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;
import net.countercraft.movecraft.crafttransfer.utils.bungee.BungeeHandler;
import net.homeip.hall.sqnetevents.packet.ReceivedDataEvent;

public class BungeeCraftSender {
	//Called externally
	public static void sendCraft(SerializableLocation destinationLocation, final Craft c) {
		TransferData data = buildTransferData(c, destinationLocation);
		writeToDatabase(data);
		sendCraftSpawnPacket(c.getPilot().getName(), destinationLocation.getWorldName());
	}
	private static TransferData buildTransferData(Craft c, SerializableLocation destinationLocation) {
		System.out.println("Built TransferData object for pilot " + c.getPilot().getName());
		return new TransferData(c, destinationLocation);
	}
	//Writes the data to the SQL db
	private static void writeToDatabase(TransferData data) {
		Movecraft.getInstance().getSQLDatabase().writeData(data);
		System.out.println("Transfer data returned: " + Movecraft.getInstance().getSQLDatabase().readData(data.getPilot()));
	}
	//Sends a message to Bungee-linked servers through SocketMessenger that triggers craft reception
	private static void sendCraftSpawnPacket(String pilot, String targetServer) {
		BungeeHandler.sendCraftSpawnPacket(pilot, targetServer);
		System.out.println("Sent CraftSpawnPacket for pilot " + pilot);
		System.out.println("Test");
	}
}
