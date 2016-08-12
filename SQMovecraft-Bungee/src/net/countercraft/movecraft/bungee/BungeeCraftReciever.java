package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * @author Dibujaron
 * BUNGEECORD: TURN BACK NOW FAINT OF HEART
 * This class recieves data from BungeeListener, loads files, and processes the data stream. It then calls BungeeCraftConstructor to build the ship in the world.
 */
public class BungeeCraftReciever {
	private static int[] inventoryIDs = {23,54,61,62,146,158};
	public static void recieveCraft(DataInputStream in){
		try{
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);

			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));

			String pilot = msgin.readUTF();
			System.out.println(pilot);
		byte[] craftData = BungeeFileHandler.readCraftBytes(pilot);
		readCraftAndBuild(craftData, true);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void readCraftAndBuild(byte[] databytes, boolean affectPlayers){
        try {
        	DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(databytes));
        	//recieve slip data
        	boolean slip = msgin.readBoolean();
        	
        	//recieve target location data
        	String targetworld = msgin.readUTF();
        	int Xcoord = msgin.readInt();
        	int Ycoord = msgin.readInt();
        	int Zcoord = msgin.readInt();
        	
        	//recieve initial location data
        	String oldworld = msgin.readUTF();
        	int oldX = msgin.readInt();
        	int oldY = msgin.readInt();
        	int oldZ = msgin.readInt();
        	
        	//recieve craft type name
        	String cName = msgin.readUTF();
        	
        	//recieve pilot name & UUID
        	String pName = msgin.readUTF();
        	UUID pUUID = UUID.fromString(msgin.readUTF());
        	
        	if(!affectPlayers){
        		Player p = Movecraft.getPlayer(pUUID);
        		if(p != null){
        			Location loc = p.getLocation();
        			Xcoord = loc.getBlockX();
        			Ycoord = loc.getBlockY();
        			Zcoord = loc.getBlockZ();
        		}
        	}

        	//recieve block list length from craft
        	int length = msgin.readInt();
        	
        	//recieve the blocks list and their accompanying types
        	LocAndBlock[] blockloclist = new LocAndBlock[length];
        	for (int i = 0; i < length; i++){
        		int X = msgin.readInt();
        		int Y = msgin.readInt();
        		int Z = msgin.readInt();
        		int id = msgin.readInt();
        		int data = msgin.readInt();
        		boolean inv = msgin.readBoolean();
        		//recieve inventory blocks
        		if (inv){
        			InventoryType type = InventoryType.valueOf(msgin.readUTF());
        			Inventory contents = InventoryUtils.readInventory(msgin, type);
        			LocAndBlock b = new LocAndBlock(X, Y, Z, id, data, contents);
        			blockloclist[i] = b;	        		
    			} else if(id == 63 || id == 68){
    				String line1 = msgin.readUTF();
    				String line2 = msgin.readUTF();
    				String line3 = msgin.readUTF();
    				String line4 = msgin.readUTF();
    				LocAndBlock b = new LocAndBlock(X,Y,Z,id,data,line1,line2,line3,line4);
    				blockloclist[i] = b;
    			}else {
	        		LocAndBlock b = new LocAndBlock(X, Y, Z, id, data);
	        		blockloclist[i] = b;
        		}
        	}
        	ArrayList<String> bedspawnNames = new ArrayList<String>();
        	ArrayList<ServerjumpTeleport> playersOnShip = new ArrayList<ServerjumpTeleport>();
        	if(affectPlayers){
	        	int bedspawnsLength = msgin.readInt();
	        	for(int i = 0; i < bedspawnsLength; i++){
	        		String playername = msgin.readUTF();
	        		bedspawnNames.add(playername);
	        	}
	        	int playersLength = msgin.readInt();
	        	for(int i = 0; i < playersLength; i++){
	        		ServerjumpTeleport t = BungeePlayerHandler.recievePlayerTeleport(msgin);
	        		if(t != null){
	        			playersOnShip.add(t);
	        		}
	        	}
        	} else {
        		Player p = Movecraft.getPlayer(pUUID);
        		Location l = p.getLocation();
        		ServerjumpTeleport t = new ServerjumpTeleport(pUUID, l.getWorld().getName(), oldX, oldY, oldZ, l.getYaw(), l.getPitch(), null, p.getGameMode());
        		System.out.println("created fake teleport.");
        		playersOnShip.add(t);
        	}
        	BungeeCraftConstructor.calculateLocationAndBuild(slip, targetworld,Xcoord,Ycoord,Zcoord, oldworld, oldX, oldY, oldZ, cName, pName, pUUID, blockloclist, bedspawnNames, playersOnShip, !affectPlayers);
        }
        catch(IOException e){
        	e.printStackTrace();
        }
	}
}
