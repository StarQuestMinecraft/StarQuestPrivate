package net.countercraft.movecraft.slip;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.task.RepeatTryServerJumpTask;
import net.countercraft.movecraft.utils.DirectionUtils;
import net.countercraft.movecraft.utils.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WarpUtils {
	
	static FuelTakeTask task;
	
	static{
		task = new FuelTakeTask(WarpUtils.getEnd(Bukkit.getWorld(Bukkit.getServerName())));
	}
	
	public static void enterWarp(Player p, Craft c){
		Location l = p.getLocation();
		for(UUID u : c.playersRidingShip){
			Player plr = Movecraft.getPlayer(u);
			plr.playSound(plr.getLocation(), Sound.PORTAL_TRAVEL, 2.0F, 1.0F);
		}
		World w = getEnd(p.getWorld());
		if(w == null) return;
		RepeatTryWorldJumpTask task = new RepeatTryWorldJumpTask(c, p, new Location(w, l.getX(), l.getY(), l.getZ()), true, false);
		task.runTaskTimer(Movecraft.getInstance(), 0, 1);
		c.warpCoordsX = l.getBlockX();
		c.warpCoordsZ = l.getBlockZ();
	}
	
	public static void leaveWarp(Player p, Craft c, boolean repilot){
		leaveWarp(p, c, repilot, true);
	}
	public static void leaveWarp(Player p, Craft c, boolean repilot, boolean searchSlipSigns){
		if(!c.isProcessing()){
			Location l = p.getLocation();
			World w2 = getNormal(p.getWorld());
			if(w2 == null) return;
			Location targ = new Location(w2, c.warpCoordsX, l.getY(), c.warpCoordsZ);
			for(int i = 0; i < c.playersRidingShip.size(); i++){
				Player plr = Movecraft.getPlayer(c.playersRidingShip.get(i));
				plr.playSound(plr.getLocation(), Sound.PORTAL_TRAVEL, 2.0F, 1.0F);
			}
			RepeatTryWorldJumpTask task = new RepeatTryWorldJumpTask(c, p, targ, repilot, searchSlipSigns);
			task.runTaskTimer(Movecraft.getInstance(), 0, 1);
		}
	}
	public static void translate(Craft c, int x, int y, int z){
		if(LocationUtils.isBeingJammed(getNormal(c.getW()), c.getMinX(), c.getMinZ())){
			leaveWarp(c.pilot, c, true);
			c.pilot.sendMessage("Your warp field was disrupted by a jamming device!");
			return;
		}
		c.warpCoordsX =  c.warpCoordsX + x;
		c.warpCoordsZ = c.warpCoordsZ + z;
		Player p = c.pilot;
		p.sendMessage("Coordinates: " + c.warpCoordsX + "," + c.warpCoordsZ);
		
		//should they be in another system?
		String system = LocationUtils.slipWarpCheck(c.warpCoordsX, c.warpCoordsZ);
		if(system != null){
			p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + " You have reached the edge of the solar system!");
			p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Your ship's computer will now navigate through the Slip to the next solar system.");
			RepeatTryServerJumpTask t = new RepeatTryServerJumpTask(p, c, system, system + "_the_end", LocationUtils.getSlipCoordX(system, c.warpCoordsX), 100, LocationUtils.getSlipCoordZ(system, c.warpCoordsZ));
			c.setProcessingTeleport(true);
			t.runTaskTimer(Movecraft.getInstance(), 0, 1);
		}
	}
	
	public static World getEnd(World normal){
		return Bukkit.getWorld(normal.getName() + "_the_End");
	}
	public static World getNormal(World end){
		return Bukkit.getWorld(end.getName().substring(0, end.getName().length() - 8));
	}
	
	
	public static boolean takeFuel(Sign s, int amount){
		BlockFace dir = DirectionUtils.getGateDirection(s.getBlock());
		Block dia = s.getBlock().getRelative(dir);
		if(dia.getType() != Material.DIAMOND_BLOCK) return false;
		Block hopperLeft = dia.getRelative(DirectionUtils.getBlockFaceLeft(dir));
		if(hopperLeft.getType() != Material.HOPPER) return false;
		Block hopperRight = dia.getRelative(DirectionUtils.getBlockFaceRight(dir));
		if(hopperRight.getType() != Material.HOPPER) return false;
		
		Hopper hpr1 = (Hopper) hopperLeft.getState();
		Hopper hpr2 = (Hopper) hopperRight.getState();
		
		Inventory i = hpr1.getInventory();
		Inventory i2 = hpr2.getInventory();
		
		if(i.contains(Material.EYE_OF_ENDER, amount)) {
			i.removeItem(new ItemStack(Material.EYE_OF_ENDER, amount));
			return true;
			
		} else if(i2.contains(Material.EYE_OF_ENDER, amount)){
			i.removeItem(new ItemStack(Material.EYE_OF_ENDER, amount));
			return true;
		} else {
			return false;
		}
	}
}
