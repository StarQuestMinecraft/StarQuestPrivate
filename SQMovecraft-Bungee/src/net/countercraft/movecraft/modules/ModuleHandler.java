package net.countercraft.movecraft.modules;

import net.countercraft.movecraft.utils.Rotation;
import net.countercraft.movecraft.utils.SignUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ModuleHandler {
	private static ModuleHandler instance;
	
	static{
		instance = new ModuleHandler();
	}
	
	public ModuleHandler getInstance(){
		return instance;
	}
	
	public static ModuleType detectModule(Block b, BlockFace dir, BlockFace left, BlockFace right, BlockFace back){
		
		if(isCannon(b)) return ModuleType.CANNON;
		if(isLandingGear(b)) return ModuleType.LANDING_GEAR;
		if(isTurret(b)) return ModuleType.TURRET;
		if(isTorpedo(b, dir, left, right, back)) return ModuleType.TORPEDO;
		if(isEngineSmall(b, dir, left, right, back)) return ModuleType.ENGINE_SMALL;
		if(isEngineMedium(b, dir, left, right, back)) return ModuleType.ENGINE_MEDIUM;
		if(isEngineLarge(b, dir, left, right, back)) return ModuleType.ENGINE_LARGE;
		return ModuleType.NONE;
	}
	
	private static boolean isEngineSmall(Block b, BlockFace dir, BlockFace left, BlockFace right, BlockFace back){
		return(
		isBlockSurroundings(b, left, right)
		&& isGlowstone(b.getRelative(back))
		&& isBlockSurroundings(b.getRelative(back), left, right)
		&& b.getRelative(back).getRelative(back).isEmpty()
		&& isBlockSurroundings(b.getRelative(back).getRelative(back), left, right));
	}
	
	private static boolean isEngineMedium(Block b, BlockFace dir, BlockFace left, BlockFace right, BlockFace back){
		return false;
	}
	
	private static boolean isEngineLarge(Block b, BlockFace dir, BlockFace left, BlockFace right, BlockFace back){
		return false;
	}
	
	private static boolean isTorpedo(Block b, BlockFace dir, BlockFace left, BlockFace right, BlockFace back){
		return false;
	}
	
	private static boolean isLandingGear(Block b){
		return false;
	}
	
	private static boolean isTurret(Block b){
		return false;
	}
	private static boolean isCannon(Block b){
		if (b.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE){
			return true;
		} else if(b.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE) {
			return true;
		} else if(b.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE) {
			return true;
		} else if(b.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE) {
			return true;
		}
		return false;
	}
	@SuppressWarnings("deprecation")
	private static boolean isBlock(Block b){
		return !(b.getTypeId() == 0);
	}
	
	private static boolean isBlockSurroundings(Block b, BlockFace left, BlockFace right){
		return (isBlock(b.getRelative(right))
				&& isBlock(b.getRelative(left))
				&& isBlock(b.getRelative(BlockFace.UP))
				&& isBlock(b.getRelative(BlockFace.DOWN)));
	}
	
	private static boolean isGlowstone(Block b){
		return b.getType() == Material.GLOWSTONE;
	}
}
