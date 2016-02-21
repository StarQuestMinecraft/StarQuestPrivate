package net.countercraft.movecraft.bungee;

import java.util.UUID;

import org.bukkit.Location;

import com.dibujaron.cardboardbox.Knapsack;

public class PlayerTransferData {
	private Knapsack playerKnapsack;
	private Location playerLocation;
	private UUID playerID;
	
	public Knapsack getPlayerKnapsack() {
		return playerKnapsack;
	}
	public Location getPlayerLocation() {
		return playerLocation;
	}
	public UUID getPlayerID() {
		return playerID;
	}
	public void setPlayerKnapsack(Knapsack knapsack) {
		playerKnapsack = knapsack;
	}
	public void setPlayerLocation(Location location) {
		playerLocation = location;
	}
	public void setPlayerID(UUID id) {
		playerID = id;
	}
}
