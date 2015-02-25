package net.countercraft.movecraft.vapor;

import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class VaporUtils {

	static VaporRunnable run = null;

	public static void testAndCreateTrail(Location source, int minx, int miny, int minz, int maxx, int maxy, int maxz, int dx, int dy, int dz) {
		int x = source.getBlockX();
		int y = source.getBlockY();
		int z = source.getBlockZ();

		// while our coordinates are still within the ship bounding box
		while (minx <= x && x <= maxx && miny <= y && y <= maxy && minz <= z && z <= maxz) {

			// consider each coordinate backwards against the direction of
			// motion
			x -= dx;
			y -= dy;
			z -= dz;

			int id = source.getWorld().getBlockTypeIdAt(x, y, z);
			if (id != 0) {
				// found an obstruction, return
				return;
			}
		}

		// left the bounding box without hitting anything, it's an exterior
		// glowstone
		int targetX = source.getBlockX() - dx;
		int targetY = source.getBlockY() - dy;
		int targetZ = source.getBlockZ() - dz;
		
		VaporBlock vb = new VaporBlock(new Location(source.getWorld(), targetX, targetY, targetZ), (int) (Math.random() * 20));
		VaporRunnable.addForNextRun(vb);
		if (run == null) {
			run = new VaporRunnable();
			run.runTaskTimer(Movecraft.getInstance(), 5, 5);
		}
	}

}
