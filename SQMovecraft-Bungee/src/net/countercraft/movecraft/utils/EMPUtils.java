package net.countercraft.movecraft.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.countercraft.movecraft.async.detection.SaveableBlock;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.database.StarshipData;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.shield.Compression;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class EMPUtils {
	
	private final static String[] explodesigns = {ChatColor.BLUE + "AUTOPILOT", "\\  ||  /"};
	private static final List<String> EXPLODE_SIGNS = Arrays.asList(explodesigns);
	public static HashMap<UUID, StarshipData> dataMap = new HashMap<UUID, StarshipData>();

	public static boolean detonateEMP(Block hit){
		MovecraftLocation bloc = new MovecraftLocation(hit.getX(), hit.getY(), hit.getZ());
		Craft[] craftsInWorld = CraftManager.getInstance().getCraftsInWorld(hit.getWorld());
		if(craftsInWorld != null){
			for(Craft c : craftsInWorld){
				MovecraftLocation[] blocks = c.getBlockList();
				for(MovecraftLocation l : blocks){
					if(bloc.equals(l)){
						emp(blocks, c);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//sends an EMP charge through a ship
	private static void emp(MovecraftLocation[] blocks, Craft c){
		Player pilot = c.pilot;
		SaveableBlock[] lb = new SaveableBlock[c.getBlockList().length];
		for(int i = 0; i < c.getBlockList().length; i++){
			lb[i] = new SaveableBlock(c.getW(), c.getBlockList()[i]);
		}
		dataMap.put(pilot.getUniqueId(), new StarshipData(lb, c.getType().getCraftName(), pilot.getUniqueId()));
		// first remove the craft
		c.pilot.sendMessage(ChatColor.RED + "Your ship has been hit by an EMP!");
		CraftManager.getInstance().removeCraft(c);
		World w = c.getW();
		for(MovecraftLocation l : blocks){
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
				Sign s = (Sign) b.getState();
				String l1 = s.getLine(0);
				if(InteractListener.getCraftTypeFromString(l1) != null){
					nukeCraftSign(s, pilot);
				} else if(EXPLODE_SIGNS.contains(l1)){
					nukeBlock(b);
				}
			} else if(Math.random() < 0.25){
				smoke(b);
			}
		}
		
	}
	
	
	private static void smoke(Block b){
		b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 0);
		b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10, 5);
	}
	
	private static void nukeBlock(Block b){
		b.breakNaturally();
		smoke(b);
	}
	
	private static void nukeCraftSign(Sign s, Player pilot){
		String[] lines = new String[]{
			ChatColor.RED + "EMP shorted",
			ChatColor.RED + "control",
			ChatColor.RED + "sign",
			ChatColor.RED + Compression.uuidToStr15(pilot.getUniqueId()),
		};
		for(int i = 0; i < lines.length; i++){
			s.setLine(i, lines[i]);
		}
		s.update();
	}
}
