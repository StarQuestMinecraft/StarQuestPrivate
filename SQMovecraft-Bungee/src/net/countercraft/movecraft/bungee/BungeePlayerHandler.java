
package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.higashiyama.george.CardboardBox.Knapsack;

public class BungeePlayerHandler {

	public static ArrayList<PlayerTeleport> teleportQueue = new ArrayList<PlayerTeleport>();

	public static void sendPlayer(Player p, String targetserver, String world, int X, int Y, int Z) {

		sendPlayerCoordinateData(p, targetserver, world, X, Y, Z);
		sendPlayer(p, targetserver);
	}

	public static void sendPlayer(Player p, String targetserver) {

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("Connect");
			out.writeUTF(targetserver);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
	}

	private static void sendPlayerCoordinateData(Player p, String targetserver, String world, int X, int Y, int Z) {

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Location l = p.getLocation();
		// send player data of where they should be teleported to
		try {
			out.writeUTF("Forward");
			out.writeUTF(targetserver);
			out.writeUTF("movecraftPlayer");

			ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
			DataOutputStream msgout = new DataOutputStream(msgbytes);

			writePlayerData(msgout, p, targetserver, world, X, Y, Z);

			byte[] outbytes = msgbytes.toByteArray();
			out.writeShort(outbytes.length);
			out.write(outbytes);
			p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writePlayerData(DataOutputStream msgout, Player p, String targetserver, String world, int X, int Y, int Z) throws IOException {

		Location l = p.getLocation();
		msgout.writeUTF(world);
		msgout.writeUTF(p.getName());
		msgout.writeInt(X);
		msgout.writeInt(Y);
		msgout.writeInt(Z);
		msgout.writeDouble(l.getYaw());
		msgout.writeDouble(l.getPitch());
		InventoryUtils.writePlayer(msgout, p);
		msgout.writeInt(gameModeToInt(p.getGameMode()));
	}

	public static void wipePlayerInventory(Player p) {

		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public static void recievePlayer(DataInputStream in) {

		try {
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);

			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));

			PlayerTeleport t = recievePlayerTeleport(msgin);

			if (Bukkit.getServer().getPlayer(t.playername) != null) {
				t.execute();
			} else {
				teleportQueue.add(t);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PlayerTeleport recievePlayerTeleport(DataInputStream msgin) throws IOException {

		String worldname = msgin.readUTF();
		String playername = msgin.readUTF();
		int coordX = msgin.readInt();
		int coordY = msgin.readInt();
		int coordZ = msgin.readInt();
		double yaw = msgin.readDouble();
		double pitch = msgin.readDouble();
		Knapsack playerKnap = InventoryUtils.readPlayer(msgin);
		GameMode gamemode = intToGameMode(msgin.readInt());

		if (coordY > 256) {
			Location loc = Bukkit.getServer().getWorld(worldname).getSpawnLocation();
			coordX = loc.getBlockX();
			coordY = loc.getBlockY();
			coordZ = loc.getBlockZ();
		}
		PlayerTeleport t = new PlayerTeleport(playername, worldname, coordX, coordY, coordZ, yaw, pitch, playerKnap, gamemode);
		return t;
	}

	private static int gameModeToInt(GameMode g) {

		switch (g) {
			case SURVIVAL:
				return 0;
			case CREATIVE:
				return 1;
			case ADVENTURE:
				return 2;
			default:
				return 0;
		}
	}

	private static GameMode intToGameMode(int g) {

		switch (g) {
			case 0:
				return GameMode.SURVIVAL;
			case 1:
				return GameMode.CREATIVE;
			case 2:
				return GameMode.ADVENTURE;
			default:
				return GameMode.SURVIVAL;
		}
	}

	public static PlayerTeleport teleportFromString(String name) {

		for (PlayerTeleport t : teleportQueue) {
			if (t.playername.equals(name)) {
				return t;
			}
		}
		return null;
	}

	private static Inventory armorInv(Player p) {

		Inventory armorInv = Bukkit.createInventory(p, 9);
		ItemStack[] armor = p.getInventory().getArmorContents();
		for (int i = 0; i < armor.length; i++) {
			armorInv.setItem(i, armor[i]);
		}
		return armorInv;
	}
}
