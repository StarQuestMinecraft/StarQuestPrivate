package net.countercraft.movecraft.bungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.map.MapUpdateManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class BungeeCraftConstructor {
	
	//calculate for destination obstructions and then build the craft
	public static void calculateLocationAndBuild(String world, int tX, int tY, int tZ, int oldX, int oldY, int oldZ, final String type, final String pilot, final UUID pilotUUID, LocAndBlock[] bll, ArrayList<String> bedSpawnPlayersOnShip, final ArrayList<PlayerTeleport> playersOnShip){
		World w = Movecraft.getInstance().getServer().getWorld(world);
		Location targetLoc = new Location(w, tX, tY, tZ);
		Location oldLoc = new Location(w, oldX, oldY, oldZ);
		int dX = getdX(oldLoc, targetLoc);
		int dY = getdY(oldLoc, targetLoc);
		int dZ = getdZ(oldLoc, targetLoc);
		int count = 0;
		while(count < 100 && destinationObstructed(bll, w, dX, dY, dZ)){
			count++;
			dX += 10;
			tX += 10;
		}
		
		//create a list of string playernames on ship from player teleports
		ArrayList<UUID> playersOnShipString = new ArrayList<UUID>();
		
		//modify the players' locations for collision detection and also add them to the string list
		for(PlayerTeleport t : playersOnShip){
			t.x = t.x + dX;
			t.y = t.y + dY;
			t.z = t.z + dZ;
			playersOnShipString.add(t.uuid);
		}
		buildCraft(w, tX, tY, tZ, dX, dY, dZ, type, pilot, pilotUUID, bll, bedSpawnPlayersOnShip, playersOnShipString);
		warpPlayers(playersOnShip);
	}
	
	private static void warpPlayers(ArrayList<PlayerTeleport> playersOnShip) {
		for(final PlayerTeleport t : playersOnShip){
			if (Movecraft.playerIndex.get(t.uuid) != null) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						t.execute();
					}
				}, 1L);
			} else {
				BungeePlayerHandler.teleportQueue.add(t);
			}
		}
	}
	public static void buildCraft(final World w, int X, int Y, int Z, int dX, int dY, int dZ, final String type, final String pilot, final UUID pilotUUID, LocAndBlock[] bll, ArrayList<String> names, ArrayList<UUID> namesOnShip){
		int[] fragileBlocks = MapUpdateManager.fragileBlocks;
		ArrayList<LocAndBlock> fragiles = new ArrayList<LocAndBlock>();
		
		//update bedspawns
		for(String s : names){
			Bedspawn b = Bedspawn.getBedspawn(s);
			b.server = Bukkit.getServerName();
			b.x = b.x + dX;
			b.y = b.y + dY;
			b.z = b.z + dZ;
			b.world = w.getName();
			Bedspawn.saveBedspawn(b);
		}
		//place blocks
		for(LocAndBlock b : bll){
			//do fragiles later
			if(Arrays.binarySearch(fragileBlocks,b.id)>=0){
				fragiles.add(b);
			} else {
				//do dX and dY and dZ stuff
				Location l = new Location(w, b.X + dX, b.Y + dY, b.Z + dZ);
				l.getBlock().setTypeIdAndData(b.id, (byte) b.data, false);
				restoreInv(l.getBlock(), b);
			}
		}
		for(LocAndBlock b : fragiles){
			Location l = new Location(w, b.X + dX, b.Y + dY, b.Z + dZ);
			l.getBlock().setTypeIdAndData(b.id, (byte) b.data, false);
			restoreInv(l.getBlock(), b);
		}
		//final int XDIFF = xDiff;
		Craft c = new Craft(InteractListener.getCraftTypeFromString( type ), w);
		c.setOriginalPilotLoc(new Location(w, X, Y, Z));
		try{
			c.playersRidingLock.acquire();
			for(UUID s : namesOnShip){
				if(!c.playersRidingShip.contains(s))
				c.playersRidingShip.add(s);
			}
			c.playersRidingLock.release();
		} catch (Exception e){
			e.printStackTrace();
		}
		attemptPilot(0, c, pilot, pilotUUID, type, w);
		delayStarshipMoving(c);
	}
	
	private static void restoreInv(Block b, LocAndBlock lb){
		if(b.getTypeId() == 63 || b.getTypeId() == 68){
			
			Sign s = (Sign) b.getState();
			s.setLine(0, lb.line1);
			s.setLine(1, lb.line2);
			s.setLine(2, lb.line3);
			s.setLine(3, lb.line4);
			s.update();
		}
		if(lb.i == null) return;
		if(!(b.getState() instanceof InventoryHolder)) return;
		InventoryHolder i = (InventoryHolder) b.getState();
		i.getInventory().setContents(lb.i.getContents());
	}
	public static boolean destinationObstructed(LocAndBlock[] bll, World targ, int dX, int dY, int dZ){
		for (int i = 0; i < bll.length; i++) {
			Location newLoc = new Location (targ, bll[i].X + dX, bll[i].Y + dY, bll[i].Z + dZ);
			for(Block b : getEdges(newLoc)){
				int testID = b.getTypeId();
				if (testID != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void delayStarshipMoving(final Craft c){
		c.setProcessingTeleport(true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				c.setProcessingTeleport(false);
			}
		}, 60L);
	}
	public static Block[] getEdges(Location l){
		Block b = l.getBlock();
		Block[] retval = new Block[19];
		//block itself
		retval[0] = b;
		
		//faces
		retval[1] = b.getRelative(0, 1, 0);
		retval[2] = b.getRelative(0, -1, 0);
		retval[3] = b.getRelative(1, 0, 0);
		retval[4] = b.getRelative(-1, 0, 0);
		retval[5] = b.getRelative(0, 0, 1);
		retval[6] = b.getRelative(0, 0, -1);
		
		//edges on the upper side
		retval[7] = b.getRelative(1, 1, 0);
		retval[8] = b.getRelative(-1, 1, 0);
		retval[9] = b.getRelative(0, 1, 1);
		retval[10] = b.getRelative(0, 1, -1);
		
		//edges on the lower side
		retval[11] = b.getRelative(1, -1, 0);
		retval[12] = b.getRelative(-1, -1, 0);
		retval[13] = b.getRelative(0, -1, 1);
		retval[14] = b.getRelative(0, -1, -1);
		
		//edges on the same plane
		retval[15] = b.getRelative(1, 0, 1);
		retval[16] = b.getRelative(-1, 0, 1);
		retval[17] = b.getRelative(1, 0, -1);
		retval[18] = b.getRelative(-1, 0, -1);
		
		return retval;
	}
	
	//helping methods for calculating differences in X, Y, and Z
	private static int getdX(Location from, Location to){
		int fromX = from.getBlockX();
		int toX = to.getBlockX();
		return (toX - fromX);
		
	}
	
	//dY calculator
	private static int getdY(Location from, Location to){
		int fromY = from.getBlockY();
		int toY = to.getBlockY();
		return (toY - fromY);
	}
	
	//dZ calculator
	private static int getdZ(Location from, Location to){
		int fromZ = from.getBlockZ();
		int toZ = to.getBlockZ();
		return (toZ - fromZ);
	}
	
	public static void debug(String s){
		Movecraft.getInstance().getServer().broadcastMessage(s);
	}
	
	private static void attemptPilot(final int count, final Craft c, final String pilot, final UUID uid, final String type, final World w){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				Player p = Bukkit.getServer().getPlayer(pilot);
				if(p != null){
					c.detect(pilot,  MathUtils.bukkit2MovecraftLoc(p.getLocation()));
				} else {
					//player is not logged in yet, but has a playerteleport set to execute when they do log in (hopefully)
					BungeePlayerHandler.pilotQueue.put(uid, c);
				}
			}
		}, 5L);
	}
}
