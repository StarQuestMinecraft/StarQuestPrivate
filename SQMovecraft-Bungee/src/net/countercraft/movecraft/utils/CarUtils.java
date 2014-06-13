package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;

import org.bukkit.World;

public class CarUtils {
	public static int getNewdY(Craft c, int dX, int dZ){
		int minY=65535;
		int maxY=-65535;
		int [][][] hb = c.getHitBox();
		for (int [][] i1 : hb) {
			for (int [] i2 : i1) {
				if(i2!=null) {
					if(i2[0]<minY) {
						minY=i2[0];
					}
					if(i2[1]>maxY) {
						maxY=i2[1];
					}
				}
			}
		}
		int groundLevel = getGroundLevel(c, dX, dZ, minY, maxY, hb);
		if (groundLevel < minY - 2){
			return -1;
		}
		if (groundLevel > minY - 2){
			return 1;
		}else{
			return 0;
		}
	}
	public static int getGroundLevel(Craft c, int dX, int dZ, int minY, int maxY, int [] [] [] hb){
		
		// start by finding the minimum and maximum y coord
		
		int maxX=c.getMinX() + dZ +hb.length;
		int maxZ=c.getMinZ()+ dZ + hb[0].length;  // safe because if the first x array doesn't have a z array, then it wouldn't be the first x array
		int minX=c.getMinX() + dX;
		int minZ=c.getMinZ() + dZ;
		
		//caulculate the y heights of the ground at the corners
		int y = getHighestBlockBelow(c.getW(), minY, minX, minZ);
		int y2 = getHighestBlockBelow(c.getW(), minY, maxX, minZ);
		int y3 = getHighestBlockBelow(c.getW(), minY, minX, maxZ);
		int y4 = getHighestBlockBelow(c.getW(), minY, maxX, maxZ);
		
		if (y2 > y) y = y2;
		if (y3 > y) y = y3;
		if (y4 > y) y = y4;
		
		return y;
	}
	@SuppressWarnings("deprecation")
	public static int getHighestBlockBelow(World w, int belowheight, int x, int z){
		for (int i = belowheight - 1; i > 0; i--){
			if (w.getBlockTypeIdAt(x, i, z) != 0){
				return i;
			}
		}
		return 0;
	}
}
