package net.countercraft.movecraft.utils;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

public class OfflinePilotUtils {
	private static HashMap<UUID, Craft> offlinePilotIndex = new HashMap<UUID, Craft>();
	
	public static void registerOfflinePilot(Player p, Craft c){
		registerOfflinePilot(p.getUniqueId(), c);
	}
	
	public static void registerOfflinePilot(final UUID uid, Craft c){
		offlinePilotIndex.put(uid, c);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				offlinePilotIndex.remove(uid);
			}
		}, 12000);
	}
	
	public static void onPlayerLogin(Player p){
		Craft c = offlinePilotIndex.get(p.getUniqueId());
		if(c != null){
			c.teleportToOriginalPilotLoc(p);
			p.sendMessage("Hey, you logged out while flying a ship. We teleported you back to your ship, just in case you weren't there!");
			offlinePilotIndex.remove(p.getUniqueId());
		}
	}
}
