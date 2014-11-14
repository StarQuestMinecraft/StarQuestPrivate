package net.countercraft.movecraft.cryo;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

public class CryoSpawn {
	public static final String KEY_LINE = "{" + ChatColor.AQUA + "Cryo Pod" + ChatColor.BLACK + "}";

	public String player;
	public String server;
	public String world;
	public int x, y, z;
	
	public static CryoSpawn DEFAULT;
	
	public CryoSpawn(String player, String server, String world, int x, int y, int z) {
		this.player = player;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
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
		CryoSpawn def = new CryoSpawn(bd.player, bd.server, bd.world, bd.x, bd.y, bd.z);
		DEFAULT = def;
	}
	
	//called when a player right clicks a [cryopod] sign
	public static void setUpCryoTube(Sign initCryoSign, String player){
		if(isCryoTube(initCryoSign)){
			initCryoSign.setLine(0, KEY_LINE);
			initCryoSign.setLine(1, signTrim(player));
			initCryoSign.update();
			if(hasKey(player)){
				updatePodSpawn(initCryoSign);
			}else{
				Block point = initCryoSign.getBlock().getRelative(0, -1, 0);
				CryoSpawn spawn = new CryoSpawn(player, Bukkit.getServerName(), point.getWorld().getName(), point.getX(), point.getY(), point.getZ());
				Bukkit.getPlayer(player).sendMessage("Your spawn has been set!");
				setNewPodSpawn(player, spawn);
			}
		} else {
			Bukkit.getPlayer(player).sendMessage("Not a valid cryopod!");
		}
	}
	
	public static void updatePodSpawn(Sign s){
		String player = s.getLine(1);
		Block point = s.getBlock().getRelative(0, -1, 0);
		CryoSpawn spawn = new CryoSpawn(player, Bukkit.getServerName(), s.getBlock().getWorld().getName(), point.getX(), point.getY(), point.getZ());
		updatePodSpawn(player, spawn);
	}
	
	public static void updatePodSpawns(World w, ArrayList<MovecraftLocation> signLocations){
		for(MovecraftLocation l : signLocations){
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			Sign s = (Sign) b.getState();
			if(isCryoTube(s)){
				updatePodSpawn(s);
			}
		}
	}
	
	public void onSignBreak(Sign s){
		if(s.getLine(0).equals(KEY_LINE)){
			removePodSpawn(s.getLine(1));
		}
	}
	
	public static void removePodSpawn(Sign s){
		removePodSpawn(s.getLine(1));
	}
	
	//==========================================================
	//SQL STUFF
	//==========================================================
	
	public static void setNewPodSpawn(String name, CryoSpawn spawn){
		System.out.println("Setting new pod spawn!");
		name = signTrim(name);
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
	
	public static void updatePodSpawn(String name, CryoSpawn b){
		Player p = Bukkit.getPlayer(name);
		if(p != null && p.isOnline()){
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
	
	public static void removePodSpawn(String name){
		System.out.println("removing pod spawn: " + name);
		name = signTrim(name);
		PreparedStatement s = null;
		try {
			s = Bedspawn.cntx.prepareStatement("DELETE FROM CRYOSPAWNS WHERE `name` = ?");
			s.setString(1, name);
			s.execute();
			s.close();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			close(s);
		}
		Player p = Bukkit.getPlayer(name);
		if(p != null){
			p.sendMessage(ChatColor.RED + "Your CryoSpawn was broken!");
		}
	}
	
	public static CryoSpawn getSpawn(String name){
		System.out.println("Getting spawn for: " + name);
		name = signTrim(name);
		String playerName;
		String playerWorld;
		String playerServer;
		int playerX;
		int playerY;
		int playerZ;
		if (!getContext())
			System.out.println("[SQBedSpawn] context error");
		CryoSpawn retval = null;
		Statement s = null;
		try {
			s = Bedspawn.cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM CRYOSPAWNS WHERE name = " + "\'" + name + "\'");

			if (rs.next()) {
				playerName = rs.getString("name");
				playerWorld = rs.getString("world");
				playerServer = rs.getString("server");
				playerX = rs.getInt("x");
				playerY = rs.getInt("y");
				playerZ = rs.getInt("z");
				retval = new CryoSpawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ);
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
	
	
	//==========================================================
	//END SQL STUFF
	//==========================================================
	
	
	static boolean isCryoTube(Sign s){
		return CryoUtils.isCryoTube(s);
	}
	
	private static boolean test(Block b, BlockFace relative){
		Block rel = b.getRelative(relative);
		Material type = rel.getType();
		byte data = rel.getData();
		if(type == Material.SMOOTH_BRICK && data == 3){
			return true;
		}
		if(type == Material.STAINED_GLASS && data == 7){
			return true;
		}
		return false;
	}
	
	private static String signTrim(String s){
		if(s.length() < 16) return s;
		else return s.substring(0, 15);
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
	
	private static void close(Statement s){
		if(s == null) return;
		try{
			s.close();
			System.out.println("[Movecraft] Closing statement");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static boolean respawnPlayer(PlayerRespawnEvent event, final Player p) {
		CryoSpawn spawn = CryoSpawn.getSpawn(p.getName());
		if(spawn == null){
			//spawn = CryoSpawn.DEFAULT;
			//use the bedspawn system
			return false;
		}
		
		final CryoSpawn s = spawn;
		System.out.println("Player Current Server: " + Bukkit.getServerName());
		if(!s.server.equals(Bukkit.getServerName())){
			System.out.println("server name and target server name aren't equal, teleporting.");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				public void run(){
					BungeePlayerHandler.sendDeath(p, s.server);
				}
			}, 3L);
		} else {
			final Location loc2 = new Location(Bukkit.getWorld(s.world), s.x + 0.5, s.y, s.z + 0.5);
			if (checkForNotAir(loc2)){
				if(event != null){
					event.setRespawnLocation(loc2);
				} else {
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
						public void run(){
							p.teleport(loc2);
						}
					}, 3L);
				}
			} else {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						BungeePlayerHandler.sendPlayer(p, Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
					}
				}, 20L);
				Bedspawn.deleteBedspawn(event.getPlayer().getName());
			}
		}
		return true;
	}
	public static boolean checkForNotAir(Location loc){
		boolean found = false;
		Block testblock = loc.getBlock();
		int tries = 0;
		while (!found && tries < 5){
			tries++;
			testblock = testblock.getRelative(BlockFace.DOWN);
			if (testblock.getType().isSolid()){
				found = true;
				break;
			}
		}
		return found;
	}	
}
