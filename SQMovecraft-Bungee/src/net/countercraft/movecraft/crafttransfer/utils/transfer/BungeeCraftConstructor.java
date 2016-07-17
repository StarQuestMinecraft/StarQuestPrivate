package net.countercraft.movecraft.crafttransfer.utils.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

import com.google.common.collect.Lists;

import net.countercraft.movecraft.Movecraft;

import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.crafttransfer.transferdata.CraftTransferData;
import net.countercraft.movecraft.crafttransfer.transferdata.TransferData;
import net.countercraft.movecraft.utils.BlockUtils;
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
		if(buildCraftAtLocation(transferData.getCraftData(), newDestinationLocation.getLocation(), true)) {
			System.out.println("Successfully built craft for pilot " + transferData.getPilot());
			return newDestinationLocation;
		}
		return null;
	}
	//called externally, builds craft at location, returns location of shipsign. Used for non-transferdata setup
	public static SerializableLocation calculateAndBuild(ArrayList<CraftTransferData> transferData, SerializableLocation location, boolean saveInventories, boolean testExtraLocations) {
		if(testExtraLocations) {
			SerializableLocation destinationLocation = getUnobstructedLocation(transferData, location);
			if(buildCraftAtLocation(transferData, destinationLocation.getLocation(), saveInventories)) {
				return destinationLocation;
			}
		}
		else {
			if(!isObstructed(transferData, location)) {
				if(buildCraftAtLocation(transferData, location.getLocation(), saveInventories)) {
					return location;
				}
			}
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
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	//returns the optimal unobstructed location for the craft to be built at
	private static SerializableLocation getUnobstructedLocation(TransferData transferData) {
		SerializableLocation signLocation = transferData.getDestinationLocation();
		System.out.println("signLocation: " + signLocation);
		SerializableLocation oldLocation = transferData.getOriginalSignLocation();
		System.out.println("oldLocation: " + oldLocation);
		if(!isObstructed(transferData.getCraftData(), signLocation)) {
			return signLocation;
		}
		int count = 0;
		//creates shallow copy
		SerializableLocation newDestination = signLocation.copy();
		double angle = 0;
		boolean reversed = false;
		//if this is a space server
		boolean isInSpace = false;
		if(LocationUtils.spaceCheck(newDestination.getWorldName())) {
			isInSpace = true;
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
		while((count < 512) && ((isObstructed(transferData.getCraftData(), newDestination)) || ((isInSpace) && (LocationUtils.isInRegionHitbox(newDestination.getLocation()))))) {
			System.out.println("Count: " + count);
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
		if((!isObstructed(transferData.getCraftData(), newDestination)) && ((!isInSpace) || ((isInSpace) && (!LocationUtils.isInRegionHitbox(newDestination.getLocation()))))) {
			return newDestination;
		}
		return null;
	}
	
	private static SerializableLocation getUnobstructedLocation(ArrayList<CraftTransferData> transferData, SerializableLocation destinationLocation) {
		if(!isObstructed(transferData, destinationLocation)) {
			return destinationLocation;
		}
		int count = 0;
		//creates shallow copy
		SerializableLocation newDestination = destinationLocation.copy();
		double angle = 0;
		boolean reversed = false;
		//if this is a space server
		boolean isInSpace = false;
		if(LocationUtils.spaceCheck(newDestination.getWorldName())) {
			isInSpace = true;
			//If the jump came from a space server
			if(!LocationUtils.spaceCheck(destinationLocation.getWorldName())) {
				angle = LocationUtils.getAngleFromGivenPointTo(LocationUtils.locationOfPlanet(destinationLocation.getWorldName()), newDestination.getLocation());
			}
		}
		else {
			angle = LocationUtils.getAngleFromOriginTo(newDestination.getLocation());
		}
		double xOffset = Math.sin(angle) * 10;
		double yOffset = 0;
		double zOffset = Math.cos(angle) * 10;
		//returns true when it finds an unobstructed location
		while((count < 512) && (isObstructed(transferData, newDestination))) {
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
				newDestination = destinationLocation.copy();
			}
		}
		if((!isObstructed(transferData, newDestination)) && ((!isInSpace) || ((isInSpace) && (!LocationUtils.isInRegionHitbox(newDestination.getLocation()))))) {
			return newDestination;
		}
		return null;
	}
	//attempts to build craft at given location. returns true if build successful
	private static synchronized boolean buildCraftAtLocation(final ArrayList<CraftTransferData> craftTransferData, Location newDestinationLocation, final boolean saveInventories) {
		final String worldName = newDestinationLocation.getWorld().getName();
		final Double destinationX = newDestinationLocation.getX();
		final Double destinationY = newDestinationLocation.getY();
		final Double destinationZ = newDestinationLocation.getZ();
		List<List<CraftTransferData>> partitionedList = Lists.partition(craftTransferData, 2000);
		//Sets all of the blocks
		long delay = 0;
		final List<CraftTransferData> fragiles = new ArrayList<CraftTransferData>();
		for(final List<CraftTransferData> data : partitionedList) {
			Bukkit.getScheduler().runTaskLater(Movecraft.getInstance(), new Runnable() {
				public void run() {
					for(CraftTransferData craftData : data) {
						final Double x = craftData.getRelativeX() + destinationX;
						final Double y = craftData.getRelativeY() + destinationY;
						final Double z = craftData.getRelativeZ() + destinationZ;
						if(craftData.isSign()) {
							System.out.println("Is sign");
						}
						//Tests that craft is still not obstructed
						if(!(Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue()).getTypeId() == 0)) {
							System.out.println("Om nom nom ship");
						}
						//if block is attached, do later
						if((!craftData.hasInventory()) && (BlockUtils.blockIsFragile(craftData.getID))) {
							fragiles.add(craftData);
						}
						else {
							//appropriately sets blocks
							final int id = craftData.getID();
							final byte data = craftData.getData();
							Block block = Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue());
							block.setTypeIdAndData(id, data, false);
							//sets container inventories
							if((craftData.hasInventory()) && (saveInventories)) {
								InventoryHolder i = (InventoryHolder) block.getState();
								craftData.getInventory().unpack(i.getInventory());
							}
						}
					}
				}
			}, delay);
			delay += 2;
		}
		//does the fragile blocks
		Bukkit.getScheduler().runTaskLater(Movecraft.getInstance(), new Runnable() {
			public void run() {
				for(CraftTransferData craftData : fragiles) {
					final Double x = craftData.getRelativeX() + destinationX;
					final Double y = craftData.getRelativeY() + destinationY;
					final Double z = craftData.getRelativeZ() + destinationZ;
					//Tests that craft is still not obstructed
					if(!(Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue()).getTypeId() == 0)) {
						System.out.println("Om nom nom ship");
					}
					//appropriately sets blocks
					final int id = craftData.getID();
					final byte data = craftData.getData();
					Block block = Bukkit.getWorld(worldName).getBlockAt(x.intValue(), y.intValue(), z.intValue());
					block.setTypeIdAndData(id, data, false);
					
					//sets sign lines
					if(craftData.isSign()) {
						String[] lines = craftData.getSignLines();
						Sign sign = (Sign) block.getState();
						for(int i = 0; i < lines.length; i++) {
							System.out.println(lines[i]);
							sign.setLine(i, lines[i]);
						}
						sign.update();
					}
				}
			}
		}, delay);
		//Closes builder and tells the program to cancel the transfer if the craft was obstructed
		return true;
	}
	//attempts to remove craft at given location, returns true if successful
	private static boolean removeCraftAtLocation(final ArrayList<CraftTransferData> craftTransferData, final SerializableLocation oldShipSignLocation) {
		final World world = Bukkit.getWorld(oldShipSignLocation.getWorldName());
		List<List<CraftTransferData>> partitionedList = Lists.partition(craftTransferData, 2000);
		long delay = 0;
		for(final List<CraftTransferData> data : partitionedList) {
			Bukkit.getScheduler().runTaskLater(Movecraft.getInstance(), new Runnable() {
				public void run() {
					SerializableLocation l;
					//goes through and removes fragiles first
					for(CraftTransferData craftData : data) {
						l = oldShipSignLocation.copy();
						l.offsetCoordinatesBy(craftData.getRelativeX(), craftData.getRelativeY(), craftData.getRelativeZ());
						Location blockLocation = l.getLocation();
						if(blockLocation.getBlock().getState() instanceof InventoryHolder) {
							System.out.println("Has inventory");
							InventoryHolder i = (InventoryHolder) blockLocation.getBlock().getState();
							i.getInventory().clear();
						}
						if(BlockUtils.blockIsFragile(craftData.getID())) {
							world.getBlockAt(blockLocation).setTypeId(0);
						}
					}
					//clears everything else
					for(CraftTransferData craftData : data) {
						l = oldShipSignLocation.copy();
						l.offsetCoordinatesBy(craftData.getRelativeX(), craftData.getRelativeY(), craftData.getRelativeZ());
						Location blockLocation = l.getLocation();
						world.getBlockAt(blockLocation).setTypeId(0);
					}
				}
			}, delay);
			delay += 2;
		}
		return true;
	}
	//grabs transferdata from db
	private static TransferData readData(String pilot) {
		return Movecraft.getInstance().getSQLDatabase().readData(pilot);
	}
}
