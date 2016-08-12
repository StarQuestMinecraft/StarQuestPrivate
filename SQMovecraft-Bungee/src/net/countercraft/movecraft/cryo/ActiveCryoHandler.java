package net.countercraft.movecraft.cryo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActiveCryoHandler {
	private static HashSet<String> unacceptedLogins = new HashSet<String>();
	
	private static void onMessageRecieved(String player){
		Player p = Bukkit.getPlayer(player);
		if(p != null){
			doChecksAndSendPlayer(p);
		} else {
			unacceptedLogins.add(player);
		}
	}
	
	public static boolean onPlayerLogin(Player player){
		if(unacceptedLogins.contains(player.getName())){
			unacceptedLogins.remove(player.getName());
			return doChecksAndSendPlayer(player);
		}
		return false;
	}
	
	
	private static boolean doChecksAndSendPlayer(final Player p){
		final CryoSpawn s = CryoSpawn.getSpawnIfNeedsActiveRespawn(p.getName());
		if(s == null || !s.isActive) return false;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			@Override
			public void run(){
				CryoSpawn.unsetUpdatedSinceLastLogin(p.getName());
				if(!s.server.equals(Bukkit.getServerName())){
					BungeePlayerHandler.sendPlayer(p, s.server, s.world, s.x, s.y, s.z);
				} else {
					Location target = new Location(Bukkit.getWorld(s.world), s.x + 0.5, s.y, s.z + 0.5);
					CryoUtils.removeBlockAtCryoSpawn(target);
					p.teleport(target);
					//CryoSpawn.addToAnyShips(target, p);
					CryoSpawn.playEffects(target);
				}
			}
		}, 3L);
		return true;
	}

	public static void decodeMessage(DataInputStream in) {
		try {
			String name = in.readUTF();
			onMessageRecieved(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
