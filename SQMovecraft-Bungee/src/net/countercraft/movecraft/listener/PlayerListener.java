/*

 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.listener;

import java.util.ArrayList;
import java.util.HashMap;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.translation.AutopilotRunTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;
import net.countercraft.movecraft.bungee.PlayerTeleport;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.OfflinePilotUtils;
import net.countercraft.movecraft.utils.SneakMoveTask;
import net.countercraft.movecraft.utils.WarpUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerListener implements Listener {
	private final HashMap<Player, BukkitTask> releaseEvents = new HashMap<Player, BukkitTask>();

/*	public void onPlayerDamaged( EntityDamageByEntityEvent e ) {
		if ( e instanceof Player ) {
			Player p = ( Player ) e;
			CraftManager.getInstance().removeCraft( CraftManager.getInstance().getCraftByPlayer( p ) );
		}
	}*/
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerJoinEvent event){
		Player p = event.getPlayer();
		Movecraft.playerIndex.put(p.getUniqueId(), p);
		BungeePlayerHandler.onLogin(p);
		OfflinePilotUtils.onPlayerLogin(p);
		/*boolean isTeleported = false;
		
		for(int i = 0; i < BungeePlayerHandler.teleportQueue.size(); i++){
			final PlayerTeleport t = BungeePlayerHandler.teleportQueue.get(i);
			String playername = event.getPlayer().getName();
			if(playername.equals(t.playername)){
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						t.execute();
					}
				}, 1L);
				isTeleported = true;
				break;
			}
		}
		
		if(!isTeleported){
			if(p.getGameMode() == GameMode.SURVIVAL){
				if(shouldTempFly(p)){
					giveTempFly(p);
				} else {
						p.setAllowFlight(false);
						p.setFlySpeed(0F);
						p.setFlying(false);
				}
			}
		}*/
	}
	
	@EventHandler
	public void onPlayerDeath( EntityDamageByEntityEvent e ) {  // changed to death so when you shoot up an airship and hit the pilot, it still sinks
		if ( e.getEntity() instanceof Player ) {
			Player p = ( Player ) e.getEntity();
			Craft c = CraftManager.getInstance().getCraftByPlayer( p );
			if (c != null)
			CraftManager.getInstance().removeCraft( CraftManager.getInstance().getCraftByPlayer( p ) );
		}
	}
	@EventHandler
	public void onPlayerQuit( PlayerQuitEvent e ) {  // changed to death so when you shoot up an airship and hit the pilot, it still sinks
			Player p = e.getPlayer();
			final Craft c = CraftManager.getInstance().getCraftByPlayer( p );
			if( c != null){
				CraftManager.getInstance().removeCraft(c);
				OfflinePilotUtils.registerOfflinePilot(p, c);
			}
			
			System.out.println("Removed player " + e.getPlayer().getName() + " with UUID " + e.getPlayer().getUniqueId() + " from index.");
			Movecraft.playerIndex.remove(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
		Craft c = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
		if (c != null){;
			if (event.isSneaking()){
				if (event.getPlayer().getItemInHand().getType() == Material.WATCH){
					if (event.getPlayer().hasPermission("movecraft." + c.getType().getCraftName() + ".move") || event.getPlayer().hasPermission("movecraft.override")) {
						SneakMoveTask task = new SneakMoveTask(c, event.getPlayer());
						task.runTaskTimer(Movecraft.getInstance(), 1, 1);
						c.setMoveTaskId(task.getTaskId());
					}
				}
			} else {
				if (c.getMoveTaskId() != -1){
					Bukkit.getScheduler().cancelTask(c.getMoveTaskId());
					c.setMoveTaskId(-1);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerMove( PlayerMoveEvent event ) {
		Player p = event.getPlayer();
		if(! (event instanceof PlayerTeleportEvent)){
			Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
			if(crafts == null) return;
			for(Craft c : crafts){
				if(c.playersRidingShip.contains(p.getUniqueId())){
					if(!MathUtils.playerIsWithinBoundingPolygon( c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc( event.getTo() ) ) ){
						if(!c.isProcessingTeleport()){
							p.setFallDistance(0.0F);
							p.teleport(c.originalPilotLoc);
							p.sendMessage("You attempted to leave the craft. If you want to leave the ship, type /stopriding.");
						}
					}
				}
			}
		}
		final Craft c = CraftManager.getInstance().getCraftByPlayer( p );
		if ( c != null ) {
			if ( !MathUtils.playerIsWithinBoundingPolygon( c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc( p.getLocation() ) ) ) {
				if(p.getWorld().getEnvironment() == Environment.THE_END){
					event.setCancelled(true);
					WarpUtils.leaveWarp(p, c);
					return;
				}
				if ( !releaseEvents.containsKey( p ) ) {
					p.sendMessage("You have left the craft, you have 15 seconds to return to the craft before it auto releases.");

					BukkitTask releaseTask = new BukkitRunnable() {

						@Override
						public void run() {
							CraftManager.getInstance().removeCraft( c );
						}

					}.runTaskLater( Movecraft.getInstance(), ( 20 * 15 ) );

					releaseEvents.put( p, releaseTask );
				}

			} else if ( releaseEvents.containsKey( p ) ) {
				releaseEvents.get( p ).cancel();
				releaseEvents.remove( p );
			}
		}
	}
	/*@EventHandler
	public void onAreaEnter (PlayerEnterAreaEvent event) throws IOException{
		Player p = event.getPlayer();
		p.sendMessage("before");
		Area area = (Area) event.getArea();
		String name = area.getName();
		event.getPlayer().sendMessage("You entered area "+ name);
		if (name.endsWith("warp")){
			final Craft c = CraftManager.getInstance().getCraftByPlayer( event.getPlayer() );
			if (c != null){
				if (MathUtils.playerIsWithinBoundingPolygon( c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc( event.getPlayer().getLocation() ) ) ) {
						
					String[] split = name.split("_");
					final String worldname = BungeeUtils.capitalizeFirstLeter(split[0]);
					event.getPlayer().sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + "Entering the atmosphere of " + worldname + "!");
					event.getPlayer().setVelocity(new Vector(0, 0, 0));					
					
					c.shipAttemptingTeleport = true;
					
					p.sendMessage("before task creation.");
					RepeatTryServerJumpTask task = new RepeatTryServerJumpTask(p, c, worldname, 0, 205, 0);
					task.runTaskTimer(Movecraft.getInstance(), 0, 1);
					

					return;
				}
			}
		}
	}*/
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSleep(PlayerBedLeaveEvent event){
		//if someone sleeps, update their bedspawn.
		Location l = getValidLocationToRespawn(event.getBed().getLocation().getBlock());
		Bedspawn b = new Bedspawn(event.getPlayer().getName(), Bukkit.getServerName(), l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
		if(!Bedspawn.hasKey(b)){
			Bedspawn.saveNewBedspawn(b);
		} else {
			Bedspawn.saveBedspawn(b);
		}
		event.getPlayer().sendMessage("Bedspawn updated. If this bed is on a ship, make sure you release and repilot your ship before flying it away.");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(final PlayerRespawnEvent event){
		Bedspawn b = Bedspawn.getBedspawn(event.getPlayer().getName());
		if(b == null) b = Bedspawn.DEFAULT;
		System.out.println(b);
		System.out.println("Player Current Server: " + Bukkit.getServerName());
		if(!b.server.equals(Bukkit.getServerName())){
			System.out.println("server name and target server name aren't equal, teleporting.");
			final Bedspawn b2 = b;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				public void run(){
					BungeePlayerHandler.sendPlayer(event.getPlayer(), b2.server, b2.world, b2.x, b2.y, b2.z);
				}
			}, 3L);
		} else {
			Location loc2 = new Location(Bukkit.getWorld(b.world), b.x, b.y, b.z);
			if (checkForNotAir(loc2)){
				event.setRespawnLocation(loc2);
			} else {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
					public void run(){
						BungeePlayerHandler.sendPlayer(event.getPlayer(), Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
					}
				}, 20L);
				Bedspawn.deleteBedspawn(event.getPlayer().getName());
			}
		}
	}

	@EventHandler
	public void onPlayerHit( EntityDamageEvent event ) {
		if ( event.getEntity() instanceof Player && event.getCause() == DamageCause.SUFFOCATION) {
			Player plr = (Player) event.getEntity();
			Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(event.getEntity().getWorld());
			if(event.getEntity() != null && crafts != null){
				for(Craft c : crafts){
					if(c.playersRidingShip.contains(plr.getUniqueId())){
						plr.teleport(c.originalPilotLoc);
						plr.setHealth(plr.getMaxHealth());
						plr.sendMessage("Whoa there! You kissed a wall...");
						return;
					}
				}
			}
		}
	}   
	private Location getValidLocationToRespawn(Block bed){
		Block bed2 = bed;
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(bed2.getRelative(BlockFace.NORTH));
		blocks.add(bed2.getRelative(BlockFace.SOUTH));
		blocks.add(bed2.getRelative(BlockFace.EAST));
		blocks.add(bed2.getRelative(BlockFace.WEST));
		
		for (Block b : blocks){
			if (b.getType() == Material.AIR){
				if(b.getRelative(BlockFace.UP).getType() == Material.AIR){
					Location bLoc = b.getLocation();
					return new Location(bLoc.getWorld(), bLoc.getX()+0.5, bLoc.getY(), bLoc.getZ() + 0.5);
				}
			}
		}
		return bed.getLocation();
	}
	public static boolean checkForNotAir(Location loc){
		boolean found = false;
		Block testblock = loc.getBlock();
		int tries = 0;
		while (!found && tries < 5){
			tries++;
			testblock = testblock.getRelative(BlockFace.DOWN);
			if (testblock.getType() != Material.AIR){
				found = true;
				break;
			}
		}
		return found;
	}

}
