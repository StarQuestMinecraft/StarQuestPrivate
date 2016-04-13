package net.countercraft.movecraft.crafttransfer.utils.transfer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.CraftTransferData;
import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;

import net.countercraft.movecraft.utils.BorderUtils;
import net.countercraft.movecraft.utils.LocationUtils;

public class BungeeCraftConstructor {
	//called externally, returns location of ship sign
	public static SerializableLocation calculateAndBuild(TransferData transferData) {
		SerializableLocation newDestinationLocation = getUnobstructedLocation(transferData);
		//Tests if build was successful
		if(buildCraftAtLocation(transferData.getCraftData(), newDestinationLocation.getLocation())) {
			System.out.println("Successfully built craft for pilot " + transferData.getPilot());
			return newDestinationLocation;
		}
		return null;
	}
	//called externally, removes old copy of craft
	public static void findAndRemove(String pilot) {
		TransferData transferData = readData(pilot);
		if(removeCraftAtLocation(transferData.getCraftData(), transferData.getOriginalSignLocation())) {
			System.out.println("Successfully removed old craft for pilot " + pilot);
		}
	}
	//returns true if any blocks will be obstructed
	private static boolean isObstructed(ArrayList<CraftTransferData> craftTransferData, SerializableLocation signLocation) {
		for(CraftTransferData craftData : craftTransferData) {
			String name = signLocation.getWorldName();
			double x = craftData.getRelativeX() + signLocation.getX();
			double y = craftData.getRelativeY() + signLocation.getY();
			double z = craftData.getRelativeZ() + signLocation.getZ();
			Location blockLocation = new Location(Bukkit.getWorld(name), x, y, z);
			//if not air
			if(!(blockLocation.getBlock().getTypeId() == 0)) {
				return true;
			}
		}
		return false;
	}
	//returns the optimal unobstructed location for the craft to be built at
	private static SerializableLocation getUnobstructedLocation(TransferData transferData) {
		SerializableLocation signLocation = transferData.getDestinationLocation();
		SerializableLocation oldLocation = transferData.getOriginalSignLocation();
		if(!isObstructed(transferData.getCraftData(), signLocation)) {
			return signLocation;
		}
		int count = 0;
		//creates shallow copy
		SerializableLocation newDestination = signLocation.copy();
		double angle = 0;
		boolean reversed = false;
		//if this is a space server
		if(LocationUtils.spaceCheck(newDestination.getWorldName())) {
			//If the jump came from a space server
			if(!LocationUtils.spaceCheck(oldLocation.getWorldName())) {
				angle = LocationUtils.getAngleFromGivenPointTo(LocationUtils.locationOfPlanet(oldLocation.getWorldName()), newDestination.getLocation());
			}
		}
		else {
			angle = LocationUtils.getAngleFromOriginTo(newDestination.getLocation());
		}
		double xOffset = Math.sin(angle) * 10;
		double yOffset = 0;
		double zOffset = Math.cos(angle) * 10;
		//returns true when it finds an unobstructed location
		while((count < 20) && (isObstructed(transferData.getCraftData(), newDestination))) {
			count++;
			if(!reversed) {
				newDestination.offsetCoordinatesBy(xOffset, yOffset, zOffset);
			} 
			else {
				newDestination.offsetCoordinatesBy(-1 * xOffset, -1 * yOffset, -1 * zOffset);
			}
			
			if(!(BorderUtils.isWithinBorder((int) newDestination.getX(), (int) newDestination.getZ())) && !(reversed)){
				reversed = true;
				count = 0;
				newDestination = signLocation.copy();
			}
		}
		if(!isObstructed(transferData.getCraftData(), newDestination)) {
			return newDestination;
		}
		return null;
	}
	//attempts to build craft at given location. returns true if build successful
	private static boolean buildCraftAtLocation(ArrayList<CraftTransferData> craftTransferData, Location newDestinationLocation) {
		for(CraftTransferData craftData : craftTransferData) {
			World w = newDestinationLocation.getWorld();
			double x = craftData.getRelativeX() + newDestinationLocation.getX();
			double y = craftData.getRelativeY() + newDestinationLocation.getY();
			double z = craftData.getRelativeZ() + newDestinationLocation.getZ();
			Location blockLocation = new Location(w, x, y, z);
			//Tests that craft is still not obstructed
			if(!(blockLocation.getBlock().getTypeId() == 0)) {
				return false;
			}
			//appropriately sets blocks
			int id = craftData.getID();
			byte data = craftData.getData();
			boolean isSign = craftData.isSign();
			boolean hasInventory = craftData.hasInventory();
			blockLocation.getBlock().setTypeIdAndData(id, data, false);
			//sets inventory contents
			if(hasInventory) {
				InventoryHolder i = (InventoryHolder) blockLocation.getBlock().getState();
				Inventory inv = craftData.getInventory();
				i.getInventory().setContents(inv.getContents());
			}
			//sets sign lines
			else if(isSign) {
				Sign s = (Sign) blockLocation.getBlock().getState();
				String[] lines = craftData.getSignLines();
				s.setLine(0, lines[0]);
				s.setLine(1, lines[1]);
				s.setLine(2, lines[2]);
				s.setLine(3, lines[3]);
			}
		}
		return true;
	}
	//attempts to remove craft at given location, returns true if successful
	private static boolean removeCraftAtLocation(ArrayList<CraftTransferData> craftTransferData, SerializableLocation oldShipSignLocation) {
		World world = Bukkit.getWorld(oldShipSignLocation.getWorldName());
		for(CraftTransferData craftData : craftTransferData) {
			SerializableLocation l = oldShipSignLocation.copy();
			l.offsetCoordinatesBy(craftData.getRelativeX(), craftData.getRelativeY(), craftData.getRelativeZ());
			Location blockLocation = l.getLocation();
			world.getBlockAt(blockLocation).setTypeId(0);
		}
		return true;
	}
	//grabs transferdata from db
	private static TransferData readData(String pilot) {
		return Movecraft.getInstance().getSQLDatabase().readData(pilot);
	}
}
