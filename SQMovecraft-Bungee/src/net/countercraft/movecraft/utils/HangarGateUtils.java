package net.countercraft.movecraft.utils;

import java.util.HashSet;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class HangarGateUtils {

	private static HashSet<DestroyedBlock> activeBlocks = new HashSet<DestroyedBlock>();
	public static void addDestroyedHangarBlock(Block b){
		final DestroyedBlock d = new DestroyedBlock(b.getLocation(), b.getData());
		activeBlocks.add(d);
		BukkitRunnable r = new BukkitRunnable(){
			public void run(){
				if(d.restore()){
					activeBlocks.remove(d);
					this.cancel();
				}
			}
		};
		r.runTaskTimer(Movecraft.getInstance(), 100L, 100L);
	}
	
	public static void onDisable(){
		for(DestroyedBlock d : activeBlocks){
			d.forceRestore();
		}
	}
	
	private static class DestroyedBlock{
		
		private Location l;
		private byte data;
		private int tries = 0;
		
		public DestroyedBlock(Location l, byte data){
			this.l = l;
			this.data = data;
		}
		
		public boolean restore(){
			Material type = l.getBlock().getType();
			if(type == Material.AIR || type == Material.WEB || tries >= 3){
				l.getBlock().setTypeIdAndData(95, data, false);
				return true;
			} else {
				tries++;
				l.getWorld().playSound(l, Sound.BLOCK_LAVA_POP, 20, 1);
				return false;
			}
		}
		
		public void forceRestore(){
			l.getBlock().setTypeIdAndData(95, data, false);
		}
	}
}
