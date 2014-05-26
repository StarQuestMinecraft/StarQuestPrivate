package net.countercraft.movecraft.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class BoardingRampUtils {
	@SuppressWarnings("deprecation")
	public static void openRamp(Sign s){
		BlockFace facingdirection = SignUtils.getFacingBlockFace(s);
		
		Block sBlock = s.getBlock();
		Block changeblock = sBlock.getRelative(BlockFace.DOWN);
		Block movingstepsup = changeblock.getRelative(facingdirection);
		Block movingstepsdown = movingstepsup.getRelative(BlockFace.DOWN);
		
		s.setLine(2, changeblock.getTypeId() + "");
		s.setLine(3, changeblock.getData() + "");
		s.update();
		
		movingstepsdown.setTypeIdAndData(movingstepsup.getTypeId(), movingstepsup.getData(), true);
		movingstepsup.setType(Material.AIR);
		
		changeblock.setTypeIdAndData(0, (byte) 0, false);
		
		
	}
	@SuppressWarnings("deprecation")
	public static boolean closeRamp(Sign s, Player interactor){
		BlockFace facingdirection = SignUtils.getFacingBlockFace(s);
		
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
		movingstepsup.setTypeIdAndData(movingstepsdown.getTypeId(), movingstepsdown.getData(), true);
		movingstepsdown.setType(Material.AIR);
		
		s.setLine(2, "");
		s.setLine(3, "");
		s.update();
		return true;
	}
}
