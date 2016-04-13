package net.countercraft.movecraft.crafttransfer.transferdata;

import java.io.Serializable;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.cryo.CryoUtils;
import net.countercraft.movecraft.listener.EntityListener;

import com.dibujaron.cardboardbox.Knapsack;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PlayerTransferData implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	private String player;
	private UUID uuid;
	private GameMode gameMode;
	private SerializableLocation destinationLocation;
	private Knapsack playerKnapsack;
	
	public PlayerTransferData(Player p, String destinationWorldName) {
		player = p.getName();
		uuid = p.getUniqueId();
		gameMode = p.getGameMode();
		playerKnapsack = new Knapsack(p);
	}
	
	public String getPlayer() {
		return player;
	}
	public UUID getUUID() {
		return uuid;
	}
	public GameMode getGameMode() {
		return gameMode;
	}
	public SerializableLocation getDestination() {
		return destinationLocation;
	}
	public void setDestination(SerializableLocation l) {
		destinationLocation = l;
	}
	public Knapsack getPlayerKnapsack() {
		return playerKnapsack;
	}
}
