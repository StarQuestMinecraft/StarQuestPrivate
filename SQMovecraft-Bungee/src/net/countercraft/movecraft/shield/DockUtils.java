package net.countercraft.movecraft.shield;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.massivecore.ps.PS;

public class DockUtils {
	public static boolean checkForCanDeployShield(Player p, ProtectedRegion pendingShield) {
		int[] cmin = getChunkCoordinates(p.getWorld(), pendingShield.getMinimumPoint().getBlockX(), pendingShield.getMinimumPoint().getBlockZ());
		int[] cmax = getChunkCoordinates(p.getWorld(), pendingShield.getMaximumPoint().getBlockX(), pendingShield.getMaximumPoint().getBlockZ());
		// iterate over every chunk that the region contains
		for (int x = cmin[0]; x <= cmax[0]; x++) {
			for (int z = cmin[1]; z <= cmax[1]; z++) {

				// create a block to test
				Block b = p.getWorld().getBlockAt(x, 100, z);
				/*BlockBreakEvent event = new BlockBreakEvent(b, p);
				CALLING_EVENT = true;
				Bukkit.getServer().getPluginManager().callEvent(event);
				CALLING_EVENT = false;*/
				boolean success = checkTownyBuild(b, p);
				if(success) success = checkFactionsBuild(b, p);
				if (!success){
					//check for dock region at location
					boolean foundDock = false;
					ApplicableRegionSet set = ShieldUtils.wg.getRegionManager(p.getWorld()).getApplicableRegions(new BlockVector(x, 100, z));
					for (ProtectedRegion pr : set) {
						if (isDockRegion(pr)) {
							foundDock = true;
							break;
						}
					}
					if(!foundDock) return false;
				}
			}
		}
		ApplicableRegionSet set = ShieldUtils.wg.getRegionManager(p.getWorld()).getApplicableRegions(pendingShield);
		for (ProtectedRegion pr : set) {
			if (!isDockRegion(pr)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkFactionsBuild(Block b, Player p) {
		return (EngineMain.canPlayerBuildAt(p.getUniqueId(), PS.valueOf(b), false));
	}

	public static boolean checkTownyBuild(Block b, Player p) {
		//TownyWorld world = TownyUniverse.getDataSource().getWorld(b.getWorld().getName());
        //WorldCoord coord = new WorldCoord(world.getName(), Coord.parseCoord(b));

        //Get build permissions (updates if none exist)
        return PlayerCacheUtil.getCachePermission(p, b.getLocation(), b.getTypeId(), b.getData(), TownyPermission.ActionType.BUILD);

        // Allow build if we are permitted
	}

	private static boolean isDockRegion(ProtectedRegion pr) {
		return pr.getId().startsWith("@dock_");
	}

	public static int checkForCanBuild(Player p, int xmin, int zmin, int xmax, int zmax) {
		// first do canbuild checks for towny and factions in all places
		int[] cmin = getChunkCoordinates(p.getWorld(), xmin, zmin);
		int[] cmax = getChunkCoordinates(p.getWorld(), xmax, zmax);

		// iterate over every chunk that the region contains
		for (int x = cmin[0]; x <= cmax[0]; x++) {
			for (int z = cmin[1]; z <= cmax[1]; z++) {

				// create a block to test
				Block b = p.getWorld().getBlockAt(x, 100, z);
				boolean success = checkTownyBuild(b, p);
				if(success) success = checkFactionsBuild(b, p);
				if (!success){
					return 0;
				}	
			}
		}

		// made it through that? Check the worldguard regions
		Map<String, ProtectedRegion> regions = ShieldUtils.wg.getRegionManager(p.getWorld()).getRegions();
		for (String s : regions.keySet()) {
			ProtectedRegion r = regions.get(s);
			if (overlaps(r, xmin, zmin, xmax, zmax)) {
				if (!(r.getMembers().contains(p.getName()) || r.getOwners().contains(p.getName()))) {
					return -1;
				}
			}
		}
		return 1;
	}

	private static boolean overlaps(ProtectedRegion r, int blueTopLeftX, int blueTopLeftZ, int blueBottomRightX, int blueBottomRightZ) {
		return xoverlapcheck(r, blueTopLeftX, blueBottomRightX) && zoverlapcheck(r, blueTopLeftZ, blueBottomRightZ);
	}

	private static boolean xoverlapcheck(ProtectedRegion r, int blueTopLeftX, int blueBottomRightX) {
		Vector blackTopLeft = r.getMinimumPoint();
		Vector blackBottomRight = r.getMaximumPoint();
		// black left side overlaps.
		if ((blackTopLeft.getX() < blueBottomRightX) && (blackTopLeft.getX() > blueTopLeftX)) {
			return true;
		}

		// black right side overlaps.
		if ((blackBottomRight.getX() < blueBottomRightX) && (blackBottomRight.getX() > blueTopLeftX)) {
			return true;
		}

		// black fully contains blue.
		if ((blackBottomRight.getX() > blueBottomRightX) && (blackTopLeft.getX() < blueTopLeftX)) {
			return true;
		}
		return false;
	}

	private static boolean zoverlapcheck(ProtectedRegion r, int blueTopLeftZ, int blueBottomRightZ) {
		Vector blackTopLeft = r.getMinimumPoint();
		Vector blackBottomRight = r.getMaximumPoint();
		// black top side overlaps.
		if ((blackTopLeft.getZ() > blueTopLeftZ) && (blackTopLeft.getZ() < blueBottomRightZ)) {
			return true;
		}

		// black bottom side overlaps.
		if ((blackBottomRight.getZ() > blueTopLeftZ) && (blackBottomRight.getZ() < blueBottomRightZ)) {
			return true;
		}

		// black fully contains blue.
		if ((blackBottomRight.getZ() > blueTopLeftZ) && (blackBottomRight.getZ() < blueBottomRightZ)) {
			return true;
		}
		return false;
	}

	public static int[] getChunkCoordinates(World w, int x, int z) {
		return new int[] { x >> 4, z >> 4 };
	}

	public static boolean claimDock(Player sender) {
		try {
			Selection s = ShieldUtils.wg.getWorldEdit().getSelection(sender);
			Location min = s.getMinimumPoint();
			Location max = s.getMaximumPoint();
			int buildcheck = checkForCanBuild(sender, min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());
			if (buildcheck == 0) {
				sender.sendMessage("Your selection overlaps a town or faction area in which you cannot build.");
				return false;
			} else if (buildcheck == -1) {
				sender.sendMessage("Your selection overlaps a worldguard region; docks cannot be claimed over WGs.");
			}
			String name = generateName(sender, min);
			ProtectedRegion pr = new ProtectedCuboidRegion(name, convertToBV(min), convertToBV(max));
			ShieldUtils.setRegionFlag(pr, DefaultFlag.PASSTHROUGH, "allow");
			RegionManager rm = ShieldUtils.wg.getRegionManager(sender.getWorld());
			rm.addRegion(pr);
			ShieldUtils.saveRM(rm);
			sender.sendMessage("Dock claimed!");
			return true;
		} catch (CommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isDockRegionAndIsOwnedByPlr(ProtectedRegion r, Player p) {
		if (!r.getId().startsWith("@dock_"))
			return false;
		String sub = r.getId().substring(5, r.getId().length());
		if (sub.startsWith(p.getName()))
			return true;
		return false;
	}

	public static void removeDockRegions(Player p) {
		RegionManager rm = ShieldUtils.wg.getRegionManager(p.getWorld());
		ApplicableRegionSet set = rm.getApplicableRegions(p.getLocation());
		ProtectedRegion remove = null;
		for (ProtectedRegion r : set) {
			if (isDockRegionAndIsOwnedByPlr(r, p)) {
				remove = r;
				break;
			}
		}
		if (remove != null) {
			rm.removeRegion(remove.getId());
			ShieldUtils.saveRM(rm);
			p.sendMessage("Removed a dock region where you're standing!");
		} else {
			p.sendMessage("No dock region owned by you found at this location.");
		}
	}

	private static String generateName(Player p, Location mn) {
		return "@dock_" + p.getName() + "@" + mn.getBlockX() + "," + mn.getBlockY() + "," + mn.getBlockZ();
	}

	public static com.sk89q.worldedit.BlockVector convertToBV(Location location) {
		return new com.sk89q.worldedit.BlockVector(location.getX(), location.getY(), location.getZ());
	}
}
