package net.countercraft.movecraft.utils;

import java.util.HashSet;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FakeBlockUtils {
	public static void sendFakeBlocks(Player p, Location newloc){
		final HashSet<Block> blocksToUpdate=new HashSet<Block>();
		World w = p.getWorld();
		Location loc=newloc.clone();
		loc=loc.subtract(0, 1, 0);
		p.sendBlockChange(loc, 166, (byte) 0);
		blocksToUpdate.add(w.getBlockAt(loc));
		p.sendBlockChange(loc.add(0, 1, 0), 0, (byte) 0);
		blocksToUpdate.add(w.getBlockAt(loc.add(0, 1, 0)));
		p.sendBlockChange(loc.add(0, 2, 0), 0, (byte) 0);
		blocksToUpdate.add(w.getBlockAt(loc.add(0, 2, 0)));

		// send neighboring blocks if the player is near them
		if(newloc.getX()-newloc.getBlockX()<0.5) {
			Location nloc=newloc.clone();
			nloc=nloc.subtract(1, 1, 0);
			p.sendBlockChange(nloc, 166, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc));
			p.sendBlockChange(nloc.add(0, 1, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 1, 0)));
			p.sendBlockChange(nloc.add(0, 2, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 2, 0)));
		} else {
			Location nloc=newloc.clone();
			nloc=nloc.subtract(-1, 1, 0);
			p.sendBlockChange(nloc, 166, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc));
			p.sendBlockChange(nloc.add(0, 1, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 1, 0)));
			p.sendBlockChange(nloc.add(0, 2, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 2, 0)));									
		}
		if(newloc.getZ()-newloc.getBlockZ()<0.5) {
			Location nloc=newloc.clone();
			nloc=nloc.subtract(0, 1, 1);
			p.sendBlockChange(nloc, 166, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc));
			p.sendBlockChange(nloc.add(0, 1, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 1, 0)));
			p.sendBlockChange(nloc.add(0, 2, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 2, 0)));
		} else {
			Location nloc=newloc.clone();
			nloc=nloc.subtract(0, 1, -1);
			p.sendBlockChange(nloc, 166, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc));
			p.sendBlockChange(nloc.add(0, 1, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 1, 0)));
			p.sendBlockChange(nloc.add(0, 2, 0), 0, (byte) 0);
			blocksToUpdate.add(w.getBlockAt(nloc.add(0, 2, 0)));									
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Block b : blocksToUpdate)
					b.getState().update();
			}
		}.runTaskLater( Movecraft.getInstance(), ( 20 * 4 ) );
	}
}
