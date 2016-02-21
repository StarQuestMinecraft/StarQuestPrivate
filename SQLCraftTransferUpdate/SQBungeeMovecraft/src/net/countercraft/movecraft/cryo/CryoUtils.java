package net.countercraft.movecraft.cryo;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import static org.bukkit.block.BlockFace.*;

public class CryoUtils {

	public static final String KEY_LINE = "{" + ChatColor.AQUA + "Cryo Pod" + ChatColor.BLACK + "}";

	/*
	 * boolean isCryoTybe(Sign s){ if(s.getLine(0).equals(KEY_LINE)){ Block b =
	 * s.getBlock(); if(test(b, BlockFace.UP)&& test(b, BlockFace.EAST) &&
	 * test(b, BlockFace.WEST) && test(b, BlockFace.SOUTH) && test(b,
	 * BlockFace.NORTH)){ Block b2 = s.getBlock().getRelative(BlockFace.DOWN);
	 * if(test(b2, BlockFace.DOWN)&& test(b2, BlockFace.EAST) && test(b2,
	 * BlockFace.WEST) && test(b2, BlockFace.SOUTH) && test(b2,
	 * BlockFace.NORTH)){ return true; } } } return false; }
	 */

	public static void removeBlockAtCryoSpawn(Location spawn) {
		Block sblock = spawn.getBlock();

		if (!(sblock.getType() == Material.AIR || sblock.getType() == Material.WALL_SIGN)) {
			sblock.setType(Material.AIR);
		}
	}

	static boolean isWoodDoor(Material m) {
		switch (m) {
		case WOODEN_DOOR:
		case ACACIA_DOOR:
		case BIRCH_DOOR:
		case SPRUCE_DOOR:
		case JUNGLE_DOOR:
		case DARK_OAK_DOOR:
			return true;
		default:
			return false;
		}
	}

	static boolean isCryoTube(Sign s) {
		Block[] windows = getWindows(s.getBlock());
		Block[] frame = getFrame(s.getBlock());

		byte primaryWindowColor = -1;

		for (Block b : windows) {
			if (b.getType() == Material.STAINED_GLASS || b.getType() == Material.GLASS) {
				if (primaryWindowColor > 0) {
					if (b.getData() != primaryWindowColor)
						return false;
				} else {
					primaryWindowColor = b.getData();
				}
			} else if (isWoodDoor(b.getType())) {
				continue;
			} else {
				return false;
			}
		}
		Material primaryFrameType = Material.AIR;
		for (Block b : frame) {
			if (primaryFrameType == Material.AIR) {
				primaryFrameType = b.getType();
			}
			if (primaryFrameType != b.getType()) {
				return false;
			}
		}

		if (primaryFrameType.isSolid())
			return true;
		return false;
	}

	/*
	 * private boolean test(Block b, BlockFace relative){ Block rel =
	 * b.getRelative(relative); Material type = rel.getType(); byte data =
	 * rel.getData(); if(type == Material.SMOOTH_BRICK && data == 3){ return
	 * true; } if(type == Material.STAINED_GLASS && data == 7){ return true; }
	 * return false; }
	 */

	private static Block[] getWindows(Block b) {
		Block[] retval = new Block[10];

		// sides of the sign
		retval[0] = b.getRelative(NORTH);
		retval[1] = b.getRelative(EAST);
		retval[2] = b.getRelative(WEST);
		retval[3] = b.getRelative(SOUTH);

		// sides of the block below it
		Block d = b.getRelative(DOWN);

		retval[4] = d.getRelative(NORTH);
		retval[5] = d.getRelative(EAST);
		retval[6] = d.getRelative(WEST);
		retval[7] = d.getRelative(SOUTH);

		// top and bottom
		retval[8] = b.getRelative(UP);
		retval[9] = d.getRelative(DOWN);

		return retval;
	}

	private static Block[] getFrame(Block b) {
		Block[] retval = new Block[16];
		// sides of the top block
		Block t = b.getRelative(UP);
		retval[0] = t.getRelative(EAST);
		retval[1] = t.getRelative(WEST);
		retval[2] = t.getRelative(NORTH);
		retval[3] = t.getRelative(SOUTH);

		// edges of main block
		retval[4] = b.getRelative(NORTH_EAST);
		retval[5] = b.getRelative(NORTH_WEST);
		retval[6] = b.getRelative(SOUTH_EAST);
		retval[7] = b.getRelative(SOUTH_WEST);

		// edges of the block below it
		Block d = b.getRelative(DOWN);
		retval[8] = d.getRelative(NORTH_EAST);
		retval[9] = d.getRelative(NORTH_WEST);
		retval[10] = d.getRelative(SOUTH_EAST);
		retval[11] = d.getRelative(SOUTH_WEST);

		// sides of the very bottom block
		Block dd = d.getRelative(DOWN);
		retval[12] = dd.getRelative(EAST);
		retval[13] = dd.getRelative(WEST);
		retval[14] = dd.getRelative(NORTH);
		retval[15] = dd.getRelative(SOUTH);
		return retval;
	}
}
