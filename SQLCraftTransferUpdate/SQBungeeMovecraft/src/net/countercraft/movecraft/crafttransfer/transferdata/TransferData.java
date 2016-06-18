package net.countercraft.movecraft.crafttransfer.transferdata;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.utils.MovecraftLocation;

import java.io.Serializable;
import java.util.ArrayList;

public class TransferData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<PlayerTransferData>  playerData;
	private ArrayList<CraftTransferData> craftData;
	private String pilot;
	private SerializableLocation destinationLocation;
	private String oldServer;
	private SerializableLocation originalSignLocation;
	
	public TransferData(Craft c, SerializableLocation destination) {
		pilot = c.getPilot().getName();
		destinationLocation = destination;
		oldServer = Bukkit.getServerName();
		//iterates over blocklist
		for(MovecraftLocation l : c.getBlockList()) {
			Location loc = new Location(c.getW(), l.getX(), l.getY(), l.getZ());
			//tests that block is a sign
			if(loc.getBlock().getState() instanceof Sign) {
				Sign s = (Sign) loc.getBlock().getState();
				//tests if block is shipsign, sets location
				if(Movecraft.signContainsPlayername(s, pilot)) {
					originalSignLocation = new SerializableLocation(loc);
				}
			}
		}
		craftData = new ArrayList<CraftTransferData>();
		for(MovecraftLocation l : c.getBlockList()) {
			craftData.add(new CraftTransferData(l, originalSignLocation.getLocation()));
		}
		playerData = new ArrayList<PlayerTransferData>();
		for(UUID u : c.playersRidingShip) {
			Player p = Movecraft.getPlayer(u);
			playerData.add(new PlayerTransferData(p, c.getW().getName(), originalSignLocation));
		}
	}
	public ArrayList<PlayerTransferData> getPlayerData() {
		return playerData;
	}
	public ArrayList<CraftTransferData> getCraftData() {
		return craftData;
	}
	public String getPilot() {
		return pilot;
	}
	public SerializableLocation getDestinationLocation() {
		return destinationLocation;
	}
	public void setDestinationLocation(SerializableLocation l) {
		destinationLocation = l;
	}
	public String getOldServer() {
		return oldServer;
	}
	public void setOldServer(String server) {
		oldServer = server;
	}
	public SerializableLocation getOriginalSignLocation() {
		return originalSignLocation;
	}
}
