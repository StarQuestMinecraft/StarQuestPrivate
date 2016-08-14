package net.countercraft.movecraft.utils;
import java.util.HashMap;

import org.bukkit.Material;


public class HPUtils {
	
	private static HashMap<Material, Double> hps;
	
	public static double getHP(int id){
		Material m = Material.getMaterial(id);
		if(m == null){
			System.out.println("ERROR: no material found with id " + id);
			return 4;
		}
		return getHP(m);
	}
	public static double getHP(Material m){

		if(hps == null){
			hps = new HashMap<Material, Double>();
			for(int i = 0; i < 255; i++){
				Material m2 = Material.getMaterial(i);
				if(m2 != null){
					double resistance = getHPSwitch(m2);
					System.out.println("Loaded hp " + resistance + " for material " + m2);
					hps.put(m2, getHPSwitch(m2));
				}
			}
		}
		Double resistance = hps.get(m);
		if(resistance == null){
			return getHPSwitch(m);
		} else {
			return resistance;
		}
	}
	
	public static double getHPSwitch(Material type){
		switch(type){
		case BARRIER:
			return 18000003;
		case BEDROCK:
		case COMMAND:
		case ENDER_PORTAL:
		case ENDER_PORTAL_FRAME:
			return 18000000;
		case ANVIL:
		case ENCHANTMENT_TABLE:
		case OBSIDIAN:
			return 6000;
		case ENDER_CHEST:
			return 3000;
		case WATER:
		case LAVA:
		case STATIONARY_LAVA:
		case STATIONARY_WATER:
			return 500;
		case DRAGON_EGG:
			return 45;
		case ENDER_STONE:
			return 45;
		case COAL_BLOCK:
			return 40;
		case DIAMOND_BLOCK:
			return 40;
		case EMERALD_BLOCK:
			return 40;
		case IRON_BLOCK:
			return 40;
		case REDSTONE_BLOCK:
			return 40;
		case CLAY_BRICK:
		case STAINED_CLAY:
			return 17.5;
		case BRICK_STAIRS:
		case HARD_CLAY:
			return 17.5;
		case STONE:
		case COBBLESTONE:
		case COBBLESTONE_STAIRS:
		case COBBLE_WALL:
		case IRON_FENCE:
		case JUKEBOX:
		case MOSSY_COBBLESTONE:
		case NETHER_BRICK:
			return 40;
		case NETHER_FENCE:
		case NETHER_BRICK_STAIRS:
			return 40;
		case PRISMARINE:
		case SMOOTH_BRICK:
		case SMOOTH_STAIRS:
		case STEP:
			return 40;
		case DOUBLE_STEP:
			return 40;
		case PURPUR_BLOCK:
			return 40;
		case PURPUR_SLAB:
			return 40;
		case PURPUR_DOUBLE_SLAB:
			return 40;
		case PURPUR_PILLAR:
			return 40;
		case PURPUR_STAIRS:
			return 40;
		case IRON_DOOR_BLOCK:
		case IRON_TRAPDOOR:
		case MOB_SPAWNER:
			return 30;
		case WEB:
			return 20;
		case DISPENSER:
		case DROPPER:
		case FURNACE:
			return 17.5;
		case BEACON:
		case COAL_ORE:
		case COCOA:
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case FENCE:
		case FENCE_GATE:
		case GOLD_ORE:
		case HOPPER:
		case IRON_ORE:
		case LAPIS_BLOCK:
			return 40;
		case LAPIS_ORE:
		case QUARTZ_ORE:
		case REDSTONE_ORE:
		case TRAP_DOOR:
		case WOOD:
		case WOOD_STEP:
		case WOOD_DOUBLE_STEP:
			return 15;
		case CHEST:
		case WORKBENCH:
		case TRAPPED_CHEST:
			return 12.5;
		case CAULDRON:
		case LOG:
			return 10;
		case BOOKSHELF:
			return 7.5;
		case BANNER:
		case JACK_O_LANTERN:
		case MELON:
		case SKULL:
		case PUMPKIN:
		case WALL_SIGN:
		case SIGN_POST:
			return 5;
		case QUARTZ_BLOCK:
			return 40;
		case QUARTZ_STAIRS:
			return 40;
		case NOTE_BLOCK:
		case RED_SANDSTONE:
		case RED_SANDSTONE_STAIRS:
		case SANDSTONE:
		case SANDSTONE_STAIRS:
		case WOOL:
			return 4;
		default:
			return 0;
		}
	}
}
