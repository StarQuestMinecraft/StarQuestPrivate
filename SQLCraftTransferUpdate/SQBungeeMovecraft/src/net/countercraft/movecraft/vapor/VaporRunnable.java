package net.countercraft.movecraft.vapor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class VaporRunnable extends BukkitRunnable {

	static ArrayList<VaporBlock> blocks = new ArrayList<VaporBlock>();
	static HashSet<VaporBlock> addNextTick = new HashSet<VaporBlock>();

	@Override
	public void run() {

		blocks.addAll(addNextTick);
		addNextTick.clear();
		ListIterator<VaporBlock> i = blocks.listIterator();
		while (i.hasNext()) {
			VaporBlock b = i.next();
			if (b.update()) {
				i.remove();
			}
		}
		if (blocks.size() == 0) {
			// nothing to process, cancel self
			this.cancel();
			VaporUtils.run = null;
		}
	}

	public static void addForNextRun(VaporBlock b) {
		addNextTick.add(b);
	}

	public static boolean isVaporBlock(Block block) {
		for (VaporBlock b : blocks) {
			if (b.l.equals(block.getLocation())) {
				return true;
			}
		}
		return false;
	}

	public static void onDisable() {
		for (VaporBlock b : blocks) {
			if (b.l.getBlock().getType() == Material.WEB) {
				b.l.getBlock().setType(Material.AIR);
			}
		}
	}
}
