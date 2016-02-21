package net.countercraft.movecraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class GunUtils {
	
	private static WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	private static ProtectedRegion spawn = null;
	@SuppressWarnings("deprecation")
	public static Block getBlockBehind(Block piston){
		Byte data = piston.getData();
		switch(data){
		case 2:
			return piston.getRelative(BlockFace.SOUTH);
		case 3:
			return piston.getRelative(BlockFace.NORTH); 
		case 4:
			return piston.getRelative(BlockFace.EAST);
		case 5:
			return piston.getRelative(BlockFace.WEST);
		default:
			return null;
		}
	}
	@SuppressWarnings("deprecation")
	public static BlockFace getFacing(Block piston){
		Byte data = piston.getData();
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
	public static int getIntegerDirection(BlockFace face){
		switch(face){
		case NORTH:
			return 2;
		case SOUTH:
			return 3;
		case EAST:
			return 5;
		case WEST:
			return 4;
		default:
			return 20;
		}
	}
	public static Vector getFireBallVelocity(BlockFace facing){
		switch (facing){
		case NORTH:
			return new Vector(0, 0, -200);
		case SOUTH:
			return new Vector(0, 0, 200);
		case WEST:
			return new Vector(-200, 0, 0);
		case EAST:
			return new Vector(200, 0, 0);
		default:
			return null;
		}
	}
	public static BlockFace yawToFace(float yaw){
		BlockFace change = yawToFace2(yaw, false);
		switch (change){
		case NORTH:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.NORTH;
		case WEST:
			return BlockFace.EAST;
		case EAST:
			return BlockFace.WEST;
		default:
			return null;
		}
	}
    private static BlockFace yawToFace2(float yaw, boolean useSubCardinalDirections) {
    	BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
        BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
        
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }
    
    public static boolean isInSpawn(Location l){
    	if(Bukkit.getServerName().equals("Regalis")){
    		if(spawn == null){
    			spawn = wg.getRegionManager(Bukkit.getWorld("Regalis")).getRegion("OriginStation");
    			if(spawn == null){
    				return false;
    			}
    		}
    		return (spawn.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
    	}
    	return false;
    }
}
