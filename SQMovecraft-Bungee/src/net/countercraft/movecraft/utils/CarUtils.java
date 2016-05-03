package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Material;
import org.bukkit.World;

public class CarUtils {

	public static int getNewdY(Craft c, int dX, int dZ){
		int [][][] hb = c.getHitBox();

		/*int minY=65535;
		for (int [][] i1 : hb) {
			for (int [] i2 : i1) {
				if(i2!=null) {
					if(i2[0]<minY) {
						minY=i2[0];
					}
				}
			}
		}*/
		int smallestDist = Integer.MAX_VALUE;
		boolean done = false;
		for(int x = 0; x < hb.length && !done; x++){
			for(int z = 0; z < hb[x].length && !done; z++){
				int worldX = x + c.getMinX();
				int worldZ = z + c.getMinZ();
				int[] col = hb[x][z];
				if(col == null) continue;
				int minY = col[0];
				int distToGround = minY - getHighestBlockBelow(c.getW(), minY, worldX, worldZ) - 1;
				if(distToGround < smallestDist){
					smallestDist = distToGround;
					if(distToGround == 0){
						//can't be less than zero so quit early.
						done=true;
					}
				}
			}
		}
		int hoverHeight = c.hoverHeight;
		if (smallestDist > hoverHeight){
			System.out.println("Smallest: " + smallestDist + " hoverHeight: " + hoverHeight);
			return -1;
		}
		if (smallestDist < hoverHeight){
			System.out.println("Smallest: " + smallestDist + " hoverHeight: " + hoverHeight);
			return 1;
		}else{
			return 0;
		}
	}
	/*public static int getMinDistToGround(Craft c, int dX, int dZ, int minY, int [] [] [] hb){
		
		// start by finding the minimum and maximum y coord
		
		int maxX=c.getMinX() + dZ +hb.length;
		int maxZ=c.getMinZ()+ dZ + hb[0].length;  // safe because if the first x array doesn't have a z array, then it wouldn't be the first x array
		int minX=c.getMinX() + dX;
		int minZ=c.getMinZ() + dZ;
		
		//also do midpoints
		int midX = minX + (maxX - minX / 2);
		int midZ = minZ + (maxZ - minZ / 2);
		
		//caulculate the y heights of the ground at the corners
		int y = getHighestBlockBelow(c.getW(), minY, minX, minZ);
		int y2 = getHighestBlockBelow(c.getW(), minY, maxX, minZ);
		int y3 = getHighestBlockBelow(c.getW(), minY, minX, maxZ);
		int y4 = getHighestBlockBelow(c.getW(), minY, maxX, maxZ);
		int y5 = getHighestBlockBelow(c.getW(), minY, midX, midZ);
		int dist = y ;
		if()
		if (y2 > y) y = y2;
		if (y3 > y) y = y3;
		if (y4 > y) y = y4;
		if (y5 > y) y = y5;
		
		return y;
	}
	
	public static int getLowestBlockOnShipAt(int x, int z, int[][][] hb, int minx, int minz){
		return hb[x][z][0];
	}*/
	
	@SuppressWarnings("deprecation")
	public static int getHighestBlockBelow(World w, int belowheight, int x, int z){
		for (int i = belowheight - 1; i > 0; i--){
			if (w.getBlockAt(x, i, z).getType() != Material.AIR){
				return i;
			}
		}
		return 0;
	}
}
