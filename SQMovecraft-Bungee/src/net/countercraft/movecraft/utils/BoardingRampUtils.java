package net.countercraft.movecraft.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class BoardingRampUtils {
	
	static final int[] VALID_TYPES = {35,53,67,108,109,114,128,134,135,136,156,163,164,180};
	@SuppressWarnings("deprecation")
	public static void openRamp(Sign s){
		BlockFace facingdirection = getFacingBlockFace(s);
		
		Block sBlock = s.getBlock();
		Block changeblock = sBlock.getRelative(BlockFace.DOWN);
		Block movingstepsup = changeblock.getRelative(facingdirection);
		Block movingstepsdown = movingstepsup.getRelative(BlockFace.DOWN);
		
		int id = changeblock.getTypeId();
		for(int i : VALID_TYPES){
			if(i == id){
				//stop exploit with boarding ramps removing blocks
				if(movingstepsdown.getType() == Material.AIR){
					s.setLine(2, changeblock.getTypeId() + "");
					s.setLine(3, changeblock.getData() + "");
					s.update();
					
					movingstepsdown.setTypeIdAndData(movingstepsup.getTypeId(), movingstepsup.getData(), true);
					movingstepsup.setType(Material.AIR);
					
					changeblock.setTypeIdAndData(0, (byte) 0, false);
				}
				return;
			}
		}
		
	}
	@SuppressWarnings("deprecation")
	public static boolean closeRamp(Sign s, Player interactor){
		BlockFace facingdirection = getFacingBlockFace(s);
		
		Block sBlock = s.getBlock();
		Block changeblock = sBlock.getRelative(BlockFace.DOWN);
		Block movingstepsup = changeblock.getRelative(facingdirection);
		Block movingstepsdown = movingstepsup.getRelative(BlockFace.DOWN);
		
		int id;
		byte data;
		try{
			
			id = Integer.parseInt(s.getLine(2));
			data = Byte.parseByte(s.getLine(3));
			
		} catch (Exception e){
			interactor.sendMessage("Your boarding ramp is built wrong. Check the instructions again.");
			return false;
		}
		
		changeblock.setTypeIdAndData(id, data, true);
		if(movingstepsup.getType() == Material.AIR){
			movingstepsup.setTypeIdAndData(movingstepsdown.getTypeId(), movingstepsdown.getData(), true);
			movingstepsdown.setType(Material.AIR);
		}
		
		s.setLine(2, "");
		s.setLine(3, "");
		s.update();
		return true;
	}
	@SuppressWarnings("deprecation")
	public static BlockFace getFacingBlockFace(Sign s){
		Block sBlock = s.getBlock();
		int data = sBlock.getData();
		switch(data){
			case 2:
				return BlockFace.NORTH;
			case 3:
				return BlockFace.SOUTH;
			case 4:
				return BlockFace.WEST;
			case 5:
				return BlockFace.EAST;
			default:
				return null;
		}
	}
}
