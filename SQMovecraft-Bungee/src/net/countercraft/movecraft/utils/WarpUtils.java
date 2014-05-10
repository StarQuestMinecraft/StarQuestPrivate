package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.translation.RepeatTryWorldJumpTask;
import net.countercraft.movecraft.bungee.RepeatTryServerJumpTask;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WarpUtils {
	
	public static void enterWarp(Player p, Craft c){
		Location l = p.getLocation();
		World w = getEnd(p.getWorld());
		if(w == null) return;
		RepeatTryWorldJumpTask task = new RepeatTryWorldJumpTask(c, p, new Location(w, l.getX(), l.getY(), l.getZ()));
		task.runTaskTimer(Movecraft.getInstance(), 0, 1);
		c.warpCoordsX = l.getBlockX();
		c.warpCoordsZ = l.getBlockZ();
	}
	public static void leaveWarp(Player p, Craft c){
		if(!c.processing.get()){
			Location l = p.getLocation();
			World w2 = getNormal(p.getWorld());
			if(w2 == null) return;
			Location targ = new Location(w2, c.warpCoordsX, l.getY(), c.warpCoordsZ);
			boolean success = !destinationObstructed(l, c, targ);	
			World w = l.getWorld();
			if(!success){
				for (MovecraftLocation ml : c.getBlockList()){
					Block b = w.getBlockAt(ml.getX(), ml.getY(), ml.getZ());
					b.setTypeIdAndData(0, (byte)0, false);
					Location bckl = b.getLocation();
					w.createExplosion(l, 0.0F);
				}
				
				CraftManager.getInstance().removeCraft(c);
			} else {
				RepeatTryWorldJumpTask task = new RepeatTryWorldJumpTask(c, p, targ);
				task.runTaskTimer(Movecraft.getInstance(), 0, 1);
			}
		}
	}
	public static void translate(Craft c, int x, int y, int z){
		c.warpCoordsX =  c.warpCoordsX + x;
		c.warpCoordsZ = c.warpCoordsZ + z;
		Player p = c.pilot;
		p.sendMessage("Coordinates: " + c.warpCoordsX + "," + c.warpCoordsZ);
		
		//should they be in another system?
		String system = LocationUtils.slipWarpCheck(c.warpCoordsX, c.warpCoordsZ);
		if(system != null){
			p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + " You have reached the edge of the solar system!");
			p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Your ship's computer will now navigate through the Slip to the next solar system.");
			RepeatTryServerJumpTask t = new RepeatTryServerJumpTask(p, c, system + "_the_end", LocationUtils.getSlipCoordX(system), 100, LocationUtils.getSlipCoordZ(system));
		}
	}
	
	public static World getEnd(World normal){
		return Bukkit.getWorld(normal.getName() + "_the_End");
	}
	
	public static World getNormal(World end){
		return Bukkit.getWorld(end.getName().substring(0, end.getName().indexOf("_the")));
	}

	public static boolean destinationObstructed(Location me, Craft c, Location targ){
    	int dX = getdX(me, targ);
    	int dY = getdY(me, targ);
    	int dZ = getdZ(me, targ);
    	
    	MovecraftLocation[] blockslist = c.getBlockList();
    	World w = targ.getWorld();
    	for (int i = 0; i < blockslist.length; i++) {
			MovecraftLocation oldLoc = blockslist[i];
			Location newLoc = new Location (w, oldLoc.getX() + dX, oldLoc.getY() + dY, oldLoc.getZ() + dZ);
			newLoc.getChunk().load();
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
}
