package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.dibujaron.cardboardbox.Knapsack;

/**
 * @author Dibujaron
 * BUNGEECORD: TURN BACK NOW FAINT OF HEART
 * This class recieves data from BungeeListener, loads files, and processes the data stream. It then calls BungeeCraftConstructor to build the ship in the world.
 */
public class BungeeCraftReciever {
	private static int[] inventoryIDs = { 23,54,61,62,146,158 };
	public static void recieveCraft(DataInputStream in) {
		try {
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);

			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));

			String pilot = msgin.readUTF();
			System.out.println(pilot);
		TransferData craftData = Movecraft.getInstance().getSQLDatabase().readData(pilot);
		readCraftAndBuild(craftData, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void readCraftAndBuild(TransferData transferData, boolean affectPlayers) {
        try {
        	//receive slip data
        	boolean slip = transferData.isSlip();
        	//receive target location data
        	String targetworld = transferData.getDestinationLocation().getWorld().getName();
        	int Xcoord = (int) transferData.getDestinationLocation().getX();
        	int Ycoord = (int) transferData.getDestinationLocation().getY();
        	int Zcoord = (int) transferData.getDestinationLocation().getZ();
        	
        	//receive initial location data
        	String oldworld = transferData.getOldLocation().getWorld().getName();
        	int oldX = (int) transferData.getOldLocation().getX();
        	int oldY = (int) transferData.getOldLocation().getY();
        	int oldZ = (int) transferData.getOldLocation().getZ();
        	
        	//receive craft type name
        	String cName = transferData.getCraftType();
        	
        	//receive pilot name & UUID
        	String pName = transferData.getPilotName();
        	UUID pUUID = transferData.getPilotUUID();
        	
        	if(!affectPlayers) {
        		Player p = Movecraft.getPlayer(pUUID);
        		if(p != null) {
        			Location loc = p.getLocation();
        			Xcoord = loc.getBlockX();
        			Ycoord = loc.getBlockY();
        			Zcoord = loc.getBlockZ();
        		}
        	}
        	//receive the blocks list and their accompanying types
        	ArrayList<LocAndBlock> blockList = transferData.getBlockList();
        	for (int i = 0; i < blockList.size(); i++) {
        		LocAndBlock blockData = blockList.get(i);
        		int X = blockData.X;
        		int Y = blockData.Y;
        		int Z = blockData.Z;
        		int id = blockData.id;
        		int data = blockData.data;
        	}
        	ArrayList<String> bedspawnNames = transferData.getPlayersWithBedspawnsOnShip();
        	ArrayList<ServerjumpTeleport> playersOnShip = new ArrayList<ServerjumpTeleport>();
        	ArrayList<PlayerTransferData> playerDataList = transferData.getPlayerData();
        	
        	if(affectPlayers) {
	        	for(PlayerTransferData playerData : playerDataList) {
	        		UUID id = playerData.getPlayerID();
	        		Location location = playerData.getPlayerLocation();
	        		int x = (int) location.getX();
	        		int y = (int) location.getY();
	        		int z = (int) location.getZ();
	        		float yaw = location.getYaw();
	        		float pitch = location.getPitch();
	        		String worldName = location.getWorld().getName();
	        		Knapsack knapsack = playerData.getPlayerKnapsack();
	        		GameMode gamemode = Movecraft.getPlayer(id).getGameMode();
	        		playersOnShip.add(new ServerjumpTeleport(id, worldName, x, y, z, yaw, pitch, knapsack, gamemode));
	        	}
        	} else {
        		Player p = Movecraft.getPlayer(pUUID);
        		Location l = p.getLocation();
        		ServerjumpTeleport t = new ServerjumpTeleport(pUUID, l.getWorld().getName(), oldX, oldY, oldZ, l.getYaw(), l.getPitch(), null, p.getGameMode());
        		System.out.println("created fake teleport.");
        		playersOnShip.add(t);
        	}
        	BungeeCraftConstructor.calculateLocationAndBuild(slip, targetworld,Xcoord,Ycoord,Zcoord, oldworld, oldX, oldY, oldZ, cName, pName, pUUID, blockList, bedspawnNames, playersOnShip, !affectPlayers);
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
}
