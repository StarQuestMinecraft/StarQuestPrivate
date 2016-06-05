package net.countercraft.movecraft.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.task.RepeatTryServerJumpTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import us.higashiyama.george.SQSpace.SQSpace;

public class LocationUtils {
	public static HashMap<String, Location> planets = new HashMap<String, Location>();
	public static HashMap<String, StargateData> stargates = new HashMap<String, StargateData>();
	public static String SYSTEM;

	public static void setUp(FileConfiguration cfg) {
		SYSTEM = cfg.getString("System");
		if (SYSTEM == null) {
			Bukkit.getLogger().log(Level.SEVERE, "No System setting found in config! Severe!");
		} else if (SYSTEM.equals("AratorSystem")) {
			planets.put("Arator", locationFromConfig(cfg, "Arator"));
			planets.put("Tyder", locationFromConfig(cfg, "Tyder"));
			planets.put("Jurion", locationFromConfig(cfg, "Jurion"));
			if(cfg.contains("stargates")) {
				for(String stargateName : cfg.getConfigurationSection("stargates").getKeys(false)) {
					stargates.put(stargateName, stargateFromConfig(cfg, stargateName));
				}
			}
			//stargates.put("QuillonSystem", stargateFromConfig(cfg, "QuillonSystem"));
			//stargates.put("YavarSystem", stargateFromConfig(cfg, "YavarSystem"));

		} else if (SYSTEM.equals("QuillonSystem")) {
			planets.put("Erilon", locationFromConfig(cfg, "Erilon"));
			planets.put("Quillon", locationFromConfig(cfg, "Quillon"));
			planets.put("Mardos", locationFromConfig(cfg, "Mardos"));
			
			if(cfg.contains("stargates")) {
				for(String stargateName : cfg.getConfigurationSection("stargates").getKeys(false)) {
					stargates.put(stargateName, stargateFromConfig(cfg, stargateName));
				}
			}

		} else if (SYSTEM.equals("YavarSystem")) {
			// planets.put("Inaris", locationFromConfig(cfg, "Inaris"));
			// planets.put("AsteroidBelt", locationFromConfig(cfg,
			// "AsteroidBelt"));
			planets.put("Yavar", locationFromConfig(cfg, "Yavar"));
			planets.put("Beskytt", locationFromConfig(cfg, "Beskytt"));
			planets.put("Radawii", locationFromConfig(cfg, "Radawii"));
			
			if(cfg.contains("stargates")) {
				for(String stargateName : cfg.getConfigurationSection("stargates").getKeys(false)) {
					stargates.put(stargateName, stargateFromConfig(cfg, stargateName));
				}
			}

			//stargates.put("QuillonSystem", stargateFromConfig(cfg, "QuillonSystem"));
			//stargates.put("AratorSystem", stargateFromConfig(cfg, "AratorSystem"));
		}
	}

	public static boolean spaceCheck(String worldname) {
		
		if (SQSpace.spaceWorlds.contains(worldname)) {
			
			return true;
			
		}
		
		return false;
		
	}

	public static boolean spaceCheck(World w) {
		
		if (SQSpace.spaceWorlds.contains(w.getName())) {
			
			return true;
			
		}
		
		return false;
		
	}

	public static boolean spaceCheck(Location l) {
	
		if (SQSpace.spaceWorlds.contains(l.getWorld().getName())) {
			
			return true;
			
		}
		
		return false;
		
	}

	public static boolean spaceCheck(Player p) {
		
		if (SQSpace.spaceWorlds.contains(p.getLocation().getWorld().getName())) {
			
			return true;
			
		}
		
		return false;
		
	}

	public static boolean spaceCheck(Player p, boolean includeAsteroidBelt) {
		if (includeAsteroidBelt) {
			
			if (SQSpace.spaceWorlds.contains(p.getLocation().getWorld().getName())) {
				
				return true;
				
			}
			
			return false;
			
		} else {
			
			if (SQSpace.spaceWorlds.contains(p.getLocation().getWorld().getName()) && !p.getLocation().getWorld().getName().equalsIgnoreCase("AsteroidBelt")) {
				
				return true;
				
			}
			
			return false;
			
		}
	}

	public static World getSpaceWorld() {
		return Bukkit.getWorld(SYSTEM);
	}

	private static Location locationFromConfig(FileConfiguration cfg, String key) {
		int x = cfg.getInt(key + ".Xcoord");
		int y = cfg.getInt(key + ".Ycoord");
		int z = cfg.getInt(key + ".Zcoord");
		// yaw represents stargate orientation
		return new Location(getSpaceWorld(), x, y, z);
	}

	private static StargateData stargateFromConfig(FileConfiguration cfg, String key) {
		int x = cfg.getInt("stargates." + key + ".Xcoord");
		int y = cfg.getInt("stargates." + key + ".Ycoord");
		int z = cfg.getInt("stargates." + key + ".Zcoord");
		Location myLoc = new Location(getSpaceWorld(), x, y, z);
		int tx = cfg.getInt("stargates." + key + ".XcoordTarget");
		int ty = cfg.getInt("stargates." + key + ".YcoordTarget");
		int tz = cfg.getInt("stargates." + key + ".ZcoordTarget");
		Location targLoc = new Location(getSpaceWorld(), tx, ty, tz);
		int orientation = cfg.getInt("stargates." + key + ".orientation");
		String targServer = null;
		if(cfg.getString("stargates." + key + ".server") != null) {
			targServer = cfg.getString("stargates." + key + ".server");
		}
		else {
			targServer = key;
		}
		return new StargateData(orientation, myLoc, targLoc, targServer);
	}

	public static String locationCheck(Player pilot) {
		Location loc = pilot.getLocation();
		if (spaceCheck(loc)) {
			for (String s : planets.keySet()) {
				if (check(planets.get(s), loc)) {
					pilot.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering the atmosphere of " + s + "!");
					return s;
				}
			}
		}
		return null;
	}

	public static Location locationOfPlanet(String planet) {
		return planets.get(planet);
	}

	private static boolean check(Location planet, Location pilot) {
		if (planet.getX() - 100 < pilot.getX() && pilot.getX() < planet.getX() + 100) {
			if (planet.getZ() - 100 < pilot.getZ() && pilot.getZ() < planet.getZ() + 100) {
				return true;
			}
		}
		return false;
	}

	public static Location getWarpLocation(String planet, Location playerLoc) {
		for (String s : planets.keySet()) {
			if (s.equals(planet)) {
				Location planetLoc = planets.get(s);
				double angle = getAngleFromOriginTo(playerLoc);
				Location target = getSpawnLocationFromAngle(angle, planetLoc, 120);
				return target;
			}
		}
		return null;
	}

	public static double getAngleFromOriginTo(Location loc) {
		return Math.atan2(loc.getZ(), loc.getX());
	}

	public static double getAngleFromGivenPointTo(Location point, Location loc) {
		if (point == null || loc == null)
			return 0;
		double x = loc.getX() - point.getX();
		double z = loc.getZ() - point.getZ();
		return Math.atan2(z, x);
	}

	public static Location getSpawnLocationFromAngle(double angle, Location origin, int distance) {
		double x = origin.getX() + (Math.cos(angle) * distance);
		double z = origin.getZ() + (Math.sin(angle) * distance);
		return new Location(origin.getWorld(), x, 100, z);
	}

	public static StargateJumpHolder checkStargateJump(Player p, Craft c) {
		Location loc = p.getLocation();
		for (String s : stargates.keySet()) {
			StargateData d = stargates.get(s);
				if (stargateCheck(d, loc)) {
					p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering slipgate to " + d.targetServer + "!");
					return new StargateJumpHolder(p, c, d.targetServer, d.targetLocation.getBlockX(), d.targetLocation.getBlockY(), d.targetLocation.getBlockZ());
				}
		}
		return null;
	}

	private static boolean stargateCheck(StargateData d, Location pilot) {
		Location gate = d.startLocation;
		if (d.orientation == 0) { // north/south orientation
			// stargate bounding boxes are 24 radius across and vertical and 50
			// on either side to the front / back
			if (gate.getX() - 24 < pilot.getX() && pilot.getX() < gate.getX() + 24) {
				if (gate.getY() - 24 < pilot.getY() && pilot.getY() < gate.getY() + 24) {
					if (gate.getZ() - 50 < pilot.getZ() && pilot.getZ() < gate.getZ() + 50) {
						return true;
					}
				}
			}
		} else {
			if (gate.getX() - 50 < pilot.getX() && pilot.getX() < gate.getX() + 50) {
				if (gate.getY() - 24 < pilot.getY() && pilot.getY() < gate.getY() + 24) {
					if (gate.getZ() - 24 < pilot.getZ() && pilot.getZ() < gate.getZ() + 24) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String slipWarpCheck(int warpX, int warpZ) {
		// positive x is east. Positive z is south.
		if (SYSTEM.equals("AratorSystem")) {
			// QuillonSystem is to the south of AratorSystem
			if (warpZ > 3500)
				return "QuillonSystem";
			// Digitalis is to the east of AratorSystem
			if (warpX > 3500)
				return "YavarSystem";
		} else if (SYSTEM.equals("YavarSystem")) {
			// AratorSystem is to the west of YavarSystem
			if (warpX < -5000)
				return "AratorSystem";
			// QuillonSystem is to the south of YavarSystem
			if (warpZ > 5000)
				return "QuillonSystem";
		} else if (SYSTEM.equals("QuillonSystem")) {
			// AratorSystem is to the north of QuillonSystem
			if (warpZ < -4500)
				return "AratorSystem";
			// Digitalis is to the east of QuillonSystem
			if (warpX > 4500)
				return "QuillonSystem";
		}
		return null;
	}

	public static int getSlipCoordX(String systarget, int coordX) {
		if (SYSTEM.equals("AratorSystem")) {
			if (systarget.equals("QuillonSystem")) {
				return coordX;
			} else {
				return -4000;
			}
		} else if (SYSTEM.equals("QuillonSystem")) {
			if (systarget.equals("AratorSystem")) {
				return coordX;
			} else {
				return 4000;
			}
		} else {
			if (systarget.equals("AratorSystem")) {
				return 3500;
			} else {
				return coordX;
			}
		}
	}

	public static int getSlipCoordZ(String systarget, int coordZ) {
		if (SYSTEM.equals("AratorSystem")) {
			if (systarget.equals("QuillonSystem")) {
				return coordZ;
			} else {
				return -4000;
			}
		} else if (SYSTEM.equals("QuillonSystem")) {
			if (systarget.equals("AratorSystem")) {
				return coordZ;
			} else {
				return 4500;
			}
		} else {
			if (systarget.equals("AratorSystem")) {
				return 3500;
			} else {
				return coordZ;
			}
		}
	}

	public static String getSystem() {
		return SYSTEM;
	}

	public static ArrayList<Craft> getCraftsWithinRadius(World w, int X, int Z, int radius) {
		int compareValue = radius * radius;
		Craft[] inWorld = CraftManager.getInstance().getCraftsInWorld(w);
		ArrayList<Craft> retval = new ArrayList<Craft>();
		if (inWorld != null) {
			for (Craft c : inWorld) {
				int xDiff = c.getMinX() - X;
				int zDiff = c.getMinZ() - Z;
				if (compareValue >= xDiff * xDiff + zDiff * zDiff) {
					retval.add(c);
				}
			}
		}
		return retval;
	}

	public static boolean isBeingJammed(World w, int X, int Z) {
		ArrayList<Craft> crafts = getCraftsWithinRadius(w, X, Z, 100);
		if(crafts == null) return false;
		for (Craft c : crafts) {
			if (c.isJamming) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isInCorePlanet(Player p) {
		
		String world = p.getLocation().getWorld().getName();
		
		if (world.equals("Xira")) {
			
			return true;
			
		}
		
		return false;
		
	}
}
