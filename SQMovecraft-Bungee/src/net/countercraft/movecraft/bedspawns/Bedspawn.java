package net.countercraft.movecraft.bedspawns;

import java.sql.*;
import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.utils.MovecraftLocation;

public class Bedspawn {
	public String player;
	public String server;
	public String world;
	public int x, y, z;
	public static String driver = "com.mysql.jdbc.Driver";
	public static String hostname = "localhost";
	public static String port = "3306";
	public static String db_name = "minecraft";
	public static String username = "minecraft";
	public static String password = "R3b!rth!ng";
	public static Connection cntx = null;
	public static String dsn = ("jdbc:mysql://" + hostname + ":" + port + "/" + db_name);
	public final static Bedspawn DEFAULT = Movecraft.getInstance().getDefaultBedspawn();

	public Bedspawn(String player, String server, String world, int x, int y, int z) {
		this.player = player;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return " BEDSPAWN: Player = " + player + " & server = " + server + " & world = " + world + " & coords = " + x + "," + y + "," + z + ".";
	}

	public static void setUp() {
		// called from onEnable

		String bedspawn_table = "CREATE TABLE IF NOT EXISTS " + "BEDSPAWNS (" + "`name` VARCHAR(32) NOT NULL," + "`server` VARCHAR(32) DEFAULT NULL," + "`world` VARCHAR(32) DEFAULT NULL,"
				+ "`x` int(11) DEFAULT 0," + "`y` int(11) DEFAULT 0," + "`z` int(11) DEFAULT 0," + "PRIMARY KEY (`name`)" + ")";
		getContext();
		try {
			Driver driver = (Driver) Class.forName(Bedspawn.driver).newInstance();
			DriverManager.registerDriver(driver);
		} catch (Exception e) {
			System.out.println("[SQBedSpawns] Driver error: " + e);
		}
		Statement s = null;
		try {
			s = cntx.createStatement();
			s.executeUpdate(bedspawn_table);
			s.close();
			System.out.println("[SQBedSpawn] Table check/creation sucessful");
		} catch (SQLException ee) {
			System.out.println("[SQBedSpawn] Table Creation Error");
		} finally {
			close(s);
		}

	}

	public static Bedspawn getBedspawn(String player) {
		return loadBedspawn(player);
	}

	public static ArrayList<Bedspawn> getAllBedspawns() {
		return loadBedspawnList();
	}
	
	public static ArrayList<Bedspawn> loadBedspawnList(MovecraftLocation location, String world) {
		//TODO This method needs to be redone for a smaller radius!
		int craftX = location.getX() - 1;
		int craftZ = location.getZ() - 1;
		ArrayList<Bedspawn> bedspawnList = new ArrayList<Bedspawn>();
		String playerName;
		String playerWorld;
		String playerServer;
		int playerX;
		int playerY;
		int playerZ;
		System.out.print("[SQBedSpawn] Loading bedspawns");
		if (!getContext()) {
			System.out.println("something is wrong!");
			return null;
		}
		PreparedStatement s = null;
		try {
			s = cntx.prepareStatement("SELECT * FROM BEDSPAWNS WHERE `world` = ? AND `x` > ? AND `x` < (? + 500) AND `z` > ? AND `z` < (? + 500)");
			s.setString(1, world);
			s.setInt(2, craftX);
			s.setInt(3, craftX);
			s.setInt(4, craftZ);
			s.setInt(5, craftZ);
			ResultSet rs = s.executeQuery();

			while (rs.next()) {
				playerName = rs.getString("name");
				playerWorld = rs.getString("world");
				playerServer = rs.getString("server");
				playerX = rs.getInt("x");
				playerY = rs.getInt("y");
				playerZ = rs.getInt("z");
				Bedspawn singleBedSpawn = new Bedspawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ);
				bedspawnList.add(singleBedSpawn);
			}
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return bedspawnList;
	}



	public Bedspawn clone() {
		return new Bedspawn(player, server, world, x, y, z);
	}

	public static Bedspawn getDefault(String player) {
		Bedspawn b = DEFAULT.clone();
		b.player = player;
		return b;
	}

	public static void deleteBedspawn(String player) {
		PreparedStatement s = null;
		try {
			s = cntx.prepareStatement("DELETE FROM BEDSPAWNS WHERE `name` = ?");
			s.setString(1, player);
			s.execute();
			s.close();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			close(s);
		}
	}

	public static boolean getContext() {

		try {
			if (cntx == null || cntx.isClosed() || !cntx.isValid(1)) {
				if (cntx != null && !cntx.isClosed()) {
					try {
						cntx.close();
					} catch (SQLException e) {
						System.out.print("Exception caught");
					}
					cntx = null;
				}
				if ((Bedspawn.username.equalsIgnoreCase("")) && (Bedspawn.password.equalsIgnoreCase(""))) {
					cntx = DriverManager.getConnection(Bedspawn.dsn);
				} else
					cntx = DriverManager.getConnection(Bedspawn.dsn, Bedspawn.username, Bedspawn.password);

				if (cntx == null || cntx.isClosed())
					return false;
			}

			return true;
		} catch (SQLException e) {
			System.out.print("Error could not Connect to db " + Bedspawn.dsn + ": " + e.getMessage());
		}
		return false;
	}

	public static ArrayList<Bedspawn> loadBedspawnList() {
		ArrayList<Bedspawn> bedspawnList = new ArrayList<Bedspawn>();
		String playerName;
		String playerWorld;
		String playerServer;
		int playerX;
		int playerY;
		int playerZ;
		System.out.print("[SQBedSpawn] Loading bedspawns");
		if (!getContext()) {
			System.out.println("something is wrong!");
			return null;
		}
		Statement s = null;
		try {
			s = cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM BEDSPAWNS");

			while (rs.next()) {
				playerName = rs.getString("name");
				playerWorld = rs.getString("world");
				playerServer = rs.getString("server");
				playerX = rs.getInt("x");
				playerY = rs.getInt("y");
				playerZ = rs.getInt("z");
				Bedspawn singleBedSpawn = new Bedspawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ);
				bedspawnList.add(singleBedSpawn);
			}
		} catch (SQLException e) {
			System.out.print("[SQBedSpawn] SQL Error" + e.getMessage());
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return bedspawnList;
	}

	public static Bedspawn loadBedspawn(String player) {
		String playerName;
		String playerWorld;
		String playerServer;
		int playerX;
		int playerY;
		int playerZ;
		System.out.print("[SQBedSpawn] Loading bedspawn"); // TODO: Debugging
															// ish
		if (!getContext())
			System.out.println("[SQBedSpawn] context error");
		Bedspawn singleBedSpawn = null;
		Statement s = null;
		try {
			s = cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM BEDSPAWNS WHERE name = " + "\'" + player + "\'");

			if (rs.next()) {
				playerName = rs.getString("name");
				playerWorld = rs.getString("world");
				playerServer = rs.getString("server");
				playerX = rs.getInt("x");
				playerY = rs.getInt("y");
				playerZ = rs.getInt("z");
				singleBedSpawn = new Bedspawn(playerName, playerServer, playerWorld, playerX, playerY, playerZ);
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.print("[SQBedSpawn] SQL Error (Unknown)");
			e.printStackTrace();
		} finally {
			close(s);
		}
		return singleBedSpawn;
	}

	public static void saveBedspawn(Bedspawn b) {

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
			s = cntx.prepareStatement("UPDATE BEDSPAWNS SET server = ?, world = ?, x = ?, y = ?, z = ? WHERE NAME = ?");
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

	public static void saveNewBedspawn(Bedspawn b) {

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
			s = cntx.prepareStatement("INSERT INTO BEDSPAWNS (name, server, world, x, y, z) values (?,?,?,?,?,?)");
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

	}

	public static boolean hasKey(Bedspawn b) {

		String playerName = b.player;

		if (!getContext()) {
			System.out.println("something is wrong!");
			return false;
		}
		Statement s = null;
		try {
			s = cntx.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM BEDSPAWNS WHERE name = " + "\'" + playerName + "\'");

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

	// saveBedspawn(String player, String server, String world, int x, int y,
	// int z)
}