package net.countercraft.movecraft.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.EntityUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.dibujaron.cardboardbox.Knapsack;

public class BungeeCraftSender {

	public static void sendCraft(Player p, String targetserver, String world, int X, int Y, int Z, final Craft c) throws IOException {
		/*try {
			c.playersRidingLock.acquire();*/
			for (int i = 0; i < c.playersRidingShip.size(); i++) {
				Player plr = Movecraft.getPlayer(c.playersRidingShip.get(i));
				if (plr == null || !plr.isOnline()) {
					if (p != null)
						p.sendMessage("One of your passengers is not currently online, repilot to proceed without him!");
					//c.playersRidingLock.release();
					return;
				}
			}
			//c.playersRidingLock.release();
			TransferData data = writeData(p, targetserver, X, Y, Z, c);
			CraftManager.getInstance().removeCraft(c, false);
			System.out.println("data: " + data);
			System.out.println("Movecraft singleton: " + Movecraft.getInstance());
			System.out.println("SQLDatabase singleton: " + Movecraft.getInstance().getSQLDatabase());
			//Movecraft.getInstance().getSQLDatabase().writeData(data);
			System.out.println("Saved craft.");
			sendCraftSpawnPacket(p, targetserver);
			//c.playersRidingLock.acquire();
			for (int i = 0; i < c.playersRidingShip.size(); i++) {
				UUID s = c.playersRidingShip.get(i);
				Player player = Movecraft.getPlayer(s);
				if (player != null) {
					BungeePlayerHandler.connectPlayer(player, targetserver);
				}
			}
			//c.playersRidingLock.release();
		/*} catch (Exception e) {
			e.printStackTrace();
		}*/
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Movecraft.getInstance(), new Runnable() {
			public void run() {
				removeCraftBlocks(c);
			}
		}, 20L);
	}
	
	private static void sendCraftSpawnPacket(Player p, String targetserver) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			out.writeUTF("Forward"); // So bungeecord knows to forward it
			out.writeUTF(targetserver);
			out.writeUTF("movecraftWarp"); // The channel name to check if this
											// your data

			ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
			DataOutputStream msgout = new DataOutputStream(msgbytes);
			msgout.writeUTF(p.getName()); // You can do anything you want with
											// msgout

			byte[] outmsg = msgbytes.toByteArray();
			out.writeShort(outmsg.length);
			out.write(outmsg);

			p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static TransferData writeData(Player p, String targetserver, double x, double y, double z, Craft c) {
		return writeData(p, targetserver, x, y, z, c, true);
	}
	public static TransferData writeData(Player p, String targetserver, double destinationX, double destinationY, double destinationZ, Craft c, boolean wipeInventories) {
		TransferData transferData = new TransferData();
		// write out if it's slipspace
		boolean slip = c.getW().getEnvironment() == Environment.THE_END;
		transferData.setSlip(slip);
		// send the destination location
		transferData.setDestinationLocation(targetserver, destinationX, destinationY, destinationZ);
		// send the existing (old) location
		Location lctn = p.getLocation();
		transferData.setOldLocation(lctn);
		// send the craft type
		transferData.setCraftType(c.getType().getCraftName());
		// send the pilot name and uuid
		transferData.setPilotName(p.getName());
		transferData.setPilotUUID(p.getUniqueId());
		// send the craft's blocks
		ArrayList<LocAndBlock> locAndBlockList = new ArrayList<LocAndBlock>();
		for (MovecraftLocation l : c.getBlockList()) {
			LocAndBlock locAndBlock;
			int x = l.getX();
			int y = l.getY();
			int z = l.getZ();
			Location loc = new Location(c.getW(), x, y, z);
			int id = loc.getBlock().getTypeId();
			int data = (int) loc.getBlock().getData();
			// If block has inventory
			if (loc.getBlock().getState() instanceof InventoryHolder) {
				Inventory i = ((InventoryHolder) loc.getBlock().getState()).getInventory();
				if(wipeInventories) {
					i.clear();
				}
				locAndBlock = new LocAndBlock(x, y, z, id, data, i);
			}
			//If it's a sign
			else if (id == 63 || id == 68) {
				Sign s = (Sign) loc.getBlock().getState();
				locAndBlock = new LocAndBlock(x, y, z, id, data, s.getLine(0), s.getLine(1), s.getLine(2), s.getLine(3));
			}
			//If neither
			else {
				locAndBlock = new LocAndBlock(x, y, z, id, data);
			}
			locAndBlockList.add(locAndBlock);
		}
		transferData.setBlockList(locAndBlockList);
		transferData.setPlayersWithBedspawnsOnShip(c.playersWithBedspawnsOnShip);
		transferData.setPlayersRidingShip(c.playersRidingShip);
		ArrayList<PlayerTransferData> playerTransferData = new ArrayList<PlayerTransferData>();
			for (int i = 0; i < c.playersRidingShip.size(); i++) {
				PlayerTransferData playerData = new PlayerTransferData();
				Player plr = Movecraft.getPlayer(c.playersRidingShip.get(i));
				if (plr != null) {
					Location l = plr.getLocation();
					playerData.setPlayerLocation(l);
					Knapsack k = new Knapsack(plr);
					playerData.setPlayerKnapsack(k);
					GameMode mode = plr.getGameMode();
					playerData.setPlayerGameMode(mode);
					if(wipeInventories){
						BungeePlayerHandler.wipePlayerInventory(plr);
					}
				}
				playerTransferData.add(playerData);
			}
			transferData.setPlayerData(playerTransferData);
		return transferData;
	}

	private static void removeCraftBlocks(final Craft c) {
		//World w = c.getW();
		//byte zero = (byte) 0;
		MovecraftLocation[] blocks = c.getBlockList();
		final MapUpdateCommand[] updates = new MapUpdateCommand[blocks.length];
		for (int i = 0; i < blocks.length; i++) {
			//w.getBlockAt(l.getX(), l.getY(), l.getZ()).setTypeIdAndData(0, zero, false);
			//updateSet.add(new MapUpdateCommand(l, 0, c, false));
			updates[i] = new MapUpdateCommand(blocks[i], 0, c, false);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				MapUpdateManager.getInstance().addWorldUpdate(c.getW(), updates, new EntityUpdateCommand[0]);
			}
		});
	}

	public static void debug(String s) {
		Movecraft.getInstance().getServer().broadcastMessage(s);
	}
}
