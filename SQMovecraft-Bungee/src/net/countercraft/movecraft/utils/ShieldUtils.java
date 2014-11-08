package net.countercraft.movecraft.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShieldUtils {
	
	static ArrayList<PendingActivation> pendingActivations = new ArrayList<PendingActivation>();
	
	private static WorldGuardPlugin wg = getWorldGuard();
	private static long SHIELD_DELAY_TICKS = 5 * 60 * 20;
	
	private static String SIGN_LINE_0 = ChatColor.BLUE + "Shield Control";
	private static String ENABLED = ChatColor.GREEN + "{Protected}";
	private static String WARMING = ChatColor.YELLOW + "{Warming Up}";
	private static String DISABLED = ChatColor.RED + "{Inactive}";
	
	public static void deployShield(Craft ship, Block sign){
		ship.pilot.sendMessage("Your ship's shield has been activated.");
		ship.pilot.sendMessage("Shield warming up... estimated time: 5 minutes");
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for(MovecraftLocation l : ship.getBlockList()){
			if(l.getY() < minY) minY = l.getY();
			if(l.getY() > maxY) maxY = l.getY();
			if(l.getX() > maxX) maxX = l.getX();
			if(l.getZ() > maxZ) minY = l.getY();
		}
		BlockVector min = new BlockVector(ship.getMinX(), minY, ship.getMinZ());
		BlockVector max = new BlockVector(maxX, maxY, maxZ);
		final PendingActivation a = new PendingActivation(ship.getW(), min, max, createRName(ship), ship.pilot, sign);
		pendingActivations.add(a);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				if(pendingActivations.contains(a)){
					a.activate();
				}
			}
		}, SHIELD_DELAY_TICKS);
	}
	
	public static void removeShield(Craft ship){
		String rname = createRName(ship);
		ProtectedRegion r = wg.getRegionManager(ship.getW()).getRegionExact(rname);
		if(r != null){
			wg.getRegionManager(ship.getW()).removeRegion(rname);
		}
	}
	
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	private static String createRName(Craft ship){
		return (ship.pilot.getName() + "-shielded-ship");
	}
	
	private static class PendingActivation{
		String name;
		World w;
		BlockVector min;
		BlockVector max;
		Player pilot;
		Block sign;
		
		private PendingActivation(World w, BlockVector min, BlockVector max, String name, Player pilot, Block sign){
			this.name = name;
			this.w = w;
			this.min = min;
			this.max = max;
			this.pilot = pilot;
			this.sign = sign;
		}
		
		private void activate(){
			ProtectedRegion reg = new ProtectedCuboidRegion(name, min, max);
			RegionManager rm = wg.getRegionManager(w);
			ProtectedRegion remove = rm.getRegionExact(name);
			if(remove != null){
				rm.removeRegion(name);
				if(pilot.isOnline()){
					pilot.sendMessage("An existing starship shield was removed; you cannot have two shields in the same world.");
				}
			}
			if(sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST){
				Sign s = (Sign) sign.getState();
				if(isShieldSign(s)){
					s.setLine(1, ENABLED);
					s.update();
				}
			}
			wg.getRegionManager(w).addRegion(reg);
			if(pilot.isOnline()){
				pilot.sendMessage("Your starship's shield is fully warmed up and is providing full protection.");
			}
		}
	}

	public static boolean isShieldSign(Sign s) {
		return s.getLine(0).equals(SIGN_LINE_0);
	}

	public static void disableShield(Sign s, Craft c) {
		s.setLine(1, DISABLED);
		s.update();
		removeShield(c);
	}
	
	public static void setupShieldSign(Sign s){
		s.setLine(0, SIGN_LINE_0);
		s.setLine(1, DISABLED);
		s.update();
	}
	
	public static void enableShield(Sign s, Craft c) {
		s.setLine(1, WARMING);
		s.update();
		deployShield(c, s.getBlock());
	}

	public static void enableShield(Craft c, ArrayList<MovecraftLocation> signLocations) {
		for(MovecraftLocation l : signLocations){
			Sign s = (Sign) c.getW().getBlockAt(l.getX(), l.getY(), l.getZ()).getState();
			if(!isShieldSign(s)) continue;
			
		}
	}
}
