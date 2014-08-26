package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
        		
        		//recieve inventory blocks
        		if (Arrays.binarySearch(inventoryIDs, id) >= 0){
        			Inventory contents;
        			/*boolean continuing = true;
        			while (continuing){
        				String continuingstring = msgin.readUTF();
        				if (continuingstring.equals("continue")){
        					System.out.println("continuing.");
        					int itemid = msgin.readInt();
        					int itemdata = msgin.readInt();
        					int amount = msgin.readInt();
        					int slot = msgin.readInt();
        					System.out.println("ItemStack: " + itemid + "," + itemdata + "," + amount + "," + slot);
        					ItemStack stack = new ItemStack(itemid, amount, (short) itemdata);
        					contents.add(new ItemStackHolder(stack,slot));
        				}
        				else if (continuingstring.equals("stop")){
        					System.out.println("stopping.");
        					continuing = false;
        				}
        			}*/
        			contents = InventoryUtils.readInventory(msgin);
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
        	ArrayList<PlayerTeleport> playersOnShip = new ArrayList<PlayerTeleport>();
        	if(affectPlayers){
	        	int bedspawnsLength = msgin.readInt();
	        	for(int i = 0; i < bedspawnsLength; i++){
	        		String playername = msgin.readUTF();
	        		bedspawnNames.add(playername);
	        	}
	        	int playersLength = msgin.readInt();
	        	for(int i = 0; i < playersLength; i++){
	        		PlayerTeleport t = BungeePlayerHandler.recievePlayerTeleport(msgin);
	        		if(t != null){
	        			playersOnShip.add(t);
	        		}
	        	}
        	}
        	BungeeCraftConstructor.calculateLocationAndBuild(targetworld,Xcoord,Ycoord,Zcoord, oldworld, oldX, oldY, oldZ, cName, pName, pUUID, blockloclist, bedspawnNames, playersOnShip);
        }
        catch(IOException e){
        	e.printStackTrace();
        }
	}
}
