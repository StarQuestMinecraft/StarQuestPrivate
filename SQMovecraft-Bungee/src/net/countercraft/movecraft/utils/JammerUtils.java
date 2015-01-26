package net.countercraft.movecraft.utils;

import java.util.ArrayList;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class JammerUtils {
	public static boolean checkForAndDisableJammer(Block b) {
		Material type = b.getType();

		if (type == Material.QUARTZ_BLOCK || type == Material.IRON_BLOCK || type == Material.QUARTZ_STAIRS) {
			Block[] blocks = BlockUtils.getEdges(b, true, false);
			for (Block b2 : blocks) {
				if (b2.getType() == Material.SPONGE) {
					Block jmr = checkJammer(b2);
					if (jmr != null) {
						disableJammer(jmr);
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Block checkJammer(Block sponge) {
		Block[] signs = new Block[] { sponge.getRelative(0, -4, 0), sponge.getRelative(0, 4, 0), sponge.getRelative(4, 0, 0), sponge.getRelative(-4, 0, 0), sponge.getRelative(0, 0, 4),
				sponge.getRelative(0, 0, -4) };
		for (Block sign : signs) {
			if (sign.getType() == Material.WALL_SIGN) {
				Sign s = (Sign) sign.getState();
				if (isJammerSign(s)) {
					if (getJammerSignState(s)) {
						return sign;
					}
				}
			}
		}
		return null;
	}

	public static void disableJammer(Block s) {
		MovecraftLocation bLoc = new MovecraftLocation(s.getX(), s.getY(), s.getZ());
		Craft[] craftsInWorld = CraftManager.getInstance().getCraftsInWorld(s.getWorld());
		if (craftsInWorld != null) {
			for (Craft c : craftsInWorld) {
				MovecraftLocation[] blocks = c.getBlockList();
				for (MovecraftLocation l : blocks) {
					if (bLoc.equals(l)) {
						disableJammer(s, c);
						return;
					}
				}
			}
		}
	}

	public static void setupJammerSign(Sign s, Player p) {
		if (checkJammerConstruct(s)) {
			s.setLine(0, ChatColor.AQUA + "JAMMING");
			s.setLine(1, ChatColor.AQUA + "DEVICE");
			s.setLine(2, "[" + ChatColor.RED + "DISABLED" + ChatColor.BLACK + "]");
			s.update();
		} else {
			p.sendMessage("Improperly built jamming device.");
		}
	}

	private static boolean checkJammerConstruct(Sign s) {
		Block sBlock = s.getBlock();
		Block[] sponges = new Block[] { sBlock.getRelative(0, -4, 0), sBlock.getRelative(0, 4, 0), sBlock.getRelative(4, 0, 0), sBlock.getRelative(-4, 0, 0), sBlock.getRelative(0, 0, 4),
				sBlock.getRelative(0, 0, -4) };

		for (Block sponge : sponges) {
			if (sponge.getType() != Material.SPONGE)
				continue;
			Block[] edges = BlockUtils.getEdges(sponge, true, false);
			for (Block b : edges) {
				Material m = b.getType();
				if (m != Material.IRON_FENCE && m != Material.IRON_BLOCK && m != Material.QUARTZ_STAIRS) {
					continue;
				}
			}
			return true;
		}
		return false;
	}

	public static void disableJammer(Block b, Craft c) {
		c.isJamming = false;
		Sign s = (Sign) b.getState();
		s.setLine(0, ChatColor.AQUA + "JAMMING");
		s.setLine(1, ChatColor.AQUA + "DEVICE");
		s.setLine(2, "[" + ChatColor.RED + "DISABLED" + ChatColor.BLACK + "]");
		s.update();
		c.pilot.sendMessage("Your jammer has been disabled.");
	}

	public static void enableJammer(Block b, Craft c) {
		c.isJamming = true;
		Sign s = (Sign) b.getState();
		s.setLine(0, ChatColor.AQUA + "JAMMING");
		s.setLine(1, ChatColor.AQUA + "DEVICE");
		s.setLine(2, "[" + ChatColor.GREEN + "ENABLED" + ChatColor.BLACK + "]");
		s.update();
		c.pilot.sendMessage("Your jammer has been enabled.");
	}

	public static boolean isJammerSign(Sign s) {
		return (s.getLine(0).equals(ChatColor.AQUA + "JAMMING") && s.getLine(1).equals(ChatColor.AQUA + "DEVICE"));
	}

	public static boolean getJammerSignState(Sign s) {
		String l2 = s.getLine(2);
		return l2.equals("[" + ChatColor.GREEN + "ENABLED" + ChatColor.BLACK + "]");
	}

	public static void toggleJammer(Sign s, Player p) {
		boolean state = getJammerSignState(s);

		Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if (c == null) {
			p.sendMessage("You are not flying this, you cannot use the jammer.");
			return;
		}
		if (!state) {
			if (checkJammerConstruct(s)) {
				enableJammer(s.getBlock(), c);
				p.sendMessage("Enabled Jammer!");
			} else {
				p.sendMessage("Improperly built Jammer!");
			}
		} else {
			disableJammer(s.getBlock(), c);
			p.sendMessage("Disabled Jammer!");
		}
	}

	public static void disableJammer(Craft c, ArrayList<MovecraftLocation> signLocations) {
		if (!c.isJamming)
			return;
		for (MovecraftLocation l : signLocations) {
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) b.getState();
			if (isJammerSign(s)) {
				disableJammer(b, c);
				break;
			}
		}
	}
}
