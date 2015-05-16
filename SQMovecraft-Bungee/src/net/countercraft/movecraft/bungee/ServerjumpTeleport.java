
package net.countercraft.movecraft.bungee;

import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.cardboardbox.Knapsack;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.cryo.CryoUtils;
import net.countercraft.movecraft.listener.EntityListener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class ServerjumpTeleport implements PlayerTeleport{

	public UUID uuid;
	String worldname;
	public int x;
	public int y;
	public int z;
	float yaw;
	float pitch;
	GameMode gamemode;
	public Knapsack knapsack;

	public ServerjumpTeleport(UUID uuid, String worldname, int x, int y, int z, double yaw, double pitch, Knapsack knap, GameMode gamemode){
		init(uuid, worldname, x, y, z, yaw, pitch, knap, gamemode);
	}
	
	private void init(UUID uuid, String worldname, int x, int y, int z, double yaw, double pitch, Knapsack knap, GameMode gamemode){
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
	}

	public void execute() {

		final Player p = Movecraft.getPlayer(uuid);
		
		if(p == null){
			System.out.println("MOVECRAFT ERROR: t.execute() called on null player!");
			return;
		}
		
		World w = Bukkit.getServer().getWorld(worldname);
		
		Location l = new Location(w, x + 0.5, (double) y, z + 0.5, yaw, pitch);
		Block b = l.getBlock().getRelative(BlockFace.UP);
		if(b.getType() == Material.WALL_SIGN){
			Sign s = (Sign) b.getState();
			if(s.getLine(0).equals(CryoSpawn.KEY_LINE)){
				CryoUtils.removeBlockAtCryoSpawn(l);
			}
		}
		if(this.knapsack != null){
			this.knapsack.unpack(p);
		}
		p.setGameMode(gamemode);
		p.setHealth(p.getMaxHealth());
		p.setFallDistance(0);
		BungeePlayerHandler.teleportQueue.remove(this);
		
		/*if(!isRespawn){*/
			p.teleport(l);
			//CryoSpawn.addToAnyShips(l,p );
			//CryoSpawn.checkAndPlayEffects(l);
		/*} else {
			if (EntityListener.checkForNotAir(l)){
				p.teleport(l);
			} else {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						BungeePlayerHandler.sendPlayer(p, Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
					}
				}, 20L);
				Bedspawn.deleteBedspawn(p.getName());
			}
		}*/
	}
	
	public UUID getUUID(){
		return uuid;
	}
}
