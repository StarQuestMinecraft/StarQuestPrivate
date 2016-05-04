package net.countercraft.movecraft.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class HangarGateUtils {

	private static HashSet<DestroyedBlock> activeBlocks = new HashSet<DestroyedBlock>();
	
	public static void onEnable(){
		loadFromDatabase(activeBlocks);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				Iterator<DestroyedBlock> i = activeBlocks.iterator();
				while(i.hasNext()){
					DestroyedBlock db = i.next();
					if(db.tryRestore()){
						i.remove();
					}
				}
			}
		}, 100L, 100L);
	}

	public static void addDestroyedHangarBlock(final World w, final MovecraftLocation loc){
		Bukkit.getScheduler().runTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				addDestroyedHangarBlock(new Location(w,loc.getX(),loc.getY(),loc.getZ()).getBlock());
			}
		});
	}
	private static void addDestroyedHangarBlock(Block b){
		final DestroyedBlock d = new DestroyedBlock(b.getLocation(), b.getData());
		BukkitRunnable r = new BukkitRunnable(){
			public void run(){
				activeBlocks.add(d);
			}
		};
		r.runTaskLater(Movecraft.getInstance(), 100L);
	}
	
	public static void onDisable(){
		saveToDatabase(activeBlocks);
	}
	
	private static void loadFromDatabase(HashSet<DestroyedBlock> activeBlocks2) {
		File f = new File(Movecraft.getInstance().getDataFolder() + File.separator + "gaterestores.pending");
		World w = Bukkit.getWorld(Bukkit.getServerName());
		if(f.exists()){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				while((line = reader.readLine()) != null){
					activeBlocks.add(new DestroyedBlock(line,w));
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Read " + activeBlocks.size() + " pending hangar gate blocks from file.");
	}
	private static void saveToDatabase(HashSet<DestroyedBlock> activeBlocks2) {
		File f = new File(Movecraft.getInstance().getDataFolder() + File.separator + "gaterestores.pending");
		if(f.exists()){
			f.delete();
		}
		try {
			f.createNewFile();
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			for(DestroyedBlock d : activeBlocks){
				writer.println(d.toString());
			}
			System.out.println("Wrote " + activeBlocks.size() + " pending hangar gate blocks to file.");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class DestroyedBlock{
		
		private static final long ONE_WEEK = 1000 * 60 * 60;
		
		private Location l;
		private byte data;
		private long bornTime;
		
		public DestroyedBlock(Location l, byte data){
			this.l = l;
			this.data = data;
			this.bornTime = System.currentTimeMillis();
		}
		
		public DestroyedBlock(String line, World w){
			String[] split = line.split(",");
			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);
			int z = Integer.parseInt(split[2]);
			l = new Location(w,x,y,z);
			data = Byte.parseByte(split[3]);
			bornTime = Long.parseLong(split[4]);
		}
		
		public String toString(){
			return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + data + "," + bornTime;
		}

		public boolean tryRestore(){
			Material type = l.getBlock().getType();
			if(type == Material.STAINED_GLASS && l.getBlock().getData() == data) return true;
			else if(type == Material.AIR || type == Material.WEB || (System.currentTimeMillis() - bornTime > ONE_WEEK)){
				l.getBlock().setTypeIdAndData(95, data, false);
				return true;
			} else {
				l.getWorld().playSound(l, Sound.BLOCK_LAVA_POP, 20, 1);
				return false;
			}
		}
	}
}
