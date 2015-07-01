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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
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
	public boolean updatedSinceLogout;

	public static CryoSpawn DEFAULT;

	public CryoSpawn(String player, String server, String world, int x, int y, int z, boolean isActive) {
		this.player = signTrim(player);
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
				+ "`x` int(11) DEFAULT 0," + "`y` int(11) DEFAULT 0," + "`z` int(11) DEFAULT 0," + "`active` BOOLEAN DEFAULT false," + "`updatedSinceLastLogin` BOOLEAN DEFAULT false,"
				+ "PRIMARY KEY (`name`)" + ")";
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
	public static void setUpCryoTubeAsync(Sign initCryoSign, String playerUntrimmed) {
		String player = signTrim(playerUntrimmed);
		if (isCryoTube(initCryoSign)) {
			initCryoSign.setLine(0, KEY_LINE);
			initCryoSign.setLine(1, player);
			initCryoSign.update();
			if (hasKeyAsync(player)) {
				updatePodSpawnAsync(initCryoSign, false);
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

	public static void updatePodSpawnAsync(Sign s, boolean checkAsActiveUpdate) {
		String player = s.getLine(1).trim();
		Block point = s.getBlock().getRelative(0, -1, 0);
		CryoSpawn spawn = new CryoSpawn(player, Bukkit.getServerName(), s.getBlock().getWorld().getName(), point.getX(), point.getY(), point.getZ(), isActive(s));
		updatePodSpawnAsync(player, spawn, checkAsActiveUpdate);
	}

	public static void updatePodSpawnsAsync(final World w, final ArrayList<MovecraftLocation> signLocations) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				for (MovecraftLocation l : signLocations) {
					Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
					if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
						Sign s = (Sign) b.getState();
						if (isCryoTube(s) && s.getLine(0).equals(KEY_LINE)) {
							updatePodSpawnAsync(s, true);
						}
					}
				}
			}
		}, 0L);
		
	}

	public static void removePodSpawn(Sign s) {
		removePodSpawn(s.getLine(1), s.getLocation());
	}

	// ==========================================================
	// SQL STUFF
	// ==========================================================

	public static void setNewPodSpawn(final CryoSpawn spawn) {
		Runnable r = new Runnable() {
			public void run() {
				System.out.println("Setting new pod spawn!");
				String playerName = signTrim(spawn.player);
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
					s = Bedspawn.cntx.prepareStatement("INSERT INTO CRYOSPAWNS (name, server, world, x, y, z, active, updatedSinceLastLogin) values (?,?,?,?,?,?,?,?)");
					s.setString(1, playerName);
					s.setString(2, playerServer);
					s.setString(3, playerWorld);
					s.setInt(4, playerX);
					s.setInt(5, playerY);
					s.setInt(6, playerZ);
					s.setBoolean(7, false);
					s.setBoolean(8, false);
					s.execute();
					s.close();
				} catch (SQLException e) {
					System.out.print("[SQBedSpawn] SQL Error (new pod spawn)" + e.getMessage());
				} catch (Exception e) {
					System.out.print("[SQBedSpawn] SQL Error (new pod spawn)");
					e.printStackTrace();
				} finally {
					close(s);
				}
			}
		};
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), r);
	}

	public static void updatePodSpawnAsync(final String nameold, final CryoSpawn b, final boolean checkAsActiveUpdate) {
		Runnable r = new Runnable() {
			public void run() {
				Player p = Bukkit.getPlayer(nameold);
				if (p != null && p.isOnline()) {
					p.sendMessage("Your cryopod spawn location was updated.");
				} else {

				}
				System.out.println("Updating cryospawn!");
				String name = signTrim(nameold);
				String playerWorld = b.world;
				String playerServer = b.server;
				int playerX = b.x;
				int playerY = b.y;
				int playerZ = b.z;
				boolean active = b.isActive;

				if (!getContext()) {
					System.out.println("something is wrong!");
					return;
				}

				if (active) {
					// we need to set updatedSinceLastLogin to true
					if (p != null && p.isOnline() && checkAsActiveUpdate) {
						p.sendMessage(ChatColor.RED + "Since your cryopod is set to active and it was updated, you will be sent to it when you next log in.");
						p.sendMessage(ChatColor.RED + "If you do not wish to have this happen, set your cryopod to not active.");
					}
				}
				PreparedStatement s = null;
				try {
					if (active && checkAsActiveUpdate) {
						s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET server = ?, world = ?, x = ?, y = ?, z = ?, active = ?, updatedSinceLastLogin = ? WHERE NAME = ?");
					} else {
						s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET server = ?, world = ?, x = ?, y = ?, z = ?, active = ? WHERE NAME = ?");
					}
					s.setString(1, playerWorld);
					s.setString(2, playerServer);
					s.setInt(3, playerX);
					s.setInt(4, playerY);
					s.setInt(5, playerZ);
					s.setBoolean(6, active);
					if (active && checkAsActiveUpdate) {
						s.setBoolean(7, true);
						s.setString(8, name);
					} else {
						s.setString(7, name);
					}
					s.execute();
					s.close();
				} catch (SQLException e) {
					System.out.print("[SQBedSpawn] SQL Error (Update cryospawn)" + e.getMessage());
				} catch (Exception e) {
					System.out.print("[SQBedSpawn] SQL Error (update cryospawn)");
					e.printStackTrace();
				} finally {
					close(s);
				}
			}
		};
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), r);

	}

	public static void removePodSpawn(String playerUntrimmed, Location l) {
		removePodSpawn(playerUntrimmed, null, l);
	}

	public static void removePodSpawn(final String playerUntrimmed, final CommandSender notify, final Location l) {
		Runnable r = new Runnable() {
			public void run() {
				System.out.println("REMOVING POD SPAWN!");
				String player = signTrim(playerUntrimmed);
				CryoSpawn spawn = getSpawnAsync(player);
				Player p = Bukkit.getPlayer(playerUntrimmed);
				if(l == null || (spawn.x == l.getBlockX() && (spawn.y == l.getBlockY() || spawn.y == l.getBlockY() - 1) && spawn.z == l.getBlockZ() && spawn.world.equalsIgnoreCase(l.getWorld().getName()))){
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
					if (p != null) {
						p.sendMessage(ChatColor.RED + "Your CryoSpawn was broken!");
					}
					if (notify != null) {
						notify.sendMessage("Succesfully removed spawn.");
					}
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.RED + "This was not your currently enabled cryopod, so your spawn was not updated.");
					}
					if (notify != null) {
						notify.sendMessage("Spawn not removed; this was not their enabled cryopod.");
					}
				}
			}
		};
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), r);
	}

	public static CryoSpawn getSpawnAsync(String playerUntrimmed) {
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
				System.out.println("Got a retval!");
			} else {
				System.out.println("Retval is null!");
				return null;
			}
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (getSpawnAsync)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return retval;
	}

	// ==========================================================
	// END SQL STUFF
	// ==========================================================

	static boolean isCryoTube(Sign s) {
		return CryoUtils.isCryoTube(s);
	}

	public static String signTrim(String s) {
		if (s == null)
			return null;
		if (s.length() <= 15)
			return s;
		else
			return s.substring(0, 15);
	}

	public static boolean getContext() {
		return Bedspawn.getContext();
	}

	public static boolean hasKeyAsync(String player) {

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
			System.out.print("[SQBedSpawn] SQL Error(hasKey)" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (hasKey)");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean respawnPlayerAsync(final Player p) {
		System.out.println("CRYO RESPAWN: " + p.getName());
		CryoSpawn spawn = CryoSpawn.getSpawnAsync(p.getName());
		if (spawn == null) {
			System.out.println("CRYO RESPAWN: " + p.getName());
			System.out.println("Spawn is null for player " + p.getName());
			// spawn = CryoSpawn.DEFAULT;
			// use the bedspawn system
			return false;
		}

		final CryoSpawn s = spawn;
		if (!s.server.equalsIgnoreCase(Bukkit.getServerName())) {
			System.out.println("Server is not the same as target server, sending death to " + s.server);
			p.sendMessage(ChatColor.RED + "Teleporting in one second.");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
				public void run() {
					BungeePlayerHandler.sendPlayer(p, s.server, s.world, s.x, s.y, s.z);
				}
			}, 20L);
		} else {
			System.out.println("Server is the same as the target server, respawning.");
			final Location loc2 = new Location(Bukkit.getWorld(s.world), s.x + 0.5, s.y, s.z + 0.5);
			System.out.println("Target location is " + loc2);
			// if (checkForNotAir(loc2)) {
			System.out.println("Teleporting player delayed.");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
				public void run() {
					System.out.println("Delayed teleport activating.");
					CryoUtils.removeBlockAtCryoSpawn(loc2);
					p.setFallDistance(0);
					p.teleport(loc2);
					if (p.getGameMode() == GameMode.SURVIVAL) {
						p.setFlying(false);
						p.setAllowFlight(false);
					}
					CryoSpawn.checkAndPlayEffects(loc2);
					System.out.println("Done spawning.");
				}
			}, 3L);
			/*
			 * } else {
			 * Bukkit.getServer().getScheduler().scheduleSyncDelayedTask
			 * (Movecraft.getInstance(), new Runnable() { public void run() {
			 * BungeePlayerHandler.sendPlayer(p, Bedspawn.DEFAULT.server,
			 * Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y,
			 * Bedspawn.DEFAULT.z); } }, 20L);
			 * Bedspawn.deleteBedspawn(event.getPlayer().getName()); }
			 */
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

	public static void toggleActiveAsync(Sign sign, Player player2) {
		boolean isActive = isActive(sign);
		if (signTrim(player2.getName()).equals(signTrim(sign.getLine(1)))) {
			if (isActive) {
				sign.setLine(2, "");
				sign.setLine(3, "");
				player2.sendMessage("Your CryoPod is now in passive mode. You will not be warped to it unless you die.");
			} else {
				sign.setLine(2, ACTIVE_L1);
				sign.setLine(3, ACTIVE_L2);
				player2.sendMessage("Your CryoPod is now in active mode. Whenever its position is updated, you will be warped to it the next time you log in.");
			}
			sign.update();
			updatePodSpawnAsync(sign, false);
		}
	}

	public static boolean isActive(Sign s) {
		String l2 = s.getLine(2);
		String l3 = s.getLine(3);

		return l2.equals(ACTIVE_L1) && l3.equals(ACTIVE_L2);
	}

	/*public static void setActive(final String name, final boolean active) {
		Runnable r = new Runnable() {
			public void run() {
				String name2 = signTrim(name);
				if (!getContext()) {
					System.out.println("something is wrong!");
					return;
				}
				PreparedStatement s = null;
				try {
					s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET active = ? WHERE NAME = ?");
					s.setBoolean(1, active);
					s.setString(2, name2);
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
		};
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), r);

	}*/

	public static CryoSpawn getSpawnIfNeedsActiveRespawn(String player) {

		String playerName = signTrim(player);

		if (!getContext()) {
			System.out.println("something is wrong!");
			return null;
		}
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("SELECT * FROM CRYOSPAWNS WHERE name = ? AND active = ? AND updatedSinceLastLogin = ?");
			s.setString(1,  playerName);
			s.setBoolean(2, true);
			s.setBoolean(3, true);
			ResultSet rs = s.executeQuery();
			// s =
			// Bedspawn.cntx.prepareStatement("SELECT * FROM minecraft.cryospawns WHERE `name` = ? AND `active` = true AND `updatedSinceLastLogin` = true");
			//ResultSet rs = s.executeQuery("SELECT * FROM CRYOSPAWNS WHERE `name` = " + "\" + playerName + \" AND `active` = 1 AND `updatedSinceLastLogin` = 1");
			CryoSpawn retval = null;
			if (rs.next()) {
				String playerWorld = rs.getString("world");
				String playerServer = rs.getString("server");
				int playerX = rs.getInt("x");
				int playerY = rs.getInt("y");
				int playerZ = rs.getInt("z");
				boolean active = rs.getBoolean("active");
				retval = new CryoSpawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ, active);
			}
			s.close();
			return retval;
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error(getSpawnifNeedsActiveRespawn)" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (getSpawnIfNeedsActive)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return null;
	}

	public static void checkAndPlayEffects(Location pe) {
		Block target = pe.getBlock();
		Block up = target.getRelative(BlockFace.UP);
		if (up.getType() == Material.WALL_SIGN) {
			Sign s = (Sign) up.getState();
			if (s.getLine(0).equals(CryoSpawn.KEY_LINE)) {
				CryoSpawn.playEffects(pe);
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

	public static void unsetUpdatedSinceLastLogin(final String name) {
		Runnable r = new Runnable() {
			public void run() {
				final String name2 = signTrim(name);
				if (!getContext()) {
					System.out.println("something is wrong!");
					return;
				}
				PreparedStatement s = null;
				try {
					s = Bedspawn.cntx.prepareStatement("UPDATE CRYOSPAWNS SET active = ? WHERE NAME = ?");
					s.setBoolean(1, false);
					s.setString(2, name2);
					s.execute();
					s.close();
				} catch (SQLException e) {
					System.out.print("[SQBedSpawn] SQL Error (unsetUpdated)" + e.getMessage());
				} catch (Exception e) {
					System.out.print("[SQBedSpawn] SQL Error (Unknown)(unsetUpdated)");
					e.printStackTrace();
				} finally {
					close(s);
				}
			}
		};
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), r);
	}
}
