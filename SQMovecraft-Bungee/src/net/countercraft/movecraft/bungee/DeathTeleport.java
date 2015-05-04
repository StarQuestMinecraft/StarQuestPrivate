package net.countercraft.movecraft.bungee;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.cryo.CryoSpawn;

public class DeathTeleport implements PlayerTeleport {

	UUID uuid;

	public DeathTeleport(UUID u) {
		this.uuid = u;
	}

	public void execute() {
		Player p = Bukkit.getPlayer(uuid);
		if (p != null) {
			System.out.println("Executing death teleport!");
			CryoSpawn.respawnPlayer(null, p);
			BungeePlayerHandler.teleportQueue.remove(this);
		}
	}

	public UUID getUUID() {
		return uuid;
	}
}
