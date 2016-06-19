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
	private Knapsack playerKnapsack;
	private double xOffsetFromShipSign, yOffsetFromShipSign, zOffsetFromShipSign;
	private double pitch, yaw;

	
	public PlayerTransferData(Player p, String destinationWorldName, SerializableLocation shipSignLocation) {
		player = p.getName();
		uuid = p.getUniqueId();
		gameMode = p.getGameMode();
		playerKnapsack = new Knapsack(p);
		Location l = p.getLocation();
		xOffsetFromShipSign = l.getX() - shipSignLocation.getX();
		yOffsetFromShipSign = l.getY() - shipSignLocation.getY();
		zOffsetFromShipSign = l.getZ() - shipSignLocation.getZ();
		pitch = l.getPitch();
		yaw = l.getYaw();
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
	public Knapsack getPlayerKnapsack() {
		return playerKnapsack;
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
	public double getPitch() {
		return pitch;
	}
	public double getYaw() {
		return yaw;
	}
	public void unpack(Player p) {
		getPlayerKnapsack().unpack(p);
		p.setGameMode(getGameMode());
	}
}