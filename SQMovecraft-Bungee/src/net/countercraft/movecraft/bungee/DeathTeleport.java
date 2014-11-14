package net.countercraft.movecraft.bungee;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.countercraft.movecraft.cryo.CryoSpawn;

public class DeathTeleport implements PlayerTeleport{
	
	UUID uuid;
	
	public DeathTeleport(UUID u){
		this.uuid = u;
	}
	
	
	public void execute(){
		CryoSpawn.respawnPlayer(null, Bukkit.getPlayer(uuid));
		BungeePlayerHandler.teleportQueue.remove(this);
	}
	
	public UUID getUUID(){
		return uuid;
	}
}
