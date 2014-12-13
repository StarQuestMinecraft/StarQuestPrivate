package net.countercraft.movecraft.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.listener.InteractListener;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.FactionColls;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShieldUtils {
	
	static ArrayList<PendingActivation> pendingActivations = new ArrayList<PendingActivation>();
	
	private static WorldGuardPlugin wg = getWorldGuard();
	private static long SHIELD_DELAY_TICKS = 5 * 60 * 20;
	
	private static String SIGN_LINE_0 = ChatColor.BLUE + "Ship Shield";
	/*public static String ENABLED = ChatColor.GREEN + "{Protected}";
	public static String WARMING = ChatColor.YELLOW + "{Warming Up}";
	public static String DISABLED = ChatColor.RED + "{Inactive}";*/
	
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
		final PendingActivation a = new PendingActivation(ship.getW(), min, max, createRName(p.getName()), p.getName(), members, sign);
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

	/*public static void removeShield(Craft ship){
		String rname = createRName(ship.pilot.getName(), ship);
		RegionManager rm = wg.getRegionManager(ship.getW());
		ProtectedRegion r = rm.getRegionExact(rname);
		for(int i = 0; i < pendingActivations.size(); i++){
			PendingActivation a = pendingActivations.get(i);
			if(MathUtils.playerIsWithinBoundingPolygon( ship.getHitBox(), ship.getMinX(), ship.getMinZ(), MathUtils.bukkit2MovecraftLoc( a.sign.getLocation()))){
				pendingActivations.remove(a);
				ship.pilot.sendMessage(ChatColor.RED + "A shield activation was interrupted.");
				i--;
			}
		}
		if(r != null){
			rm.removeRegion(rname);
			saveRM(rm);
		}
	}*/
	
	/*public static void removeShield(Sign sign){
		String rname = createRName(sign);
		ProtectedRegion r = wg.getRegionManager(sign.getWorld()).getRegionExact(rname);
		if(r != null){
			RegionManager rm = wg.getRegionManager(sign.getWorld());
		}
	}*/
	
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	private static String createRName(String player){
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
							/*s.setLine(1, DISABLED);
							s.setLine(2, "");
							s.update();*/
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
				/*if(sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST){
					Sign s = (Sign) sign.getState();
					if(isShieldSign(s)){
						s.setLine(1, ENABLED);
						s.setLine(2, CryoSpawn.signTrim(pilot));
						s.update();
					}
				}*/
				rm.addRegion(reg);
				DefaultDomain members = new DefaultDomain();
				fill(members, this.members);
				reg.setMembers(members);
				setRegionFlag(reg, DefaultFlag.OTHER_EXPLOSION, "deny");
				setRegionFlag(reg, DefaultFlag.TNT, "deny");
				setRegionFlag(reg, DefaultFlag.CREEPER_EXPLOSION, "deny");
				if(p.isOnline()){
					p.sendMessage(ChatColor.GREEN + "Your starship's shield is fully warmed up and is providing full protection.");
				}
				saveRM(rm);
			}
		}
	}

	public static boolean isShieldSign(Sign s) {
		return s.getLine(0).equals(SIGN_LINE_0);
	}

	/*public static void disableShield(Sign s, Craft c) {
		/*s.setLine(1, DISABLED);
		s.setLine(2, "");
		s.update();
		removeShield(c);
	}*/
	
	public static void setupShieldSign(SignChangeEvent s){
		s.setLine(0, SIGN_LINE_0);
		//s.setLine(1, DISABLED);
	}
	
	public static void enableShield(Sign s, Craft c, Sign[] signs, Player p) {
		//s.setLine(1, WARMING);
		//s.update();
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		deployShield(c, s.getBlock(), signs, p);
	}

	public static void enableShield(Craft c, ArrayList<MovecraftLocation> signLocations, Player p) {
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		System.out.println("Enable Shield Called");
		System.out.println("SignLocations length: " + signLocations.size());
		// the below method has an NPE somewhere
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
	
	private static void saveRM(RegionManager rm){
		try {
			rm.save();
		} catch (ProtectionDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void checkForAndRemoveShield(Player plr) {
		String name = createRName(plr.getName());
		RegionManager rm = wg.getRegionManager(plr.getWorld());
		ProtectedRegion region = rm.getRegion(name);
		if(region != null){
			rm.removeRegion(name);
			saveRM(rm);
			plr.sendMessage("You boarded a ship and your shield was removed.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setRegionFlag(ProtectedRegion pr, Flag f, String value){
		try {
			pr.setFlag(f, f.parseInput(wg, Bukkit.getConsoleSender(), value));
		} catch (InvalidFlagFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean checkForCanBuild(Player p, int xmin, int zmin, int xmax, int zmax){
		int chunkx = xmin - (xmin % 16);
		System.out.println("x min: " + chunkx);
		int chunkz = zmin - (zmin % 16);
		System.out.println("z min: " + chunkz);
		
		boolean passed = true;
		passcheck:
		for(int x = chunkx; x < xmax; x += 16){
			for(int z = chunkz; z < zmax; z += 16){
				if(!checkCanBuild(p, x, z)){
					passed = false;
					break passcheck;
				}
			}
		}
		return false;
	}
	
	private static boolean checkCanBuild(Player p, int x, int z){
		BlockBreakEvent event = new BlockBreakEvent(p.getWorld().getBlockAt(x,100,z), p);
		Bukkit.getPluginManager().callEvent(event);
		return !event.isCancelled();
	}
}
