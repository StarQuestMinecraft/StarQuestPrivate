package net.countercraft.movecraft.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BungeeCraftSender {
	private static int[] inventoryIDs = {23,54,61,62,146,158};
	
	public static void sendCraft(Player p, String targetserver, String world, int X, int Y, int Z, Craft c) throws IOException{
		byte[] craftData = serialize(p, targetserver, world, X, Y, Z, c);
		CraftManager.getInstance().removeCraft(c);
		BungeeFileHandler.saveCraftBytes(craftData, p.getName());
		System.out.println("Saved craft.");
		sendCraftSpawnPacket(p.getName(), targetserver);
		for(Player plr : c.playersRiding){
			BungeePlayerHandler.sendPlayer(plr, targetserver);
		}
		removeCraftBlocks(c);
	}
	private static void sendCraftSpawnPacket(String pName, String targetserver){
		try{
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			out.writeUTF("Forward"); // So bungeecord knows to forward it
			out.writeUTF(targetserver);
			out.writeUTF("movecraftWarp"); // The channel name to check if this your data

			ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
			DataOutputStream msgout = new DataOutputStream(msgbytes);
			msgout.writeUTF(pName); // You can do anything you want with msgout
			
			byte[] outmsg = msgbytes.toByteArray();
			out.writeShort(outmsg.length);
			out.write(outmsg);
			
			Player p = Bukkit.getOnlinePlayers()[0];
			p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static byte[] serialize(Player p, String targetserver, String world, int X, int Y, int Z, Craft c) throws IOException{
		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		
		//send the location
		msgout.writeUTF(world);
		msgout.writeInt(X);
		msgout.writeInt(Y);
		msgout.writeInt(Z);
		
		//send the existing location
		Location lctn = p.getLocation();
		msgout.writeInt(lctn.getBlockX());
		msgout.writeInt(lctn.getBlockY());
		msgout.writeInt(lctn.getBlockZ());
		
		//send the craft type
		msgout.writeUTF(c.getType().getCraftName());
		
		//send the pilot name
		msgout.writeUTF(p.getName());
		
		//send the length of the craft's block list array
		msgout.writeInt(c.getBlockList().length);
		
		//send the craft's block list array
		for (MovecraftLocation l : c.getBlockList()){
			msgout.writeInt(l.getX());
			msgout.writeInt(l.getY());
			msgout.writeInt(l.getZ());
			Location loc = new Location(c.getW(), l.getX(), l.getY(), l.getZ());
			int id = loc.getBlock().getTypeId();
			int data = (int) loc.getBlock().getData();
			msgout.writeInt(id);
			msgout.writeInt(data);
			//send inventory
			if (Arrays.binarySearch(inventoryIDs, id) >= 0){
				Inventory i = ((InventoryHolder) loc.getBlock().getState()).getInventory();
				/*for (int index = 0; index < i.getSize(); index++){
					System.out.println("Scanning inventory slot.");
					ItemStack s = i.getItem(index);
					if(s != null){
						System.out.println("Sending itemstack");
						msgout.writeUTF("continue");
						System.out.println(s.getTypeId() + "," + s.getDurability() + "," + s.getAmount());
						//id data amount index
						msgout.writeInt(s.getTypeId());
						msgout.writeInt(s.getDurability());
						msgout.writeInt(s.getAmount());
						msgout.writeInt(index);
					}
				}*/
				InventoryUtils.writeInventory(msgout, i);
				i.clear();
			}
			if(id == 63 || id == 68){
				Sign s = (Sign) loc.getBlock().getState();
				msgout.writeUTF(s.getLine(0) + "");
				msgout.writeUTF(s.getLine(1) + "");
				msgout.writeUTF(s.getLine(2) + "");
				msgout.writeUTF(s.getLine(3) + "");
			}
		}
		msgout.writeInt(c.playersWithBedSpawnsOnShip.size());
		for(String s : c.playersWithBedSpawnsOnShip){
			msgout.writeUTF(s);
		}
		msgout.writeInt(c.playersRiding.size());
		for(Player plr : c.playersRiding){
			Location l = plr.getLocation();
			BungeePlayerHandler.writePlayerData(msgout, plr, targetserver, world, l.getBlockX(), l.getBlockY(), l.getBlockZ());
		}
		
		return msgbytes.toByteArray();
	}
	private static void removeCraftBlocks(Craft c){
		World w = c.getW();
		byte zero = (byte) 0;
		for(MovecraftLocation l : c.getBlockList()){
			w.getBlockAt(l.getX(), l.getY(), l.getZ()).setTypeIdAndData(0, zero, false);
		}
	}
	
	public static void debug(String s){
		Movecraft.getInstance().getServer().broadcastMessage(s);
	}
}