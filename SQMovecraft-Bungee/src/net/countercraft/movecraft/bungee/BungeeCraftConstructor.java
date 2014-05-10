package net.countercraft.movecraft.bungee;

import java.util.ArrayList;
import java.util.Arrays;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import com.mini.Arguments;

public class BungeeCraftConstructor {
	
	//calculate for destination obstructions and then build the craft
	public static void calculateLocationAndBuild(String world, int tX, int tY, int tZ, int oldX, int oldY, int oldZ, final String type, final String pilot, LocAndBlock[] bll, ArrayList<String> bedSpawnPlayersOnShip, final ArrayList<PlayerTeleport> playersOnShip){
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
		}
		
		//modify the players' locations too
		for(PlayerTeleport t : playersOnShip){
			t.x = t.x + dX;
			t.y = t.y + dY;
			t.z = t.z + dZ;
		}
		buildCraft(w, tX, tY, tZ, dX, dY, dZ, type, pilot, bll, bedSpawnPlayersOnShip);
		warpPlayers(playersOnShip);
	}
	
	private static void warpPlayers(ArrayList<PlayerTeleport> playersOnShip) {
		for(final PlayerTeleport t : playersOnShip){
			if (Bukkit.getServer().getPlayer(t.playername) != null) {
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
	public static void buildCraft(final World w, int X, int Y, int Z, int dX, int dY, int dZ, final String type, final String pilot, LocAndBlock[] bll, ArrayList<String> names){
		int[] fragileBlocks = MapUpdateManager.getInstance().fragileBlocks;
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
		attemptPilot(0, c, pilot, type, w);
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
			int testID = newLoc.getWorld().getBlockTypeIdAt(newLoc);
			if (testID != 0) {
				return true;
			}
		}
		return false;
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
	
	private static void attemptPilot(final int count, final Craft c, final String pilot, final String type, final World w){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				Player p = Bukkit.getServer().getPlayer(pilot);
				if(count >= 20 ){
					return;
				} else if(p == null){
					int count2 = count + 1;
					attemptPilot(count2, c, pilot, type, w);
				} else {
					c.detect(pilot, MathUtils.bukkit2MovecraftLoc(p.getLocation()));
				}
			}
		}, 5L);
	}
}
