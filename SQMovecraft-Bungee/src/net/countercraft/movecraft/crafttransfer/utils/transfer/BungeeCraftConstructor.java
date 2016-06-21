package net.countercraft.movecraft.crafttransfer.utils.transfer;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
		System.out.println("In calculateAndBuild()");
		SerializableLocation newDestinationLocation = getUnobstructedLocation(transferData);
		System.out.println("newDestinationLocation: " + newDestinationLocation);
		System.out.println("newDestinationLocation.getLocation(): " + newDestinationLocation.getLocation());
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
	private static boolean isObstructed(final ArrayList<CraftTransferData> craftTransferData, final SerializableLocation signLocation) {
		System.out.println("In isObstructed()");
		//Does some stuff with concurrent synchronous calls to avoid an IllegalStateException
		Future<Boolean> isObstructed = Bukkit.getScheduler().callSyncMethod(Movecraft.getInstance(), new Callable<Boolean>() {
			public Boolean call() {
				System.out.println("In callable of death");
				String name = signLocation.getWorldName();
				System.out.println("worldname: " + name);
				System.out.println("world is null: " + Bukkit.getWorld(name) == null);
				System.out.println("Sign x: " + signLocation.getX());
				System.out.println("Sign y: " + signLocation.getY());
				System.out.println("Sign z: " + signLocation.getZ());
				for(CraftTransferData craftData : craftTransferData) {
					Double x = craftData.getRelativeX() + signLocation.getX();
					Double y = craftData.getRelativeY() + signLocation.getY();
					Double z = craftData.getRelativeZ() + signLocation.getZ();
					//gets the block at the given location
					if(!(Bukkit.getWorld(name).getBlockAt(x.intValue(), y.intValue(), z.intValue()).getTypeId() == 0)) {
						return true;
					}
				}
				return false;
			}
		});
		//Checks if it's obstructed once callSyncMethod returns
		try {
			if(isObstructed.get().booleanValue() == true) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	private static boolean buildCraftAtLocation(final ArrayList<CraftTransferData> craftTransferData, Location newDestinationLocation) {
		final String worldName = newDestinationLocation.getWorld().getName();
		final Double destinationX = newDestinationLocation.getX();
		final Double destinationY = newDestinationLocation.getY();
		final Double destinationZ = newDestinationLocation.getZ();
		//Sets all of the blocks, and checks if the craft is obstructed
		Future<Boolean> isObstructed = Bukkit.getScheduler().callSyncMethod(Movecraft.getInstance(), new Callable<Boolean>() {
			public Boolean call() {
				for(CraftTransferData craftData : craftTransferData) {
					final Double x = craftData.getRelativeX() + destinationX;
					final Double y = craftData.getRelativeY() + destinationY;
					final Double z = craftData.getRelativeZ() + destinationZ;
					//Tests that craft is still not obstructed
					if(!(Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue()).getTypeId() == 0)) {
						return true;
					}
					//appropriately sets blocks
					final int id = craftData.getID();
					final byte data = craftData.getData();
					Block block = Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue());
					block.setTypeIdAndData(id, data, false);
					if(craftData.isSign()) {
						String[] lines = craftData.getSignLines();
						Sign sign = (Sign) block.getState();
						for(int i = 0; i < lines.length; i++) {
							System.out.println(lines[i]);
							sign.setLine(i, lines[i]);
						}
						sign.update();
					}
					else if(craftData.hasInventory()) {
						InventoryHolder i = (InventoryHolder) block.getState();
						craftData.getInventory().unpack(i.getInventory());
					}
				}
				return false;
			}
		});
		//Closes builder and tells the program to cancel the transfer if the craft was obstructed
		try {
			if(isObstructed.get().booleanValue()) {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	//attempts to remove craft at given location, returns true if successful
	private static boolean removeCraftAtLocation(final ArrayList<CraftTransferData> craftTransferData, final SerializableLocation oldShipSignLocation) {
		final World world = Bukkit.getWorld(oldShipSignLocation.getWorldName());
		Bukkit.getScheduler().runTask(Movecraft.getInstance(), new Runnable() {
			public void run() {
				for(CraftTransferData craftData : craftTransferData) {
					SerializableLocation l = oldShipSignLocation.copy();
					l.offsetCoordinatesBy(craftData.getRelativeX(), craftData.getRelativeY(), craftData.getRelativeZ());
					Location blockLocation = l.getLocation();
					world.getBlockAt(blockLocation).setTypeId(0);
				}
			}
		});
		return true;
	}
	//grabs transferdata from db
	private static TransferData readData(String pilot) {
		return Movecraft.getInstance().getSQLDatabase().readData(pilot);
	}
}