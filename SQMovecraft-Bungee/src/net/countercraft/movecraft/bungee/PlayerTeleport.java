
package net.countercraft.movecraft.bungee;

import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.listener.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import us.higashiyama.george.CardboardBox.Knapsack;

public class PlayerTeleport {

	public UUID uuid;
	String worldname;
	public int x;
	public int y;
	public int z;
	float yaw;
	float pitch;
	GameMode gamemode;
	public Knapsack knapsack;
	boolean isBedspawn;

	public PlayerTeleport(UUID uuid, String worldname, int x, int y, int z, double yaw, double pitch, Knapsack knap, GameMode gamemode){
		init(uuid, worldname, x, y, z, yaw, pitch, knap, gamemode, false);
	}
	public PlayerTeleport(UUID uuid, String worldname, int x, int y, int z, double yaw, double pitch, Knapsack knap, GameMode gamemode, boolean isBedspawn) {
		init(uuid, worldname, x, y, z, yaw, pitch, knap, gamemode, isBedspawn);
	}
	
	private void init(UUID uuid, String worldname, int x, int y, int z, double yaw, double pitch, Knapsack knap, GameMode gamemode, boolean isBedspawn){
		this.uuid = uuid;
		this.worldname = worldname;

		// NOTE x y and z represent either the player's preteleport location or
		// their target location. Movecraft craft tps use this data
		// for pre-tp location, but the standard player sender uses it as
		// post-tp location.
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = (float) yaw;
		this.pitch = (float) pitch;
		this.gamemode = gamemode;
		this.knapsack = knap;
		isBedspawn = false;
	}

	public void execute() {

		final Player p = Movecraft.getPlayer(uuid);
		
		if(p == null){
			System.out.println("MOVECRAFT ERROR: t.execute() called on null player!");
			return;
		}
		
		World w = Bukkit.getServer().getWorld(worldname);
		
		Location l = new Location(w, x + 0.5, (double) y, z + 0.5, yaw, pitch);
		
		if(this.knapsack != null){
			this.knapsack.unpack(p);
		}
		p.setGameMode(gamemode);
		BungeePlayerHandler.teleportQueue.remove(this);
		
		if(!isBedspawn){
			p.teleport(l);
		} else {
			if (PlayerListener.checkForNotAir(l)){
				p.teleport(l);
			} else {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						BungeePlayerHandler.sendPlayer(p, Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
					}
				}, 20L);
				Bedspawn.deleteBedspawn(p.getName());
			}
		}
	}
}
