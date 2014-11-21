package net.countercraft.movecraft.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.listener.InteractListener;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShieldUtils {
	
	static ArrayList<PendingActivation> pendingActivations = new ArrayList<PendingActivation>();
	
	private static WorldGuardPlugin wg = getWorldGuard();
	private static long SHIELD_DELAY_TICKS = 5 * 60 * 20;
	
	private static String SIGN_LINE_0 = ChatColor.BLUE + "Ship Shield";
	public static String ENABLED = ChatColor.GREEN + "{Protected}";
	public static String WARMING = ChatColor.YELLOW + "{Warming Up}";
	public static String DISABLED = ChatColor.RED + "{Inactive}";
	
	public static void deployShield(Craft ship, Block sign, Sign[] signs, Player p){
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		p.sendMessage(ChatColor.YELLOW + "Your ship's shield has been activated.");
		p.sendMessage(ChatColor.YELLOW + "Shield warming up... estimated time: 5 minutes");
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for(MovecraftLocation l : ship.getBlockList()){
			if(l.getY() < minY) minY = l.getY();
			if(l.getY() > maxY) maxY = l.getY();
			if(l.getX() > maxX) maxX = l.getX();
			if(l.getZ() > maxZ) maxZ = l.getZ();
		}
		BlockVector min = new BlockVector(ship.getMinX(), minY, ship.getMinZ());
		BlockVector max = new BlockVector(maxX, maxY, maxZ);
		String[] members = getMembers(signs);
		final PendingActivation a = new PendingActivation(ship.getW(), min, max, createRName(p.getName(), ship), p.getName(), members, sign);
		pendingActivations.add(a);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				a.activate(pendingActivations.contains(a));
				pendingActivations.remove(a);
			}
		}, SHIELD_DELAY_TICKS);
	}
	
	private static String[] getMembers(Sign[] signArr) {
		ArrayList<String> retval = new ArrayList<String>();
		for(Sign s : signArr){
			String line0 = s.getLine(0);
			if ( InteractListener.getCraftTypeFromString( line0 ) != null || line0.equals("[private]")) {
				String[] lines = s.getLines();
				for(int i = 1; i < lines.length; i++){
					String name = lines[i];
					if(name != null && name.trim().length() > 0){
						retval.add(name);
					}
				}
			} else if(line0.equals(CryoSpawn.KEY_LINE)){
				String name = s.getLine(1);
				if(name != null && name.trim().length() > 0){
					retval.add(name);
				}
			}
				
		}
		return retval.toArray(new String[1]);
	}

	public static void removeShield(Craft ship){
		String rname = createRName(ship.pilot.getName(), ship);
		ProtectedRegion r = wg.getRegionManager(ship.getW()).getRegionExact(rname);
		for(int i = 0; i < pendingActivations.size(); i++){
			PendingActivation a = pendingActivations.get(i);
			if(MathUtils.playerIsWithinBoundingPolygon( ship.getHitBox(), ship.getMinX(), ship.getMinZ(), MathUtils.bukkit2MovecraftLoc( a.sign.getLocation()))){
				pendingActivations.remove(a);
				ship.pilot.sendMessage(ChatColor.RED + "A shield activation was interrupted.");
				i--;
			}
		}
		if(r != null){
			wg.getRegionManager(ship.getW()).removeRegion(rname);
		}
	}
	
	public static void removeShield(Sign sign){
		String rname = createRName(sign);
		ProtectedRegion r = wg.getRegionManager(sign.getWorld()).getRegionExact(rname);
		if(r != null){
			wg.getRegionManager(sign.getWorld()).removeRegion(rname);
		}
	}
	
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	private static String createRName(String player, Craft ship){
		return (CryoSpawn.signTrim(player) + "-shielded-ship");
	}
	
	public static String createRName(Sign s){
		return s.getLine(2) + "-shielded-ship";
	}
	
	private static class PendingActivation{
		String name;
		World w;
		BlockVector min;
		BlockVector max;
		String pilot;
		String[] members;
		Block sign;
		
		private PendingActivation(World w, BlockVector min, BlockVector max, String name, String pilot, String[] members, Block sign){
			this.name = name;
			this.w = w;
			this.min = min;
			this.max = max;
			this.sign = sign;
			this.pilot = pilot;
			this.members = members;
		}
		
		private void activate(boolean succeed){
			Player p = Bukkit.getPlayer(pilot);
			if(!succeed){
				if(p != null){
					p.sendMessage(ChatColor.RED + "Your shield failed to deploy, its warmup cycle was interrupted.");
					
					if(sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST){
						Sign s = (Sign) sign.getState();
						if(isShieldSign(s)){
							s.setLine(1, DISABLED);
							s.setLine(2, "");
							s.update();
						}
					}
				}
			} else {
				ProtectedRegion reg = new ProtectedCuboidRegion(name, min, max);
				RegionManager rm = wg.getRegionManager(w);
				ProtectedRegion remove = rm.getRegionExact(name);
				if(remove != null){
					rm.removeRegion(name);
					if(p.isOnline()){
						p.sendMessage(ChatColor.RED + "An existing starship shield was removed; you cannot have two shields in the same world.");
					}
				}
				if(sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST){
					Sign s = (Sign) sign.getState();
					if(isShieldSign(s)){
						s.setLine(1, ENABLED);
						s.setLine(2, CryoSpawn.signTrim(pilot));
						s.update();
					}
				}
				wg.getRegionManager(w).addRegion(reg);
				DefaultDomain members = new DefaultDomain();
				fill(members, this.members);
				reg.setMembers(members);
				if(p.isOnline()){
					p.sendMessage(ChatColor.GREEN + "Your starship's shield is fully warmed up and is providing full protection.");
				}
			}
		}
	}

	public static boolean isShieldSign(Sign s) {
		return s.getLine(0).equals(SIGN_LINE_0);
	}

	public static void disableShield(Sign s, Craft c) {
		s.setLine(1, DISABLED);
		s.setLine(2, "");
		s.update();
		removeShield(c);
	}
	
	public static void setupShieldSign(SignChangeEvent s){
		s.setLine(0, SIGN_LINE_0);
		s.setLine(1, DISABLED);
	}
	
	public static void enableShield(Sign s, Craft c, Sign[] signs, Player p) {
		s.setLine(1, WARMING);
		s.update();
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		deployShield(c, s.getBlock(), signs, p);
	}

	public static void enableShield(Craft c, ArrayList<MovecraftLocation> signLocations, Player p) {
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		System.out.println("Enable Shield Called");
		System.out.println("SignLocations length: " + signLocations.size());
		Sign[] signs = toSigns(signLocations.toArray(new MovecraftLocation[1]), c.getW());
		System.out.println("Signs length: " + signs.length);
		for(Sign s : signs){
			System.out.println("Testing for shield sign!");
			if(isShieldSign(s)){
				System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
				enableShield(s, c, signs, p);
				System.out.println("Enabling Shield!");
				return;
			}
		}
	}
	
	private static void fill(DefaultDomain d, String[] s){
		for(String str : s){
			d.addPlayer(str);
		}
	}
	
	private static Sign[] toSigns(MovecraftLocation[] list, World w){
		Sign[] retval = new Sign[list.length];
		for(int i = 0; i < list.length; i++){
			MovecraftLocation l = list[i];
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) b.getState();
			retval[i] = s;
		}
		return retval;
	}
}
