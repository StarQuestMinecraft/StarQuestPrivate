package net.countercraft.movecraft.cryo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActiveCryoHandler {
	private static HashSet<String> unacceptedLogins = new HashSet<String>();
	
	private static void onMessageRecieved(String player){
		System.out.println("Message Recieved!");
		Player p = Bukkit.getPlayer(player);
		if(p != null){
			checkAndRespawn(p);
		} else {
			unacceptedLogins.add(player);
			System.out.println("Unaccepted Logins Add; size " + unacceptedLogins.size());
		}
	}
	
	public static void onPlayerLogin(Player player){
		if(unacceptedLogins.contains(player.getName())){
			unacceptedLogins.remove(player.getName());
			System.out.println("Unaccepted Logins Remove; size " +  unacceptedLogins.size());
			checkAndRespawn(player);
		}
	}
	
	
	private static void checkAndRespawn(final Player p){
		final CryoSpawn s = CryoSpawn.getSpawn(p.getName());
		if(s == null || !s.isActive) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				
				if(!s.server.equals(Bukkit.getServerName())){
					System.out.println("Sending player for Cryopod!");
					BungeePlayerHandler.sendPlayer(p, s.server, s.world, s.x, s.y, s.z);
				} else {
					System.out.println("Local teleporting player for Cryopod!");
					Location target = new Location(Bukkit.getWorld(s.world), s.x + 0.5, s.y, s.z + 0.5);
					p.teleport(target);
					CryoSpawn.addToAnyShips(target, p);
					CryoSpawn.playEffects(target);
				}
			}
		}, 3L);
	}

	public static void decodeMessage(DataInputStream in) {
		try {
			String name = in.readUTF();
			onMessageRecieved(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
