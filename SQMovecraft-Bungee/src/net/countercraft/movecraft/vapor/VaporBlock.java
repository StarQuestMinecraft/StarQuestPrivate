package net.countercraft.movecraft.vapor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class VaporBlock {
	
	int duration;
	int updates = 0;
	Location l;
	
	public VaporBlock(Location l, int duration){
		this.duration = duration;
		this.l = l;
	}
	
	public boolean update(){
		updates++;
		if(l.getBlock().getType() == Material.AIR)
			l.getBlock().setType(Material.WEB);
		if(updates >= duration){
			Block b = l.getBlock();
			if(b.getType() == Material.WEB){
				b.setType(Material.AIR);
				return true;
			}
		}
		return false;
	}
}
