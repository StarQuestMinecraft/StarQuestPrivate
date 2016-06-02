package net.countercraft.movecraft.crafttransfer.utils.transfer;

import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.PlayerTransferData;
import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;
import net.countercraft.movecraft.crafttransfer.utils.bungee.BungeeHandler;

public class BungeeCraftReceiver {
	public static void receiveCraft(String pilot) {
		System.out.println("Received CraftSpawnPacket for pilot " + pilot);
		TransferData transferData = readData(pilot);
		SerializableLocation signLocation = BungeeCraftConstructor.calculateAndBuild(transferData);
		if(signLocation != null) {
			sendPlayerServerjumpPackets(transferData.getPlayerData(), transferData.getOldServer(), signLocation, pilot);
		}
	}
	//grabs transferdata from db
	private static TransferData readData(String pilot) {
		return Movecraft.getInstance().getSQLDatabase().readData(pilot);
	}
	//Sends packets to connect (and later, indirectly, to teleport) players across server, called after craft successfully built
	private static void sendPlayerServerjumpPackets(ArrayList<PlayerTransferData> pData, String oldWorld, SerializableLocation signLocation, String pilot) {
		//iterates over players and sends packet
		for(PlayerTransferData playerData : pData) {
			System.out.println("Sent ConnectPlayerPacket for player " + playerData.getPlayer());
			BungeeHandler.sendConnectPlayerPacket(playerData.getPlayer(), oldWorld, signLocation, pilot);
		}
	}
}
