package net.countercraft.movecraft.cryo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CryoSpawn {
	public static final String KEY_LINE = "{" + ChatColor.AQUA + "Cryo Pod" + ChatColor.BLACK + "}";
	public static final String ACTIVE_L1 = ChatColor.RED + "ACTIVE";
	public static final String ACTIVE_L2 = ChatColor.RED + "POD";

	public String player;
	public String server;
	public String world;
	public int x, y, z;
	public boolean isActive;

	public static CryoSpawn DEFAULT;

	public CryoSpawn(String player, String server, String world, int x, int y, int z, boolean isActive) {
		this.player = player;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.isActive = isActive;
	}

	public static void setUp() {
		// called from onEnable
		String bedspawn_table = "CREATE TABLE IF NOT EXISTS " + "CRYOSPAWNS (" + "`name` VARCHAR(32) NOT NULL," + "`server` VARCHAR(32) DEFAULT NULL," + "`world` VARCHAR(32) DEFAULT NULL,"
				+ "`x` int(11) DEFAULT 0," + "`y` int(11) DEFAULT 0," + "`z` int(11) DEFAULT 0," + "PRIMARY KEY (`name`)" + ")";
		getContext();
		Statement s = null;
		try {
			s = Bedspawn.cntx.createStatement();
			s.executeUpdate(bedspawn_table);
			s.close();
			System.out.println("[SQBedSpawn] Table check/creation sucessful");
		} catch (SQLException ee) {
			System.out.println("[SQBedSpawn] Table Creation Error");
		} finally {
			close(s);
		}

		Bedspawn bd = Bedspawn.DEFAULT;
		CryoSpawn def = new CryoSpawn(bd.player, bd.server, bd.world, bd.x, bd.y, bd.z, false);
		DEFAULT = def;
	}

	// called when a player right clicks a [cryopod] sign
	public static void setUpCryoTube(Sign initCryoSign, String playerUntrimmed) {
		String player = signTrim(playerUntrimmed);
		if (isCryoTube(initCryoSign)) {
			initCryoSign.setLine(0, KEY_LINE);
			initCryoSign.setLine(1, player);
			initCryoSign.update();
			if (hasKey(player)) {
				updatePodSpawn(initCryoSign);
			} else {
				Block point = initCryoSign.getBlock().getRelative(0, -1, 0);
				CryoSpawn spawn = new CryoSpawn(player, Bukkit.getServerName(), point.getWorld().getName(), point.getX(), point.getY(), point.getZ(), false);
				Bukkit.getPlayer(playerUntrimmed).sendMessage("Your spawn has been set!");
				setNewPodSpawn(spawn);
			}
		} else {
			Bukkit.getPlayer(playerUntrimmed).sendMessage("Not a valid cryopod!");
		}
	}

	public static void updatePodSpawn(Sign s) {
		String player = s.getLine(1);
		Block point = s.getBlock().getRelative(0, -1, 0);
		CryoSpawn spawn = new CryoSpawn(player, Bukkit.getServerName(), s.getBlock().getWorld().getName(), point.getX(), point.getY(), point.getZ(), isActive(s));
		updatePodSpawn(player, spawn);
	}

	public static void updatePodSpawns(World w, ArrayList<MovecraftLocation> signLocations) {
		for (MovecraftLocation l : signLocations) {
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) b.getState();
			if (isCryoTube(s) && s.getLine(0).equals(KEY_LINE)) {
				updatePodSpawn(s);
			}
		}
	}

	public void onSignBreak(Sign s) {
		if (s.getLine(0).equals(KEY_LINE)) {
			removePodSpawn(s.getLine(1));
		}
	}

	public static void removePodSpawn(Sign s) {
		removePodSpawn(s.getLine(1));
	}

	// ==========================================================
	// SQL STUFF
	// ==========================================================

	public static void setNewPodSpawn(CryoSpawn spawn) {
		System.out.println("Setting new pod spawn!");
		String playerName = spawn.player;
		String playerWorld = spawn.world;
		String playerServer = spawn.server;
		int playerX = spawn.x;
		int playerY = spawn.y;
		int playerZ = spawn.z;

		if (!getContext()) {
			System.out.println("something is wrong!");
			return;
		}
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("INSERT INTO CRYOSPAWNS (name, server, world, x, y, z) values (?,?,?,?,?,?)");
			s.setString(1, playerName);
			s.setString(2, playerServer);
			s.setString(3, playerWorld);
			s.setInt(4, playerX);
			s.setInt(5, playerY);
			s.setInt(6, playerZ);
			s.execute();
			s.close();
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		System.out.println("Done setting new pod spawn!");
	}

	public static void updatePodSpawn(String name, CryoSpawn b) {
		Player p = Bukkit.getPlayer(name);
		if (p != null && p.isOnline()) {
			p.sendMessage("Your cryopod spawn location was updated.");
		}
		name = signTrim(name);
		String playerName = b.player;
		String playerWorld = b.world;
		String playerServer = b.server;
		int playerX = b.x;
		int playerY = b.y;
		int playerZ = b.z;

		if (!getContext()) {
			System.out.println("something is wrong!");
			return;
		}
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET server = ?, world = ?, x = ?, y = ?, z = ? WHERE NAME = ?");
			s.setString(1, playerWorld);
			s.setString(2, playerServer);
			s.setInt(3, playerX);
			s.setInt(4, playerY);
			s.setInt(5, playerZ);
			s.setString(6, playerName);
			s.execute();
			s.close();
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
	}

	public static void removePodSpawn(String playerUntrimmed) {
		System.out.println("removing pod spawn: " + playerUntrimmed);
		String player = signTrim(playerUntrimmed);
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("DELETE FROM CRYOSPAWNS WHERE `name` = ?");
			s.setString(1, player);
			s.execute();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(s);
		}
		Player p = Bukkit.getPlayer(playerUntrimmed);
		if (p != null) {
			p.sendMessage(ChatColor.RED + "Your CryoSpawn was broken!");
		}
	}

	public static CryoSpawn getSpawn(String playerUntrimmed) {
		System.out.println("Getting spawn for: " + playerUntrimmed);
		String player = signTrim(playerUntrimmed);
		String playerName;
		String playerWorld;
		String playerServer;
		int playerX;
		int playerY;
		int playerZ;
		boolean active;
		if (!getContext())
			System.out.println("[SQBedSpawn] context error");
		CryoSpawn retval = null;
		Statement s = null;
		try {
			s = Bedspawn.cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM CRYOSPAWNS WHERE name = " + "\'" + player + "\'");

			if (rs.next()) {
				playerName = rs.getString("name");
				playerWorld = rs.getString("world");
				playerServer = rs.getString("server");
				playerX = rs.getInt("x");
				playerY = rs.getInt("y");
				playerZ = rs.getInt("z");
				active = rs.getBoolean("active");
				retval = new CryoSpawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ, active);
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		System.out.println("Returning spawn!");
		return retval;
	}

	// ==========================================================
	// END SQL STUFF
	// ==========================================================

	static boolean isCryoTube(Sign s) {
		return CryoUtils.isCryoTube(s);
	}

	public static String signTrim(String s) {
		if (s.length() <= 15)
			return s;
		else
			return s.substring(0, 15);
	}

	public static boolean getContext() {
		return Bedspawn.getContext();
	}

	public static boolean hasKey(String player) {

		String playerName = signTrim(player);

		if (!getContext()) {
			System.out.println("something is wrong!");
			return false;
		}
		Statement s = null;
		try {
			s = Bedspawn.cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM CRYOSPAWNS WHERE name = " + "\'" + playerName + "\'");

			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return false;
	}

	private static void close(Statement s) {
		if (s == null)
			return;
		try {
			s.close();
			System.out.println("[Movecraft] Closing statement");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean respawnPlayer(PlayerRespawnEvent event, final Player p) {
		CryoSpawn spawn = CryoSpawn.getSpawn(p.getName());
		if (spawn == null) {
			// spawn = CryoSpawn.DEFAULT;
			// use the bedspawn system
			return false;
		}

		final CryoSpawn s = spawn;
		System.out.println("Player Current Server: " + Bukkit.getServerName());
		if (!s.server.equals(Bukkit.getServerName())) {
			System.out.println("server name and target server name aren't equal, teleporting.");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
				public void run() {
					BungeePlayerHandler.sendDeath(p, s.server);
				}
			}, 3L);
		} else {
			final Location loc2 = new Location(Bukkit.getWorld(s.world), s.x + 0.5, s.y, s.z + 0.5);
			if (checkForNotAir(loc2)) {
				if (event != null) {
					event.setRespawnLocation(loc2);
				} else {
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
						public void run() {
							p.teleport(loc2);
							CryoSpawn.checkAndPlayEffects(loc2);
						}
					}, 3L);
				}
			} else {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
					public void run() {
						BungeePlayerHandler.sendPlayer(p, Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
					}
				}, 20L);
				Bedspawn.deleteBedspawn(event.getPlayer().getName());
			}
		}
		return true;
	}

	public static boolean checkForNotAir(Location loc) {
		boolean found = false;
		Block testblock = loc.getBlock();
		int tries = 0;
		while (!found && tries < 5) {
			tries++;
			testblock = testblock.getRelative(BlockFace.DOWN);
			if (testblock.getType().isSolid()) {
				found = true;
				break;
			}
		}
		return found;
	}

	public static void toggleActive(Sign sign, Player player2) {
		boolean isActive = isActive(sign);
		if (signTrim(player2.getName()).equals(signTrim(sign.getLine(1)))) {
			setActive(player2.getName(), !isActive);
			if (isActive) {
				sign.setLine(2, "");
				sign.setLine(3, "");
				player2.sendMessage("Your CryoPod is now in passive mode. You will not be warped to it unless you die.");
			} else {
				sign.setLine(2, ACTIVE_L1);
				sign.setLine(3, ACTIVE_L2);
				player2.sendMessage("Your CryoPod is now in active mode. You will be warped to it whenever you log in.");
			}
		}
		sign.update();
	}

	public static boolean isActive(Sign s) {
		String l2 = s.getLine(2);
		String l3 = s.getLine(3);

		return l2.equals(ACTIVE_L1) && l3.equals(ACTIVE_L2);
	}

	public static void setActive(String name, boolean active) {
		name = signTrim(name);
		if (!getContext()) {
			System.out.println("something is wrong!");
			return;
		}
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET active = ? WHERE NAME = ?");
			s.setBoolean(1, active);
			s.setString(2, name);
			s.execute();
			s.close();
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
	}

	public static void checkAndPlayEffects(Location pe) {
		Block target = pe.getBlock();
		Block up = target.getRelative(BlockFace.UP);
		if (up.getType() == Material.WALL_SIGN) {
			Sign s = (Sign) up.getState();
			if (s.getLine(0).equals(CryoSpawn.KEY_LINE)) {
				CryoSpawn.playEffects(pe);
				System.out.println("Playing Effects!");
			}
		}
	}

	public static void playEffects(Location l) {
		World w = l.getWorld();
		w.playSound(l, Sound.PORTAL_TRAVEL, 1.0F, 1.0F);
		w.playEffect(l, Effect.ENDER_SIGNAL, 0);
		w.playEffect(l, Effect.SMOKE, 0);
	}

	public static void addToAnyShips(Location target, Player p) {
		Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
		if (crafts != null) {
			for (Craft c : crafts) {
				if (MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
					try {
						c.playersRidingLock.acquire();
						if (!c.playersRidingShip.contains(p.getUniqueId())) {
							c.playersRidingShip.add(p.getUniqueId());
							p.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
						}
						c.playersRidingLock.release();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return;
				}
			}
		}
	}
}
