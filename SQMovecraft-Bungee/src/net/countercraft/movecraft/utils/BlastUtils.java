package net.countercraft.movecraft.utils;
import java.util.HashMap;

import org.bukkit.Material;


public class BlastUtils {
	
	private static HashMap<Material, Double> resistances;
	
	public static double getBlastResistance(int id){
		Material m = Material.getMaterial(id);
		if(m == null){
			System.out.println("ERROR: no material found with id " + id);
			return 4;
		}
		return getBlastResistance(m);
	}
	public static double getBlastResistance(Material m){

		if(resistances == null){
			resistances = new HashMap<Material, Double>();
			for(int i = 0; i < 255; i++){
				Material m2 = Material.getMaterial(i);
				if(m2 != null){
					double resistance = getBlastResistanceSwitch(m2);
					System.out.println("Loaded resistance " + resistance + " for material " + m2);
					resistances.put(m2, getBlastResistanceSwitch(m2));
				}
			}
		}
		Double resistance = resistances.get(m);
		if(resistance == null){
			return getBlastResistanceSwitch(m);
		} else {
			return resistance;
		}
	}
	
	public static double getBlastResistanceSwitch(Material type){
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
		case DIAMOND_BLOCK:
		case EMERALD_BLOCK:
		case IRON_BLOCK:
		case REDSTONE_BLOCK:
		case CLAY_BRICK:
		case STAINED_CLAY:
		case BRICK_STAIRS:
		case HARD_CLAY:
		case STONE:
		case COBBLESTONE:
		case COBBLESTONE_STAIRS:
		case COBBLE_WALL:
		case IRON_FENCE:
		case JUKEBOX:
		case MOSSY_COBBLESTONE:
		case NETHER_BRICK:
		case NETHER_FENCE:
		case NETHER_BRICK_STAIRS:
		case PRISMARINE:
		case SMOOTH_BRICK:
		case SMOOTH_STAIRS:
		case STEP:
		case DOUBLE_STEP:
		case PURPUR_BLOCK:
		case PURPUR_SLAB:
		case PURPUR_DOUBLE_SLAB:
		case PURPUR_PILLAR:
		case PURPUR_STAIRS:
			return 30;
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
		case QUARTZ_STAIRS:
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
