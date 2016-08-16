package net.countercraft.movecraft.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;


public class HPUtils {
	
	private static HashMap<Material, Double> hps = new HashMap<Material, Double>();
	private static double torpedoPower = 300;
	
	public static void setup(FileConfiguration config) {
		
		if (config.contains("hp")) {
			
			for (String group : config.getConfigurationSection("hp").getKeys(false)) {
				
				String path = "hp." + group + ".";
				
				try {
					
					List<Material> materials = new ArrayList<Material>();
					
					List<Integer> ids = config.getIntegerList(path + "ids");
					
					for (Integer id : ids) {
						
						materials.add(Material.getMaterial(id));
						
					}
					
					double hp = config.getDouble(path + "hp");
					
					for (Material material : materials) {
						
						if (material != null) {
							
							hps.put(material, hp);
							
						} else {
							
							System.out.print("ERROR: Material is null");
							
						}
						
					}

				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
			}
			
		} else {
			
			System.out.println("ERROR: hp.yml is not setup properly");
			
		}
		
		if (config.contains("torpedoPower")) {
			
			torpedoPower = config.getDouble("torpedoPower");
			
		} else {
			
			System.out.println("ERROR: torpedoPower is not specified in the hp.yml");
			
		}
		
	}
	
	public static double getHP(int id){
		Material m = Material.getMaterial(id);
		if(m == null){
			System.out.println("ERROR: no material found with id " + id);
			return 1;
		}
		return getHP(m);
	}
	public static double getHP(Material m){

		if (hps.containsKey(m)) {
			
			return hps.get(m);
			
		} else {
			
			return 1;
			
		}
		
	}
	
	/*public static double getHPSwitch(Material type){
		switch(type){
		case BARRIER:
			return 18000003;
		case BEDROCK:
		case COMMAND:
		case ENDER_PORTAL:
		case ENDER_PORTAL_FRAME:
			return 18000000;
		case ANVIL
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
			return 1;
		}
	}*/

	public static double getTorpedoPower() {
		
		return torpedoPower;
		
	}

}
