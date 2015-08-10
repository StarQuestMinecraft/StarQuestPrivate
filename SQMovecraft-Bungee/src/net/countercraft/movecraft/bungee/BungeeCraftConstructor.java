package net.countercraft.movecraft.bungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.BorderUtils;
import net.countercraft.movecraft.utils.LocationUtils;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.NewShipClassConverter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

/**
 * @author Dibujaron
 * BUNGEECORD: TURN BACK NOW FAINT OF HEART
 * This class takes a craft's data and attempts to use that data to construct the craft within the world after a serverjump.
 * @see BungeeCraftReciever
 */
public class BungeeCraftConstructor {
	
	//calculate for destination obstructions and then build the craft
	public static void calcluateLocationAndBuild(boolean slip, String world, int tX, int tY, int tZ, String oldworld, int oldX, int oldY, int oldZ, final String type, final String pilot, final UUID pilotUUID, LocAndBlock[] bll, ArrayList<String> bedSpawnPlayersOnShip, final ArrayList<ServerjumpTeleport> playersOnShip){
		calculateLocationAndBuild(slip, world, tX, tY, tZ, oldworld, oldX, oldY, oldZ, type, pilot, pilotUUID, bll, bedSpawnPlayersOnShip, playersOnShip, false);
	}
	public static void calculateLocationAndBuild(boolean slip, String world, int tX, int tY, int tZ, String oldworld, int oldX, int oldY, int oldZ, final String type, final String pilot, final UUID pilotUUID, LocAndBlock[] bll, ArrayList<String> bedSpawnPlayersOnShip, final ArrayList<ServerjumpTeleport> playersOnShip, boolean isFake){
		System.out.println("tX: " + tX);
		System.out.println("tY: " + tY);
		System.out.println("tZ: " + tZ);
		World w;
		if(world.equals("transfer")){
			w = Bukkit.getPlayer(pilotUUID).getWorld();
		} else {
			w = Movecraft.getInstance().getServer().getWorld(world);
		}
		Location targetLoc = new Location(w, tX, tY, tZ);
		Location oldLoc = new Location(w, oldX, oldY, oldZ);
		int dX, dY, dZ;
		dX = getdX(oldLoc, targetLoc);
		dY = getdY(oldLoc, targetLoc);
		dZ = getdZ(oldLoc, targetLoc);
		
		boolean isSpaceWorld = LocationUtils.spaceCheck(world);
		System.out.println("This serverjump came from: " + oldworld);
		
		//if it's a space world, the obstruction check may need to take place in the opposite direction
		//needs to take place in an "outward" searching direction
		
		double angle = 0;
		if(isSpaceWorld){
			if(LocationUtils.spaceCheck(oldworld) || slip){
				angle = 0;
			}else{
				System.out.println(oldworld);
				angle = LocationUtils.getAngleFromGivenPointTo(LocationUtils.locationOfPlanet(oldworld), targetLoc);
			}
		} else {
			angle = LocationUtils.getAngleFromOriginTo(targetLoc);
		}
		double xVal = Math.cos(angle) * 10;
		double zVal = Math.sin(angle) * 10;
		int count = 0;
		boolean reversed = false;
		while(count < 20 && destinationObstructed(bll, w, dX, dY, dZ)){
			count++;
			if(!reversed){
				dX += xVal;
				tX += xVal;
				dZ += zVal;
				tZ += zVal;
			} else {
				dX -= xVal;
				tX -= xVal;
				dZ -= zVal;
				tZ -= zVal;
			}
			
			if(!isInsideBorder(tX, tY, tZ) && !reversed){
				reversed = true;
				count = 0;
			}
		}
		
		System.out.println("tX: " + tX);
		System.out.println("tY: " + tY);
		System.out.println("tZ: " + tZ);
		
		//create a list of string playernames on ship from player teleports
		ArrayList<UUID> playersOnShipString = new ArrayList<UUID>();
		
		//modify the players' locations for collision detection and also add them to the string list
		for(ServerjumpTeleport t : playersOnShip){
			System.out.println("modifying teleport: " + dX + "," + dY + "," + dZ);
			t.x = t.x + dX;
			t.y = t.y + dY;
			t.z = t.z + dZ;
			playersOnShipString.add(t.uuid);
		}
		buildCraft(slip, w, tX, tY, tZ, dX, dY, dZ, type, pilot, pilotUUID, bll, bedSpawnPlayersOnShip, playersOnShipString, isFake);
		warpPlayers(playersOnShip);
	}
	
	private static boolean isInsideBorder(int tX, int tY, int tZ){
		return BorderUtils.isWithinBorder(tX, tZ);
	}
	
	private static void warpPlayers(ArrayList<ServerjumpTeleport> playersOnShip) {
		for(final ServerjumpTeleport t : playersOnShip){
			Player p = Movecraft.getPlayer(t.uuid);
			if (p != null && p.isOnline()) {
				System.out.println("executing teleport.");
				t.execute();
			} else {
				BungeePlayerHandler.teleportQueue.add(t);
			}
		}
	}
	public static void buildCraft(boolean slip, final World w, int X, int Y, int Z, int dX, int dY, int dZ, final String type, final String pilot, final UUID pilotUUID, LocAndBlock[] bll, ArrayList<String> names, ArrayList<UUID> namesOnShip, boolean isFake){
		int[] fragileBlocks = MapUpdateManager.getInstance().fragileBlocks;
		ArrayList<LocAndBlock> fragiles = new ArrayList<LocAndBlock>();
		
		//update bedspawns
		/*for(String s : names){
			Bedspawn b = Bedspawn.getBedspawn(s);
			b.server = Bukkit.getServerName();
			b.x = b.x + dX;
			b.y = b.y + dY;
			b.z = b.z + dZ;
			b.world = w.getName();
			Bedspawn.saveBedspawn(b);
		}*/
		//place blocks
		for(LocAndBlock b : bll){
			//do fragiles later
			if(Arrays.binarySearch(fragileBlocks,b.id)>=0){
				fragiles.add(b);
			} else {
				//do dX and dY and dZ stuff
				Location l = new Location(w, b.X + dX, b.Y + dY, b.Z + dZ);
				l.getBlock().setTypeIdAndData(b.id, (byte) b.data, false);
				restoreInv(l.getBlock(), b, pilot, isFake, bll.length);
			}
		}
		for(LocAndBlock b : fragiles){
			Location l = new Location(w, b.X + dX, b.Y + dY, b.Z + dZ);
			l.getBlock().setTypeIdAndData(b.id, (byte) b.data, false);
			restoreInv(l.getBlock(), b, pilot, isFake, bll.length);
		}
		//final int XDIFF = xDiff;
		Craft c;
		if(!isFake){
			c = new Craft(InteractListener.getCraftTypeFromString( type ), w);
		} else {
			c = new Craft(InteractListener.getCraftTypeFromString(NewShipClassConverter.convert(type, bll.length)), w);
		}
		c.originalPilotLoc = new Location(w, X, Y, Z);
		c.warpCoordsX = X;
		c.warpCoordsZ = Z;
		/*try{
			c.playersRidingLock.acquire();*/
			for(UUID s : namesOnShip){
				if(!c.playersRidingShip.contains(s))
				c.playersRidingShip.add(s);
			}
			/*c.playersRidingLock.release();
		} catch (Exception e){
			e.printStackTrace();
		}*/
		attemptPilot(0, c, pilot, pilotUUID, type, w);
		delayStarshipMoving(c);
	}
	
	private static void restoreInv(Block b, LocAndBlock lb, String pilot, boolean isFake, int size){
		if(b.getTypeId() == 63 || b.getTypeId() == 68){
			
			Sign s = (Sign) b.getState();
			
			if(isFake && isCraftSign(lb.line1)){
					createLegacySign(lb.line1, s, pilot, size);
			} else {
				s.setLine(0, lb.line1);
				s.setLine(1, lb.line2);
				s.setLine(2, lb.line3);
				s.setLine(3, lb.line4);
				s.update();
			}
		}
		if(lb.i == null) return;
		if(isFake) return;
		if(!(b.getState() instanceof InventoryHolder)) return;
		InventoryHolder i = (InventoryHolder) b.getState();
		i.getInventory().setContents(lb.i.getContents());
	}
	public static boolean destinationObstructed(LocAndBlock[] bll, World targ, int dX, int dY, int dZ){
		for (int i = 0; i < bll.length; i++) {
			Location newLoc = new Location (targ, bll[i].X + dX, bll[i].Y + dY, bll[i].Z + dZ);
			Block lBlock = newLoc.getBlock();
			if(lBlock == null) return true;
			for(Block b : BlockUtils.getEdges(lBlock, true, true)){
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
					c.detect(p,  MathUtils.bukkit2MovecraftLoc(p.getLocation()));
					
				} else {
					//player is not logged in yet, but has a playerteleport set to execute when they do log in (hopefully)
					BungeePlayerHandler.pilotQueue.put(uid, c);
				}
			}


		}, 5L);
	}
	
	private static void createLegacySign(String firstline, Sign s, String playername, int n) {
		s.setLine(0, NewShipClassConverter.convert(firstline, n));
		if (playername.length() > 15) {
			s.setLine(1, playername.substring(0, 15));
		} else {
			s.setLine(1, playername);
		}
		s.setLine(2, ChatColor.RED + "LEGACY");
		s.setLine(3, ChatColor.RED + "SHIP");
		s.update();
	}
	
	private static boolean isCraftSign(String line){
		if(InteractListener.getCraftTypeFromString(line) != null) return true;
		if(line.equalsIgnoreCase("Blockade Runner")) return true;
		if(line.equalsIgnoreCase("Starfighter")) return true;
		if(line.equalsIgnoreCase("Carrier")) return true;
		if(line.equalsIgnoreCase("Ironclad")) return true;
		return false;
	}
}
