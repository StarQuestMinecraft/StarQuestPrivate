package net.countercraft.movecraft.slip;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FuelTakeTask extends BukkitRunnable{

	public static int FUEL_AMOUNT = 1;
	public static int FUEL_INTERVAL = 20 * 60 * 2;
	World w;
	
	public FuelTakeTask(World w){
		this.w = w;
		runTaskTimer(Movecraft.getInstance(), FUEL_INTERVAL, FUEL_INTERVAL);
	}
	@Override
	public void run() {
		Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(w);
		if(crafts == null) return;
		for(Craft c : crafts){
			boolean success = takeFuel(c);
			if(!success){
				c.pilot.sendMessage("You do not have enough fuel to continue, returning to realspace!");
				WarpUtils.leaveWarp(c.pilot, c, false);
			}
		}
	}
	
	
	private boolean takeFuel(Craft c) {
		for(MovecraftLocation l : c.getBlockList()){
			Block b = BlockUtils.getBlockAt(c.getW(), l);
			if(b.getType() == Material.WALL_SIGN){
				Sign s = (Sign) b.getState();
				if(s.getLine(0).equals(ChatColor.AQUA + "Slipspace")){
					c.pilot.sendMessage("Your slipdrive used up another catalyst item.");
					return WarpUtils.takeFuel(s, FUEL_AMOUNT);
				}
			}
		}
		return false;
	}

}
