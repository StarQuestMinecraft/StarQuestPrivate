package net.countercraft.movecraft.utils;

import java.util.HashMap;
import java.util.logging.Level;

import net.countercraft.movecraft.bungee.RepeatTryServerJumpTask;
import net.countercraft.movecraft.craft.Craft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LocationUtils {
	private static HashMap<String, Location> planets = new HashMap<String, Location>();
	private static HashMap<String, StargateData> stargates = new HashMap<String, StargateData>();
	private static String SYSTEM;
	public static void setUp(FileConfiguration cfg){
		SYSTEM = cfg.getString("System");
		if(SYSTEM == null){
			Bukkit.getLogger().log(Level.SEVERE, "No System setting found in config! Severe!");
		}
		else if(SYSTEM.equals("Regalis")){
			planets.put("Boskevine", locationFromConfig(cfg, "Boskevine"));
			planets.put("Quavara", locationFromConfig(cfg, "Quavara"));
			planets.put("Kelakaria", locationFromConfig(cfg, "Kelakaria"));
			planets.put("Boletarian", locationFromConfig(cfg, "Boletarian"));
			
			stargates.put("Defalos", stargateFromConfig(cfg, "Defalos"));
			stargates.put("Digitalia", stargateFromConfig(cfg, "Digitalia"));
			
		} else if(SYSTEM.equals("Defalos")){
			planets.put("Krystallos", locationFromConfig(cfg, "Krystallos"));
			planets.put("Emera", locationFromConfig(cfg, "Emera"));
			//planets.put("Drakos", locationFromConfig(cfg, "Drakos"));
			planets.put("Acualis", locationFromConfig(cfg, "Acualis"));
			
			stargates.put("Regalis", stargateFromConfig(cfg, "Regalis"));
			stargates.put("Digitalia", stargateFromConfig(cfg, "Digitalia"));
			
		} else if(SYSTEM.equals("Digitalia")){
			//planets.put("Inaris", locationFromConfig(cfg, "Inaris"));
			//planets.put("AsteroidBelt", locationFromConfig(cfg, "AsteroidBelt"));
			planets.put("Valadro", locationFromConfig(cfg, "Valadro"));
			planets.put("Iffrizar", locationFromConfig(cfg, "Iffrizar"));
			planets.put("Ceharram", locationFromConfig(cfg, "Ceharram"));
			
			stargates.put("Defalos", stargateFromConfig(cfg, "Defalos"));
			stargates.put("Regalis", stargateFromConfig(cfg, "Regalis"));
		}
	}
	
	public static boolean spaceCheck(String worldname){
		return(worldname.equals("Regalis") || worldname.equals("Defalos") || worldname.equals("Digitalia"));
	}
	
	public static boolean spaceCheck(World w){
		return spaceCheck(w.getName());
	}
	
	public static boolean spaceCheck(Location l){
		return spaceCheck(l.getWorld().getName());
	}
	
	public static boolean spaceCheck(Player p){
		String wName = p.getLocation().getWorld().getName();
		if(wName.equals("AsteroidBelt")){
			return true;
		}
		return spaceCheck(wName);
	}
	public static boolean spaceCheck(Player p, boolean includeAsteroidBelt){
		if(includeAsteroidBelt) return spaceCheck(p);
		else return spaceCheck(p.getLocation());
	}
	
	public static World getSpaceWorld(){
		return Bukkit.getWorld(SYSTEM);
	}
	
	private static Location locationFromConfig(FileConfiguration cfg, String key){
		int x = cfg.getInt(key + ".Xcoord");
		int y = cfg.getInt(key + ".Ycoord");
		int z = cfg.getInt(key + ".Zcoord");
		//yaw represents stargate orientation
		return new Location(getSpaceWorld(), x, y, z);
	}
	private static StargateData stargateFromConfig(FileConfiguration cfg, String key){
		System.out.println("loading stargate: " + key);
		int x = cfg.getInt(key + ".Xcoord");
		int y = cfg.getInt(key + ".Ycoord");
		int z = cfg.getInt(key + ".Zcoord");
		Location myLoc = new Location(getSpaceWorld(), x, y, z);
		int tx = cfg.getInt(key + ".XcoordTarget");
		int ty = cfg.getInt(key + ".YcoordTarget");
		int tz = cfg.getInt(key + ".ZcoordTarget");
		Location targLoc = new Location(getSpaceWorld(), tx, ty, tz);
		int orientation = cfg.getInt(key + ".orientation");
		return new StargateData(orientation, myLoc, targLoc);
	}
	
	public static String locationCheck(Player pilot){
		Location loc = pilot.getLocation();
		if(spaceCheck(loc)){
			for(String s : planets.keySet()){
				if(check(planets.get(s), loc)){
					pilot.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering the atmosphere of " + s + "!");
					return s;
				}
			}
		}
		return null;
	}
	
	public static Location locationOfPlanet(String planet){
		return planets.get(planet);
	}
	
	private static boolean check(Location planet, Location pilot){
		if(planet.getX() - 100 < pilot.getX() && pilot.getX() < planet.getX() + 100){
			if(planet.getZ() - 100 < pilot.getZ() && pilot.getZ() < planet.getZ() + 100){
				return true;
			}
		}
		return false;
	}
	
	public static Location getWarpLocation(String planet, Location playerLoc){
		for(String s : planets.keySet()){
			if(s.equals(planet)){
				Location planetLoc = planets.get(s);
				double angle = getAngleFromOriginTo(playerLoc);
				Location target = getSpawnLocationFromAngle(angle, planetLoc, 120);
				return target;
			}
		}
		return null;
	}
	
	public static double getAngleFromOriginTo(Location loc){
		return Math.atan(loc.getZ() / loc.getX());
	}
	
	public static double getAngleFromGivenPointTo(Location point, Location loc){
		if(point == null || loc == null) return 0;
		double x = loc.getX() - point.getX();
		double z = loc.getZ() - point.getZ();
		return Math.atan(z / x);
	}
	
	public static Location getSpawnLocationFromAngle(double angle, Location origin, int distance){
		double x = origin.getX() + (Math.cos(angle) * distance);
		double z = origin.getZ() + (Math.sin(angle) * distance);
		return new Location(origin.getWorld(), x, 100, z);
	}
	
	public static RepeatTryServerJumpTask checkStargateJump(Player p, Craft c){
		Location loc = p.getLocation();
		for(String s: stargates.keySet()){
			StargateData d = stargates.get(s);
			if(stargateCheck(d, loc)){
				p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering slipgate to " + s + "!");
				return new RepeatTryServerJumpTask(p, c, s, d.targetLocation.getBlockX(), d.targetLocation.getBlockY(), d.targetLocation.getBlockZ());
			}
		}
		return null;
	}
	
	private static boolean stargateCheck(StargateData d, Location pilot) {
		Location gate = d.startLocation;
		if(d.orientation == 0){ //north/south orientation
			//stargate bounding boxes are 24 radius across and vertical and 50 on either side to the front / back 
			if(gate.getX() - 24 < pilot.getX() && pilot.getX() < gate.getX() + 24){
				if(gate.getY() - 24 < pilot.getY() && pilot.getY() < gate.getY() + 24){
					if(gate.getZ() - 50 < pilot.getZ() && pilot.getZ() < gate.getZ() + 50){
						return true;
					}
				}
			}
		} else {
			if(gate.getX() - 50 < pilot.getX() && pilot.getX() < gate.getX() + 50){
				if(gate.getY() - 24 < pilot.getY() && pilot.getY() < gate.getY() + 24){
					if(gate.getZ() - 24 < pilot.getZ() && pilot.getZ() < gate.getZ() + 24){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static String slipWarpCheck(int warpX, int warpZ){
		//positive x is east. Positive z is south.
		if(SYSTEM.equals("Regalis")){
			//Defalos is to the south of Regalis
			if(warpZ > 5000) return "Defalos";
			//Digitalis is to the east of Regalis
			if(warpX > 5000) return "Digitalia";
		} else if(SYSTEM.equals("Digitalia")){
			//Regalis is to the west of Digitalia
			if(warpX < -5000) return "Regalis";
			//Defalos is to the south of Digitalia
			if(warpZ > 5000) return "Defalos";
		} else if(SYSTEM.equals("Defalos")){
			//Regalis is to the north of Defalos
			if(warpZ < -5000) return "Regalis";
			//Digitalis is to the east of Defalos
			if(warpX > 5000) return "Defalos";
		}
		return null;
	}
	
	public static int getSlipCoordX(String systarget, int coordX){
		if(SYSTEM.equals("Regalis")){
			if(systarget.equals("Defalos")){
				return coordX;	
			} else {
				return -4500;
			}
		}else if(SYSTEM.equals("Defalos")){
			if(systarget.equals("Regalis")){
				return coordX;	
			} else {
				return 4500;
			}
		}else{
			if(systarget.equals("Regalis")){
				return 4500;	
			} else {
				return coordX;
			}
		}
	}
	public static int getSlipCoordZ(String system, int coordZ){
		if(SYSTEM.equals("Regalis")){
			if(system.equals("Defalos")){
				return coordZ;	
			} else {
				return -4500;
			}
		}else if(SYSTEM.equals("Defalos")){
			if(system.equals("Regalis")){
				return coordZ;	
			} else {
				return 4500;
			}
		}else{
			if(system.equals("Regalis")){
				return 4500;	
			} else {
				return coordZ;
			}
		}
	}
	public static String getSystem(){
		return SYSTEM;
	}
}
