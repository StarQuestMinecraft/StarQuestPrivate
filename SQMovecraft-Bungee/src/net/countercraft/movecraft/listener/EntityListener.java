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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.bungee.BungeePlayerHandler;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.cryo.CryoSpawn;
import net.countercraft.movecraft.event.CraftGunsHitEvent;
import net.countercraft.movecraft.event.CraftShootEvent;
import net.countercraft.movecraft.projectile.LaserBolt;
import net.countercraft.movecraft.projectile.LaserBolt.LocationHit;
import net.countercraft.movecraft.slip.WarpUtils;
import net.countercraft.movecraft.task.SneakMoveTask;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.BoardingRampUtils;
import net.countercraft.movecraft.utils.JammerUtils;
import net.countercraft.movecraft.utils.KillUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.OfflinePilotUtils;
import net.countercraft.movecraft.utils.PlayerFlightUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityListener implements Listener {
	private final HashMap<Player, BukkitTask> releaseEvents = new HashMap<Player, BukkitTask>();
	private static int[] NON_EXPLODABLES = { 1, 4, 7, 14, 15, 16, 21, 22, 24, 41, 42, 45, 48, 49, 56, 57, 64, 71, 73, 74, 98, 133, 155, 159 };
	// private static int[] TORPEDO_EXCEPTIONS = {42, 101, 167};
	/*
	 * public void onPlayerDamaged( EntityDamageByEntityEvent e ) { if ( e
	 * instanceof Player ) { Player p = ( Player ) e;
	 * CraftManager.getInstance().removeCraft(
	 * CraftManager.getInstance().getCraftByPlayer( p ) ); } }
	 */


	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;
		List<Block> affectedBlocks = event.blockList();
		int count = 0;
		for (int i = affectedBlocks.size() - 1; i >= 0; i--) {
			count++;
			Block b = affectedBlocks.get(i);
			Material type = b.getType();
			if (type == Material.SIGN_POST && isCraftSign(b)) {
				affectedBlocks.remove(i);
				continue;
			} else if (type == Material.WALL_SIGN && isCraftSign(b)) {
				affectedBlocks.remove(i);
				continue;
			} else if (Arrays.binarySearch(NON_EXPLODABLES, b.getTypeId()) >= 0) {
				/*
				 * if(event.getEntity() != null){
				 * if(!(Arrays.binarySearch(TORPEDO_EXCEPTIONS, b.getTypeId())
				 * >= 0)){ affectedBlocks.remove(i); }
				 * 
				 * }
				 */
				affectedBlocks.remove(i);
				continue;
			} else if (type == Material.STAINED_GLASS) {
				if (LaserBolt.getBoltBlocks().contains(b)) {
					affectedBlocks.remove(i);
					continue;
				}
			} else {
				Block[] edges = BlockUtils.getEdges(b, false, false);
				for (Block e : edges) {
					if (e.getType() == Material.SIGN_POST && isCraftSign(e)) {
						if (e.getFace(b) == BlockFace.DOWN) {
							affectedBlocks.remove(b);
							break;
						}
					} else if (e.getType() == Material.WALL_SIGN && isCraftSign(e)) {
						BlockFace face = b.getFace(e);
						if (face == BoardingRampUtils.getFacingBlockFace((Sign) e.getState())) {
							affectedBlocks.remove(b);
							break;
						}
					}
				}
			}
		}
		for (Block b : affectedBlocks) {
			boolean yes = JammerUtils.checkForAndDisableJammer(b);
			if (yes)
				break;
		}
	}

	private static boolean isCraftSign(Block sign) {
		Sign s = (Sign) sign.getState();
		String l = s.getLine(0);
		for (CraftType t : CraftManager.getInstance().getCraftTypes()) {
			if (l.equalsIgnoreCase(t.getCraftName())) {
				return true;
			}
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(p.getGameMode() == GameMode.SURVIVAL){
			PlayerFlightUtil.removeFlightUnlessAllowed(p);
		}
		boolean teleported = BungeePlayerHandler.onLogin(p);
		if (!teleported)
			teleported = OfflinePilotUtils.onPlayerLogin(p);
		/*
		 * boolean isTeleported = false;
		 * 
		 * for(int i = 0; i < BungeePlayerHandler.teleportQueue.size(); i++){
		 * final PlayerTeleport t = BungeePlayerHandler.teleportQueue.get(i);
		 * String playername = event.getPlayer().getName();
		 * if(playername.equals(t.playername)){
		 * Bukkit.getServer().getScheduler()
		 * .scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
		 * public void run(){ t.execute(); } }, 1L); isTeleported = true; break;
		 * } }
		 * 
		 * if(!isTeleported){ if(p.getGameMode() == GameMode.SURVIVAL){
		 * if(shouldTempFly(p)){ giveTempFly(p); } else {
		 */
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {

		Player p = (Player) e.getEntity();
		Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if (c != null)
			CraftManager.getInstance().removeCraft(CraftManager.getInstance().getCraftByPlayer(p));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) { // changed to death so when
													// you shoot up an airship
													// and hit the pilot, it
													// still sinks
		Player p = e.getPlayer();
		final Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if (c != null) {
			CraftManager.getInstance().removeCraft(c);
			OfflinePilotUtils.registerOfflinePilot(p, c);
		}
		if(PlayerFlightUtil.isTeleportFlying(p)){
			PlayerFlightUtil.endTeleportFlying(p);
		}
		if(PlayerFlightUtil.isShipFlying(p)){
			PlayerFlightUtil.endShipFlying(p);
		}
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Craft c = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
		if (c != null) {
			;
			if (event.isSneaking()) {
				if (event.getPlayer().getItemInHand().getType() == Material.WATCH) {
					SneakMoveTask task = new SneakMoveTask(c, event.getPlayer());
					task.runTaskTimer(Movecraft.getInstance(), 1, 1);
					c.setMoveTaskId(task.getTaskId());
				}
			} else {
				if (c.getMoveTaskId() != -1) {
					Bukkit.getScheduler().cancelTask(c.getMoveTaskId());
					c.setMoveTaskId(-1);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if(event instanceof PlayerTeleportEvent) return;
		
		//get the craft the player is on
		Craft pCraft = null;
		Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
		if (crafts != null){
			for (Craft c : crafts) {
				if (c.playersRidingShip.contains(p.getUniqueId())) {
					pCraft = c;
				}
			}
		}
		//if they are ship flying and they move, end ship flying.
		if(PlayerFlightUtil.isShipFlying(event.getPlayer())){
			//but cancel if their ship is moving because this is unpredictable.
			if(pCraft != null && pCraft.isProcessing()){
				event.setCancelled(true);
				return;
			}
			PlayerFlightUtil.endShipFlying(event.getPlayer());
		}
		if(pCraft == null) return;
		if (!MathUtils.playerIsWithinBoundingPolygon(pCraft.getHitBox(), pCraft.getMinX(), pCraft.getMinZ(), MathUtils.bukkit2MovecraftLoc(event.getTo()))) {
			if (!pCraft.isProcessingTeleport()) {
				p.setFallDistance(0.0F);
				p.teleport(pCraft.originalPilotLoc);
				p.sendMessage("You attempted to leave the craft. If you want to leave the ship, type /stopriding.");
				return;
			}
		}
		/*Player p = event.getPlayer();
		if (event instanceof PlayerTeleportEvent) {
			return;
		} else {
			Craft pCraft = null;
			Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
			if (crafts != null){
				for (Craft c : crafts) {
					if (c.playersRidingShip.contains(p.getUniqueId())) {
						pCraft = c;
						if (!MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(event.getTo()))) {
							if (!c.isProcessingTeleport()) {
								p.setFallDistance(0.0F);
								p.teleport(c.originalPilotLoc);
								p.sendMessage("You attempted to leave the craft. If you want to leave the ship, type /stopriding.");
							}
						}
					}
				}
			}
			if(PlayerFlightUtil.isShipFlying(event.getPlayer()) && !isFalling(event)){
				if(pCraft != null && pCraft.isProcessing()){
					event.setCancelled(true);
					return;
				}
				PlayerFlightUtil.endShipFlying(event.getPlayer());
			}
		}
		final Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if (c != null) {
			if (!MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
				
				 // if(p.getWorld().getEnvironment() == Environment.THE_END){
				 // event.setCancelled(true); WarpUtils.leaveWarp(p, c, true);
				 // return; }
				 
				if (!releaseEvents.containsKey(p)) {
					p.sendMessage("You have left the craft, you have 15 seconds to return to the craft before it auto releases.");

					BukkitTask releaseTask = new BukkitRunnable() {

						@Override
						public void run() {
							CraftManager.getInstance().removeCraft(c);
						}

					}.runTaskLater(Movecraft.getInstance(), (20 * 15));

					releaseEvents.put(p, releaseTask);
				}

			} else if (releaseEvents.containsKey(p)) {
				releaseEvents.get(p).cancel();
				releaseEvents.remove(p);
			}
		}*/
	}

	private boolean isFalling(PlayerMoveEvent event) {
		//first check if dy < 0;
		if(event.getTo().getY() < event.getFrom().getY()){
			//check if x and z change are small
			double dx = Math.abs(event.getTo().getX() - event.getFrom().getX());
			double dz = Math.abs(event.getTo().getZ() - event.getFrom().getZ());
			if(dx < 1 && dz < 1){
				return true;
			}
		}
		return false;
	}

	/*
	 * @EventHandler public void onAreaEnter (PlayerEnterAreaEvent event) throws
	 * IOException{ Player p = event.getPlayer(); p.sendMessage("before"); Area
	 * area = (Area) event.getArea(); String name = area.getName();
	 * event.getPlayer().sendMessage("You entered area "+ name); if
	 * (name.endsWith("warp")){ final Craft c =
	 * CraftManager.getInstance().getCraftByPlayer( event.getPlayer() ); if (c
	 * != null){ if (MathUtils.playerIsWithinBoundingPolygon( c.getHitBox(),
	 * c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(
	 * event.getPlayer().getLocation() ) ) ) {
	 * 
	 * String[] split = name.split("_"); final String worldname =
	 * BungeeUtils.capitalizeFirstLeter(split[0]);
	 * event.getPlayer().sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD
	 * + "Entering the atmosphere of " + worldname + "!");
	 * event.getPlayer().setVelocity(new Vector(0, 0, 0));
	 * 
	 * c.shipAttemptingTeleport = true;
	 * 
	 * p.sendMessage("before task creation."); RepeatTryServerJumpTask task =
	 * new RepeatTryServerJumpTask(p, c, worldname, 0, 205, 0);
	 * task.runTaskTimer(Movecraft.getInstance(), 0, 1);
	 * 
	 * 
	 * return; } } } }
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSleep(PlayerBedLeaveEvent event) {
		// if someone sleeps, update their bedspawn.
		// Location l =
		// getValidLocationToRespawn(event.getBed().getLocation().getBlock());
		/*
		 * Bedspawn b = new Bedspawn(event.getPlayer().getName(),
		 * Bukkit.getServerName(), l.getWorld().getName(), l.getBlockX(),
		 * l.getBlockY(), l.getBlockZ()); if(!Bedspawn.hasKey(b)){
		 * Bedspawn.saveNewBedspawn(b); } else { Bedspawn.saveBedspawn(b); }
		 */
		// event.getPlayer().sendMessage("Bedspawn updated. If this bed is on a ship, make sure you release and repilot your ship before flying it away.");
		event.getPlayer().sendMessage(ChatColor.RED + "Bedspawns are now disabled on starquest!");
		event.getPlayer().sendMessage(ChatColor.RED + "Please use a cryopod instead! " + ChatColor.GOLD + " http://tinyurl.com/sqcryo");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable() {
			public void run() {
				if (CryoSpawn.respawnPlayerAsync(event.getPlayer())) {
					return;
				} else {
					System.out.println("Defaulting back to bedspawn, CryoSpawn failed.");
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
						public void run() {
							BungeePlayerHandler.sendPlayer(event.getPlayer(), Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
						}
					}, 20L);
					/*Bedspawn b = Bedspawn.getBedspawn(event.getPlayer().getName());
					if (b == null) {
						b = Bedspawn.DEFAULT;
					}
					System.out.println("Player Current Server: " + Bukkit.getServerName());
					if (!b.server.equals(Bukkit.getServerName())) {
						final Bedspawn b2 = b;
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
							public void run() {
								BungeePlayerHandler.sendPlayer(event.getPlayer(), b2.server, b2.world, b2.x, b2.y, b2.z);
							}
						}, 3L);
					} else {
						Location loc2 = new Location(Bukkit.getWorld(b.world), b.x, b.y, b.z);
						if (checkForNotAir(loc2)) {
							event.setRespawnLocation(loc2);
						} else {
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
								public void run() {
									BungeePlayerHandler.sendPlayer(event.getPlayer(), Bedspawn.DEFAULT.server, Bedspawn.DEFAULT.world, Bedspawn.DEFAULT.x, Bedspawn.DEFAULT.y, Bedspawn.DEFAULT.z);
								}
							}, 20L);
							Bedspawn.deleteBedspawn(event.getPlayer().getName());
						}
					}*/
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerHit(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player plr = (Player) event.getEntity();
			if (event.getCause() == DamageCause.SUFFOCATION) {
				Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(event.getEntity().getWorld());
				if (event.getEntity() != null && crafts != null) {
					for (Craft c : crafts) {
						if (c.playersRidingShip.contains(plr.getUniqueId())) {
							plr.teleport(c.originalPilotLoc);
							plr.setHealth(plr.getMaxHealth());
							plr.sendMessage("Whoa there! You kissed a wall...");
							return;
						}
					}
				}
			} else {
				if (event.getCause() == DamageCause.BLOCK_EXPLOSION) {
					LocationHit l = LaserBolt.getClosestExplosion(event.getEntity().getLocation());
					if (l != null && l.distanceSquared(event.getEntity().getLocation()) < 49) {
						Player shooter = l.getPlayer();
						CraftGunsHitEvent event2 = new CraftGunsHitEvent(null, shooter, plr);
						System.out.println("calling event.");
						event2.call();
						if(!event2.isCancelled()){
							event.setDamage(4D);
						}
						return;
					}
				}
				Craft c = CraftManager.getInstance().getCraftByPlayer(plr);
				if (c != null)
					CraftManager.getInstance().removeCraft(c);
			}
		}
	}

	private Location getValidLocationToRespawn(Block bed) {
		Block bed2 = bed;
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(bed2.getRelative(BlockFace.NORTH));
		blocks.add(bed2.getRelative(BlockFace.SOUTH));
		blocks.add(bed2.getRelative(BlockFace.EAST));
		blocks.add(bed2.getRelative(BlockFace.WEST));

		for (Block b : blocks) {
			if (b.getType() == Material.AIR) {
				if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
					Location bLoc = b.getLocation();
					return new Location(bLoc.getWorld(), bLoc.getX() + 0.5, bLoc.getY(), bLoc.getZ() + 0.5);
				}
			}
		}
		return bed.getLocation();
	}

	public static boolean checkForNotAir(Location loc) {
		boolean found = false;
		Block testblock = loc.getBlock();
		int tries = 0;
		while (!found && tries < 5) {
			tries++;
			testblock = testblock.getRelative(BlockFace.DOWN);
			if (testblock.getType().isSolid()) {
				found = true;
				break;
			}
		}
		return found;
	}

}
