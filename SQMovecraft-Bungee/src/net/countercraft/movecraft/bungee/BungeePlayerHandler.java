
package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MathUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import us.higashiyama.george.CardboardBox.Knapsack;

public class BungeePlayerHandler {

//@formatting:off
	public static ArrayList<PlayerTeleport> teleportQueue = new ArrayList<PlayerTeleport>();
	static HashMap<String, Craft> pilotQueue = new HashMap<String, Craft>();
	
	public static void onLogin(final Player p){
		final String playername = p.getName();
		for(int i = 0; i < teleportQueue.size(); i++){
			final PlayerTeleport t = teleportQueue.get(i);
			if(playername.equals(t.playername)){
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						t.execute();
					}
				}, 1L);
				final Craft c = pilotQueue.get(playername);
				if(c != null){
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
						public void run(){
							if(c.originalPilotLoc == null){
								c.detect(playername, MathUtils.bukkit2MovecraftLoc(p.getLocation()));
							} else {
								c.detect(playername, MathUtils.bukkit2MovecraftLoc(c.originalPilotLoc));
							}
						}
					}, 2L);
				}
				break;
			}
		}
	}
	public static void sendPlayer(Player p, String targetserver, String world, int X, int Y, int Z){
		sendPlayer(p, targetserver, world, X, Y, Z, false);
	}

	public static void sendPlayer(Player p, String targetserver, String world, int X, int Y, int Z, boolean isBedspawn) {

		sendPlayerCoordinateData(p, targetserver, world, X, Y, Z, isBedspawn);
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

	private static void sendPlayerCoordinateData(Player p, String targetserver, String world, int X, int Y, int Z, boolean isBedspawn) {

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		// send player data of where they should be teleported to
		try {
			out.writeUTF("Forward");
			out.writeUTF(targetserver);
			out.writeUTF("movecraftPlayer");

			ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
			DataOutputStream msgout = new DataOutputStream(msgbytes);

			writePlayerData(msgout, p, targetserver, world, X, Y, Z, isBedspawn);

			byte[] outbytes = msgbytes.toByteArray();
			out.writeShort(outbytes.length);
			out.write(outbytes);
			p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writePlayerData(DataOutputStream msgout, Player p, String targetserver, String world, int X, int Y, int Z, boolean isBedspawn) throws IOException {

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
		msgout.writeBoolean(isBedspawn);
	}
	
	public static void writePlayerData(DataOutputStream msgout, Player p, String targetserver, String world, int X, int Y, int Z) throws IOException{
		writePlayerData(msgout, p, targetserver, world, X, Y, Z, false);
	}

	public static void wipePlayerInventory(Player p) {

		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		for(PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
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
		boolean isBedspawn = msgin.readBoolean();

		if (coordY > 256) {
			Location loc = Bukkit.getServer().getWorld(worldname).getSpawnLocation();
			coordX = loc.getBlockX();
			coordY = loc.getBlockY();
			coordZ = loc.getBlockZ();
		}
		PlayerTeleport t = new PlayerTeleport(playername, worldname, coordX, coordY, coordZ, yaw, pitch, playerKnap, gamemode, isBedspawn);
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
