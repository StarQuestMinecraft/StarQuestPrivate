package net.countercraft.movecraft.bungee;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;

import com.dibujaron.cardboardbox.Knapsack;

public class PlayerTransferData implements Serializable {
	private Knapsack playerKnapsack;
	private SerializableLocation playerLocation;
	private UUID playerID;
	private GameMode gameMode;
	
	public Knapsack getPlayerKnapsack() {
		return playerKnapsack;
	}
	public SerializableLocation getPlayerLocation() {
		return playerLocation;
	}
	public UUID getPlayerID() {
		return playerID;
	}
	public GameMode getPlayerGameMode() {
		return gameMode;
	}
	public void setPlayerKnapsack(Knapsack knapsack) {
		playerKnapsack = knapsack;
	}
	public void setPlayerLocation(Location location) {
		playerLocation = new SerializableLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	public void setPlayerID(UUID id) {
		playerID = id;
	}
	public void setPlayerGameMode(GameMode mode) {
		gameMode = mode;
	}
}
