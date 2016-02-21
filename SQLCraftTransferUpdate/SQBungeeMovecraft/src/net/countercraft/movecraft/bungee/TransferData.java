package net.countercraft.movecraft.bungee;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.Location;

public class TransferData {
	
	private boolean slip;
	
	private Location destinationLocation;
	
	private Location oldLocation;
	
	private String craftType;
	
	private String pilotName;
	private UUID pilotUUID;
	
	private ArrayList<LocAndBlock> blockList;
	
	private ArrayList<String> playersWithBedspawnsOnShip;
	
	private Vector<UUID> playersRidingShip;
	
	private ArrayList<PlayerTransferData> playerData;

	public ArrayList<PlayerTransferData> getPlayerData() {
		return playerData;
	}

	public void setPlayerData(ArrayList<PlayerTransferData> playerData) {
		this.playerData = playerData;
	}

	public Vector<UUID> getPlayersRidingShip() {
		return playersRidingShip;
	}

	public void setPlayersRidingShip(Vector<UUID> playersRidingShip) {
		this.playersRidingShip = playersRidingShip;
	}

	public ArrayList<String> getPlayersWithBedspawnsOnShip() {
		return playersWithBedspawnsOnShip;
	}

	public void setPlayersWithBedspawnsOnShip(ArrayList<String> playersWithBedspawnsOnShip) {
		this.playersWithBedspawnsOnShip = playersWithBedspawnsOnShip;
	}

	public ArrayList<LocAndBlock> getBlockList() {
		return blockList;
	}

	public void setBlockList(ArrayList<LocAndBlock> blockList) {
		this.blockList = blockList;
	}

	public UUID getPilotUUID() {
		return pilotUUID;
	}

	public void setPilotUUID(UUID pilotUUID) {
		this.pilotUUID = pilotUUID;
	}

	public String getPilotName() {
		return pilotName;
	}

	public void setPilotName(String pilotName) {
		this.pilotName = pilotName;
	}

	public String getCraftType() {
		return craftType;
	}

	public void setCraftType(String craftType) {
		this.craftType = craftType;
	}

	public Location getOldLocation() {
		return oldLocation;
	}

	public void setOldLocation(Location oldLocation) {
		this.oldLocation = oldLocation;
	}

	public Location getDestinationLocation() {
		return destinationLocation;
	}

	public void setDestinationLocation(Location destinationLocation) {
		this.destinationLocation = destinationLocation;
	}

	public boolean isSlip() {
		return slip;
	}

	public void setSlip(boolean slip) {
		this.slip = slip;
	}
}
