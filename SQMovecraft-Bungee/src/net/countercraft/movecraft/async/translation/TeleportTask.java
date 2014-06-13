package net.countercraft.movecraft.async.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TeleportTask {
	public static boolean worldJump(Player pilot, Craft c, Location locto) {

		// calculate the difference in x and difference in y of the current
		// location to the target location
		Location startloc = pilot.getLocation();
		int dX = getdX(startloc, locto);
		int dY = getdY(startloc, locto);
		int dZ = getdZ(startloc, locto);

		// load the block list
		MovecraftLocation[] blocksList = c.getBlockList();

		// check for destination obstructions and if obstructed quit.
		boolean obstructed = destinationObstructed(blocksList, locto, dX, dY, dZ);
		if (obstructed) {
			return false;
		}

		// make a new output file
		// SimpleDateFormat sdf = new SimpleDateFormat("dd:MM,HH:mm");
		// Date resultdate = new Date(System.currentTimeMillis());

		// File statText = Movecraft.getInstance().getDataFolder();
		// ( !statText.exists() ) {
		// statText.mkdirs();
		// }
		// String filePrefix = pilot.getName() + "@" +
		// System.currentTimeMillis();
		// File ourfile = File.createTempFile(filePrefix, ".txt", statText);
		// FileOutputStream is = new FileOutputStream(ourfile);
		// OutputStreamWriter osw = new OutputStreamWriter(is);
		// BufferedWriter w = new BufferedWriter(osw);

		// w.write("===============================");
		// w.newLine();
		// w.write("WORLDJUMP INITIATED");
		// w.newLine();
		// w.write("Player: " + pilot.getName());
		// w.newLine();
		// w.write("Craft Type: " + c.getType());
		// w.newLine();
		// w.write("Target World: " + locto.getWorld().getName());
		// w.newLine();
		// w.write("Target Coords: " + locto.getX() + "," + locto.getY() + "," +
		// locto.getZ());
		// w.newLine();
		// w.write("Blocks List Length: " + blocksList.length);
		// w.newLine();
		// w.write("===============================")

		// w.write("BLOCK LIST PREVIOUS");
		// w.newLine();
		// for (MovecraftLocation l : blocksList){
		// .write(wrld.getBlockTypeIdAt(l.getX(), l.getY(), l.getZ()) + "@" +
		// l.getX()+ "," + l.getY() + "," + l.getZ());
		// .newLine();
		// }

		// store old hitbox and data so we can retrieve the players later
		int[][][] oldHitBox = c.getHitBox();
		int oldMinX = c.getMinX();
		int oldMinZ = c.getMinZ();

		// Player List
		HashMap<Player, Location> players = new HashMap<Player, Location>();

		// fill the player list
		for (UUID s : c.playersRiding) {
			Player p = Bukkit.getPlayer(s);
			if (p != null) {
				if (MathUtils.playerIsWithinBoundingPolygon(oldHitBox, oldMinX, oldMinZ, MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
					Location loc = p.getLocation();
					Location target = new Location(locto.getWorld(), loc.getX() + dX, loc.getY() + dY, loc.getZ() + dZ, loc.getYaw(), loc.getPitch());
					players.put(p, target);
				}
			}
		}

		// remove the old craft
		CraftManager.getInstance().removeCraft(c);

		// w.write("BEGIN teleportBlocks: " + System.currentTimeMillis());
		// w.newLine();
		// teleport the craft's blocks
		for (Player p : players.keySet()) {
			p.teleport(players.get(p));
		}

		teleportBlocks(blocksList, locto, dX, dY, dZ, c.getW());

		// w.write("END teleportBlocks: " + System.currentTimeMillis());
		// w.newLine();

		// teleport the players again bcuz yolo
		for (Player p : players.keySet()) {
			p.teleport(players.get(p));
		}
		// update bedspawns
		updateBedspawns(c.playersWithBedSpawnsOnShip, locto, dX, dY, dZ);
		// w.newLine();

		// pilot the new craft
		Location loc = pilot.getLocation();
		MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
		Craft newCraft = new Craft(c.getType(), loc.getWorld());
		newCraft.detect(pilot.getName(), startPoint);

		// all done!
		// w.write("===============================");
		// w.newLine();
		// w.write("WORLDJUMP FINISHED");
		// w.newLine();
		// w.write("Player: " + pilot.getName());
		// w.newLine();
		// w.write("Craft Type: " + newCraft.getType());
		// w.newLine();
		// w.write("Blocks List Length: " + newCraft.getBlockList().length);
		// w.newLine();
		// w.write("===============================");
		// w.close();
		return true;
	}

	// block processing method
	@SuppressWarnings("deprecation")
	private static void teleportBlocks(MovecraftLocation[] blocksList, Location locto, int dX, int dY, int dZ, World w) {

		// Iterate through the blocks list
		for (int i = 0; i < blocksList.length; i++) {
			MovecraftLocation oldLoc = blocksList[i];

			final int oldID = w.getBlockTypeIdAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ());
			final byte oldData = w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).getData();
			// wrt.write("Index: " + i + " ID: " + oldID + " Data: " + oldData);
			// .newLine();
			Location newLoc = new Location(locto.getWorld(), oldLoc.getX() + dX, oldLoc.getY() + dY, oldLoc.getZ() + dZ);
			if (!newLoc.getChunk().isLoaded()) {
				newLoc.getChunk().load();
			}
			processSingleBlock(oldLoc, newLoc, w, oldID, oldData);
		}
	}

	// single block processor
	@SuppressWarnings("deprecation")
	private static void processSingleBlock(MovecraftLocation oldLoc, Location newLoc, World w, int oldID, byte oldData) {

		// set the new block to the correct type id and data
		// .write("Id updated to " + oldID + " and data updated to " + oldData +
		// " at " + newLoc.getX() + "," + newLoc.getY() + "," + newLoc.getZ());
		// wrt.newLine();
		newLoc.getBlock().setTypeIdAndData(oldID, oldData, false);

		// if it's a sign update the sign.
		if (oldID == 63 || oldID == 68) {
			// wrt.write("Found to be a sign");
			// wrt.newLine();
			String[] lines = ((Sign) w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).getState()).getLines();
			Sign ns = (Sign) newLoc.getBlock().getState();
			// wrt.write("    " + lines[0]);
			// wrt.write("    " + lines[1]);
			// wrt.write("    " + lines[2]);
			// wrt.write("    " + lines[3]);
			ns.setLine(0, lines[0]);
			ns.setLine(1, lines[1]);
			ns.setLine(2, lines[2]);
			ns.setLine(3, lines[3]);
			ns.update();
		}
		// if it has an inventory update the inventory.
		if (oldID == 23) {
			// wrt.write("Found to be a dispenser.");
			// wrt.newLine();
			Dispenser d = (Dispenser) w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).getState();
			Inventory inv = d.getInventory();
			((Dispenser) newLoc.getBlock().getState()).getInventory().setContents(inv.getContents());
			d.getInventory().clear();
		}
		if (oldID == 158) {
			// wrt.write("Found to be a dropper.");
			// wrt.newLine();
			Dropper d = (Dropper) w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).getState();
			Inventory inv = d.getInventory();
			((Dropper) newLoc.getBlock().getState()).getInventory().setContents(inv.getContents());
			d.getInventory().clear();
		}
		if (oldID == 61 || oldID == 62) {
			// wrt.write("Found to be a furnace.");
			// wrt.newLine();
			Furnace d = (Furnace) w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).getState();
			Inventory inv = d.getInventory();
			((Furnace) newLoc.getBlock().getState()).getInventory().setContents(inv.getContents());
			d.getInventory().clear();
		}
		// update the storage crates
		/*
		 * if (oldID == 121){ //wrt.write("Found to be a storage crate.");
		 * //wrt.newLine(); Inventory inventory =
		 * StorageChestItem.getInventoryOfCrateAtLocation(oldLoc, w);
		 * StorageChestItem.removeInventoryAtLocation(w, oldLoc);
		 * StorageChestItem.setInventoryOfCrateAtLocation(inventory, new
		 * MovecraftLocation(newLoc.getBlockX(), newLoc.getBlockY(),
		 * newLoc.getBlockZ()), newLoc.getWorld()); }
		 */

		// wrt.write("Setting old block to air.");
		// wrt.newLine();
		// set the old block to air
		w.getBlockAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ()).setTypeIdAndData(0, (byte) 0, false);

	}

	// helping method for updating bedspawns
	private static void updateBedspawns(ArrayList<String> list, Location locto, int dX, int dY, int dZ) {
		for (String s : list) {
			Bedspawn b = Bedspawn.getBedspawn(s);
			b.world = locto.getWorld().getName();
			b.x = b.x + dX;
			b.y = b.y + dY;
			b.z = b.z + dZ;
		}
	}

	// helping method to check for destination obstruction
	@SuppressWarnings("deprecation")
	public static boolean destinationObstructed(MovecraftLocation[] blocksList, Location locto, int dX, int dY, int dZ) {
		for (int i = 0; i < blocksList.length; i++) {
			MovecraftLocation oldLoc = blocksList[i];
			Location newLoc = new Location(locto.getWorld(), oldLoc.getX() + dX, oldLoc.getY() + dY, oldLoc.getZ() + dZ);
			int testID = newLoc.getWorld().getBlockTypeIdAt(newLoc);
			if (testID != 0) {
				return true;
			}
		}
		return false;
	}

	// helping methods for calculating differences in X, Y, and Z
	private static int getdX(Location from, Location to) {
		int fromX = from.getBlockX();
		int toX = to.getBlockX();
		return (toX - fromX);

	}

	// dY calculator
	private static int getdY(Location from, Location to) {
		int fromY = from.getBlockY();
		int toY = to.getBlockY();
		return (toY - fromY);
	}

	// dZ calculator
	private static int getdZ(Location from, Location to) {
		int fromZ = from.getBlockZ();
		int toZ = to.getBlockZ();
		return (toZ - fromZ);
	}

}
