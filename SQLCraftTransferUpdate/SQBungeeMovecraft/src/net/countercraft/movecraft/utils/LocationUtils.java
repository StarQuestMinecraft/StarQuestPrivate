package net.countercraft.movecraft.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.task.RepeatTryServerJumpTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LocationUtils {
	public static HashMap<String, Location> planets = new HashMap<String, Location>();
	public static HashMap<String, StargateData> stargates = new HashMap<String, StargateData>();
	public static String SYSTEM;

	public static void setUp(FileConfiguration cfg) {
		SYSTEM = cfg.getString("System");
		if (SYSTEM == null) {
			Bukkit.getLogger().log(Level.SEVERE, "No System setting found in config! Severe!");
		} else if (SYSTEM.equals("Defalos")) {
			planets.put("Acualis", locationFromConfig(cfg, "Acualis"));

		}
	}

	public static boolean spaceCheck(String worldname) {
		return (worldname.startsWith("Defalos"));
	}

	public static boolean spaceCheck(World w) {
		return spaceCheck(w.getName());
	}

	public static boolean spaceCheck(Location l) {
		return spaceCheck(l.getWorld().getName());
	}

	public static boolean spaceCheck(Player p) {
		String wName = p.getLocation().getWorld().getName();
		if (wName.equals("AsteroidBelt")) {
			return true;
		}
		return spaceCheck(wName);
	}

	public static boolean spaceCheck(Player p, boolean includeAsteroidBelt) {
		if (includeAsteroidBelt)
			return spaceCheck(p);
		else
			return spaceCheck(p.getLocation());
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
	@Deprecated
	public static Location getWarpLocation(String planet, Location playerLoc) {
		System.out.println("Planets is null: " + (planets == null));
		for (String s : planets.keySet()) {
			System.out.println("s: " + s);
			System.out.println("planet: " + planet);
			if (s.equals(planet)) {
				Location planetLoc = planets.get(s);
				double angle = getAngleFromOriginTo(playerLoc);
				Location target = getSpawnLocationFromAngle(angle, planetLoc, 120);
				return target;
			}
		}
		return null;
	}
	public static SerializableLocation getWarpLocation(Location playerLoc) {
		String worldName = playerLoc.getWorld().getName();
		System.out.println("worldName: " + worldName);
		String systemName = Movecraft.getInstance().getConfig().getString("System");
		System.out.println("systemName: " + systemName);
		ConfigurationSection worldLocation = Movecraft.getInstance().getConfig().getConfigurationSection(worldName);
		String xString = worldLocation.getString("Xcoord");
		String yString = worldLocation.getString("Ycoord");
		String zString = worldLocation.getString("Zcoord");
		System.out.println(xString);
		System.out.println(yString);
		System.out.println(zString);
		double x = Double.parseDouble(xString);
		double y = Double.parseDouble(yString);
		double z = Double.parseDouble(zString);
		
		System.out.println("Destination x: " + x);
		System.out.println("Destination y: " + y);
		System.out.println("Destination z: " + z);
		SerializableLocation planetLocation = new SerializableLocation(systemName, x, y, z);
		double angle = getAngleFromOriginTo(playerLoc);
		SerializableLocation targetLocation = getSpawnLocationFromAngle(angle, planetLocation, 120);
		return targetLocation;
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
	public static SerializableLocation getSpawnLocationFromAngle(double angle, SerializableLocation planetLocation, int distance) {
		planetLocation.offsetCoordinatesBy(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
		return planetLocation;
	}

	public static StargateJumpHolder checkStargateJump(Player p, Craft c) {
		Location loc = p.getLocation();
		for (String s : stargates.keySet()) {
			StargateData d = stargates.get(s);
			if (stargateCheck(d, loc)) {
				p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering slipgate to " + s + "!");
				return new StargateJumpHolder(p, c, s, d.targetLocation.getBlockX(), d.targetLocation.getBlockY(), d.targetLocation.getBlockZ());
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
		if (SYSTEM.equals("Trinitos_Alpha")) {
			// Trinitos_Gamma is to the south of Trinitos_Alpha
			if (warpZ > 3500)
				return "Trinitos_Gamma";
			// Digitalis is to the east of Trinitos_Alpha
			if (warpX > 3500)
				return "Trinitos_Beta";
		} else if (SYSTEM.equals("Trinitos_Beta")) {
			// Trinitos_Alpha is to the west of Trinitos_Beta
			if (warpX < -5000)
				return "Trinitos_Alpha";
			// Trinitos_Gamma is to the south of Trinitos_Beta
			if (warpZ > 5000)
				return "Trinitos_Gamma";
		} else if (SYSTEM.equals("Trinitos_Gamma")) {
			// Trinitos_Alpha is to the north of Trinitos_Gamma
			if (warpZ < -4500)
				return "Trinitos_Alpha";
			// Digitalis is to the east of Trinitos_Gamma
			if (warpX > 4500)
				return "Trinitos_Gamma";
		}
		return null;
	}

	public static int getSlipCoordX(String systarget, int coordX) {
		if (SYSTEM.equals("Trinitos_Alpha")) {
			if (systarget.equals("Trinitos_Gamma")) {
				return coordX;
			} else {
				return -4000;
			}
		} else if (SYSTEM.equals("Trinitos_Gamma")) {
			if (systarget.equals("Trinitos_Alpha")) {
				return coordX;
			} else {
				return 4000;
			}
		} else {
			if (systarget.equals("Trinitos_Alpha")) {
				return 3500;
			} else {
				return coordX;
			}
		}
	}

	public static int getSlipCoordZ(String systarget, int coordZ) {
		if (SYSTEM.equals("Trinitos_Alpha")) {
			if (systarget.equals("Trinitos_Gamma")) {
				return coordZ;
			} else {
				return -4000;
			}
		} else if (SYSTEM.equals("Trinitos_Gamma")) {
			if (systarget.equals("Trinitos_Alpha")) {
				return coordZ;
			} else {
				return 4500;
			}
		} else {
			if (systarget.equals("Trinitos_Alpha")) {
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
}
