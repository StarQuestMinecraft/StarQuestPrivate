package net.countercraft.movecraft.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class SignUtils {
	@SuppressWarnings("deprecation")
	public static BlockFace getFacingBlockFace(Sign s){
		Block sBlock = s.getBlock();
		int data = sBlock.getData();
		switch(data){
			case 2:
				return BlockFace.SOUTH;
			case 3:
				return BlockFace.NORTH;
			case 4:
				return BlockFace.EAST;
			case 5:
				return BlockFace.WEST;
			default:
				return null;
		}
	}
	
	public static BlockFace rotate(BlockFace b, Rotation r){
		if(r == Rotation.CLOCKWISE){
			switch(b){
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			default:
				return b;
			}
		} else {
			switch(b){
			case NORTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.NORTH;
			default:
				return b;
			}
		}
	}
}
