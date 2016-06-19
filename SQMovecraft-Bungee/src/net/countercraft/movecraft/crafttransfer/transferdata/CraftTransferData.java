package net.countercraft.movecraft.crafttransfer.transferdata;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.dibujaron.cardboardbox.Crate;

import net.countercraft.movecraft.utils.MovecraftLocation;

public class CraftTransferData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private double xOffsetFromShipSign, yOffsetFromShipSign, zOffsetFromShipSign;
	private int id;
	private byte data;
	private boolean hasInventory;
	private Crate inventory;
	private boolean isSign;
	private boolean isShipSign;
	private String[] lines = new String[4];
	
	public CraftTransferData(MovecraftLocation blockLocation, Location shipSignLocation) {
		xOffsetFromShipSign = blockLocation.getX() - shipSignLocation.getX();
		yOffsetFromShipSign = blockLocation.getY() - shipSignLocation.getY();
		zOffsetFromShipSign = blockLocation.getZ() - shipSignLocation.getZ();
		Location location = new Location(shipSignLocation.getWorld(), blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
		id = location.getBlock().getTypeId();
		data = location.getBlock().getData();
		if(location.getBlock().getState() instanceof Sign) {
			isSign = true;
			Sign s = (Sign) location.getBlock().getState();
			lines[0] = s.getLine(0);
			lines[1] = s.getLine(1);
			lines[2] = s.getLine(2);
			lines[3] = s.getLine(3);
			//test
			for(String line : lines) {
				System.out.println(line);
			}
		}
		else {
			if(location.getBlock().getState() instanceof InventoryHolder) {
				hasInventory = true;
				InventoryHolder invholder = (InventoryHolder) location.getBlock().getState();
				inventory = new Crate(null, invholder.getInventory());
			}
			else {
				hasInventory = false;
			}
			isSign = false;
		}
		if((xOffsetFromShipSign == 0) && (yOffsetFromShipSign == 0) && (zOffsetFromShipSign == 0)) {
			isShipSign = true;
		}
		else {
			isShipSign = false;
		}
	}
	public double getRelativeX() {
		return xOffsetFromShipSign;
	}
	public double getRelativeY() {
		return yOffsetFromShipSign;
	}
	public double getRelativeZ() {
		return zOffsetFromShipSign;
	}
	public int getID() {
		return id;
	}
	public byte getData() {
		return data;
	}
	public boolean hasInventory() {
		return hasInventory;
	}
	public Crate getInventory() {
		return inventory;
	}
	public boolean isSign() {
		return isSign;
	}
	public boolean isShipSign() {
		return isShipSign;
	}
	public String[] getSignLines() {
		return lines;
	}
}