package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ShipNuker {
	
	public static void nuke(Player sender){
		Craft c = CraftManager.getInstance().getCraftByPlayer(sender);
		if(c == null){
			sender.sendMessage("You are not currently piloting a ship.");
		} else {
			World w = sender.getWorld();
			for(MovecraftLocation l : c.getBlockList()){
				Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
				b.setType(Material.AIR);
			}
			CraftManager.getInstance().removeCraft(c);
		}
	}
}
