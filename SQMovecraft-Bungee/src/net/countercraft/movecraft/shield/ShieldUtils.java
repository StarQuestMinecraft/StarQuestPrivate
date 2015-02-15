package net.countercraft.movecraft.shield;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShieldUtils {

	public static ArrayList<PendingActivation> pendingActivations = new ArrayList<PendingActivation>();

	public static WorldGuardPlugin wg = getWorldGuard();
	private static long SHIELD_DELAY_TICKS = 5 * 20 * 60;

	private static String SIGN_LINE_0 = ChatColor.BLUE + "Ship Shield";
	private static String SIGN2_LINE_0 = ChatColor.BLUE + "More Access";

	/*
	 * public static String ENABLED = ChatColor.GREEN + "{Protected}"; public
	 * static String WARMING = ChatColor.YELLOW + "{Warming Up}"; public static
	 * String DISABLED = ChatColor.RED + "{Inactive}";
	 */

	public static void removePendingActivationsForPlayer(Player p) {
		for (int i = 0; i < pendingActivations.size(); i++) {
			if (pendingActivations.get(i).pilot.equals(p.getName())) {
				pendingActivations.remove(i);
				return;
			}
		}
	}

	public static void deployShield(Craft ship, Block sign, Sign[] signs, Player p) {
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (MovecraftLocation l : ship.getBlockList()) {
			if (l.getY() < minY)
				minY = l.getY();
			if (l.getY() > maxY)
				maxY = l.getY();
			if (l.getX() > maxX)
				maxX = l.getX();
			if (l.getZ() > maxZ)
				maxZ = l.getZ();
		}
		BlockVector min = new BlockVector(ship.getMinX(), minY, ship.getMinZ());
		BlockVector max = new BlockVector(maxX, maxY, maxZ);
		ArrayList<String> members = getMembersMainSign(p, sign);
		if (DockUtils.checkForCanDeployShield(p, new ProtectedCuboidRegion("test", min, max))) {
			p.sendMessage(ChatColor.YELLOW + "Your ship's shield has been activated.");
			p.sendMessage(ChatColor.YELLOW + "Shield warming up... estimated time: 5 minutes");

			final PendingActivation a = new PendingActivation(ship.getW(), min, max, createRName(p.getName()), p.getName(), members, sign);
			pendingActivations.add(a);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
				public void run() {
					a.activate(pendingActivations.contains(a) && (a.sign.getType() == Material.WALL_SIGN || a.sign.getType() == Material.SIGN_POST));
					pendingActivations.remove(a);
				}
			}, SHIELD_DELAY_TICKS);
		}
	}

	private static ArrayList<String> getMembersMainSign(Player p, Block sign) {
		ArrayList<String> retvals = new ArrayList<String>();
		retvals.addAll(getMembers(p, sign));
		for (Block b : BlockUtils.getEdges(sign, false, false)) {
			if (b.getType() == Material.WALL_SIGN) {
				Sign s = (Sign) b.getState();
				if (s.getLine(0).equalsIgnoreCase(SIGN2_LINE_0)) {
					retvals.addAll(getMembers(p, sign));
				}
			}
		}
		return retvals;
	}

	private static ArrayList<String> getMembers(Player p, Block sign) {
		Sign s = (Sign) sign.getState();
		String[] lines = s.getLines();
		ArrayList<String> retval = new ArrayList<String>(3);
		for (int i = 0; i < 3; i++) {
			String line = lines[i + 1];
			if (line.equals(""))
				continue;
			UUID u = Compression.str15ToUuid(line);
			OfflinePlayer plr = Bukkit.getOfflinePlayer(u);
			retval.add(plr.getName());
		}
		return retval;
	}

	/*
	 * private static String[] getMembers(Sign[] signArr) { ArrayList<String>
	 * retval = new ArrayList<String>(); for (Sign s : signArr) { String line0 =
	 * s.getLine(0); if (InteractListener.getCraftTypeFromString(line0) != null
	 * || line0.equals("[private]")) { String[] lines = s.getLines(); for (int i
	 * = 1; i < lines.length; i++) { String name = lines[i]; if (name != null &&
	 * name.trim().length() > 0) { retval.add(name); } } } else if
	 * (line0.equals(CryoSpawn.KEY_LINE)) { String name = s.getLine(1); if (name
	 * != null && name.trim().length() > 0) { retval.add(name); } }
	 * 
	 * } return retval.toArray(new String[1]); }
	 */

	/*
	 * public static void removeShield(Craft ship){ String rname =
	 * createRName(ship.pilot.getName(), ship); RegionManager rm =
	 * wg.getRegionManager(ship.getW()); ProtectedRegion r =
	 * rm.getRegionExact(rname); for(int i = 0; i < pendingActivations.size();
	 * i++){ PendingActivation a = pendingActivations.get(i);
	 * if(MathUtils.playerIsWithinBoundingPolygon( ship.getHitBox(),
	 * ship.getMinX(), ship.getMinZ(), MathUtils.bukkit2MovecraftLoc(
	 * a.sign.getLocation()))){ pendingActivations.remove(a);
	 * ship.pilot.sendMessage(ChatColor.RED +
	 * "A shield activation was interrupted."); i--; } } if(r != null){
	 * rm.removeRegion(rname); saveRM(rm); } }
	 */

	/*
	 * public static void removeShield(Sign sign){ String rname =
	 * createRName(sign); ProtectedRegion r =
	 * wg.getRegionManager(sign.getWorld()).getRegionExact(rname); if(r !=
	 * null){ RegionManager rm = wg.getRegionManager(sign.getWorld()); } }
	 */

	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}

	private static String createRName(String player) {
		return (CryoSpawn.signTrim(player) + "-shielded-ship");
	}

	public static String createRName(Sign s) {
		return s.getLine(2) + "-shielded-ship";
	}

	public static boolean isShieldSign(Sign s) {
		return s.getLine(0).equals(SIGN_LINE_0);
	}

	/*
	 * public static void disableShield(Sign s, Craft c) { /*s.setLine(1,
	 * DISABLED); s.setLine(2, ""); s.update(); removeShield(c); }
	 */

	public static void setupShieldSign(SignChangeEvent s) {
		s.setLine(0, SIGN_LINE_0);
		String[] lines = s.getLines();
		int start = 1;
		if(s.getLine(1) == null || s.getLine(1) == ""){
			s.setLine(1, Compression.uuidToStr15(s.getPlayer().getUniqueId()));
			s.getPlayer().sendMessage("Player " + s.getPlayer().getName() + " added to this shield.");
			start++;
		}
		for (int i = start; i < lines.length; i++) {
			String signName = lines[i];
			if (signName == null || signName.equals(""))
				continue;
			Player p = Bukkit.getPlayer(signName);
			if (p == null) {
				s.getPlayer().sendMessage("Player " + signName + " is not online at the moment and thus cannot be added to this shield.");
				s.setLine(i, "");
			} else {
				s.setLine(i, Compression.uuidToStr15(p.getUniqueId()));
				s.getPlayer().sendMessage("Player " + p.getName() + " added to this shield.");
			}
		}
	}

	public static void setupMoreShieldSign(SignChangeEvent s) {
		s.setLine(0, SIGN2_LINE_0);
		String[] lines = s.getLines();
		for (int i = 1; i < lines.length; i++) {
			String signName = lines[i];
			if (lines[i] == null || lines[i] == "")
				continue;
			Player p = Bukkit.getPlayerExact(signName);
			if (p == null) {
				s.getPlayer().sendMessage("Player " + signName + " is not online at the moment and thus cannot be added to this shield.");
				s.setLine(i, "");
			} else {
				s.setLine(i, Compression.uuidToStr15(p.getUniqueId()));
				s.getPlayer().sendMessage("Player " + p.getName() + " added to this shield.");
			}
		}
	}

	public static void enableShield(Craft c, ArrayList<MovecraftLocation> signLocations, Player p) {
		System.out.println((p == null) ? ("P is null!") : ("P is not null!"));
		System.out.println("SignLocations length: " + signLocations.size());
		// the below method has an NPE somewhere
		Sign[] signs = toSigns(signLocations, c.getW());
		System.out.println("Signs length: " + signs.length);
		for (Sign s : signs) {
			if (isShieldSign(s)) {
				long t1 = System.currentTimeMillis();
				deployShield(c, s.getBlock(), signs, p);
				long t2 = System.currentTimeMillis();
				System.out.println("Shield deploy took " + (t2 - t1) + " ms");
				return;
			}
		}
	}

	public static void fill(DefaultDomain d, List<String> members) {
		for (String str : members) {
			if (!(str == null || str.equals(""))) {
				d.addPlayer(str);
			}
		}
	}

	private static Sign[] toSigns(ArrayList<MovecraftLocation> list, World w) {
		Sign[] retval = new Sign[list.size()];
		for (int i = 0; i < list.size(); i++) {
			MovecraftLocation l = list.get(i);
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) b.getState();
			retval[i] = s;
		}
		return retval;
	}

	public static void saveRM(RegionManager rm) {
		try {
			rm.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void checkForAndRemoveShield(Player plr) {
		String name = createRName(plr.getName());
		RegionManager rm = wg.getRegionManager(plr.getWorld());
		ProtectedRegion region = rm.getRegion(name);
		if (region != null) {
			rm.removeRegion(name);
			saveRM(rm);
			plr.sendMessage("You boarded a ship and your shield was removed.");
		}
	}

	@SuppressWarnings("unchecked")
	public static void setRegionFlag(ProtectedRegion pr, Flag f, String value) {
		try {
			pr.setFlag(f, f.parseInput(wg, Bukkit.getConsoleSender(), value));
		} catch (InvalidFlagFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
