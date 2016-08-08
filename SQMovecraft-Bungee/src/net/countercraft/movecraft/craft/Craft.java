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

package net.countercraft.movecraft.craft;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.AsyncManager;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.detection.PodDetectionTask;
import net.countercraft.movecraft.async.detection.RedetectTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.async.translation.TranslationTaskData;
import net.countercraft.movecraft.event.CraftProjectileDetonateEvent;
import net.countercraft.movecraft.event.CraftShootEvent;
import net.countercraft.movecraft.event.CraftSyncTranslateEvent;
import net.countercraft.movecraft.utils.FakeBlockUtils;
import net.countercraft.movecraft.utils.GunUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.inventivetalent.particle.ParticleEffect;

import com.whirlwindgames.dibujaron.sqempire.Empire;
import com.whirlwindgames.dibujaron.sqempire.database.object.EmpirePlayer;

import us.higashiyama.george.SQSpace.SQSpace;

import java.util.Vector;

public class Craft {
	private int[][][] hitBox;
	private final CraftType type;
	private MovecraftLocation[] blockList;
	private World w;
	private AtomicBoolean processing = new AtomicBoolean();
	private AtomicBoolean processingTeleport = new AtomicBoolean();
	private int moveTaskID;
	private int minX, minZ;
	public int xDist, yDist, zDist;
	public ArrayList<String> playersWithBedspawnsOnShip = new ArrayList<String>();
	public Semaphore bedspawnsLock = new Semaphore(1);
	public int vX, vZ, vY;
	public Vector<UUID> playersRidingShip = new Vector<UUID>();
	//public Semaphore playersRidingLock = new Semaphore(1);
	public int warpCoordsX;
	public int warpCoordsZ;
	public Location originalPilotLoc = null;
	public Player pilot;
	private boolean released = false;
	public boolean isJamming = false;
	private long lastMove = 0;
	public int hoverHeight;

	public int cannonCooldown = 0;
	
	public Craft(CraftType type, World world) {
		this.type = type;
		this.w = world;
		this.blockList = new MovecraftLocation[1];
		this.moveTaskID = -1;
		xDist = 0;
		yDist = 0;
		zDist = 0;
		this.hoverHeight = type.getHoverHeight();
	}

	/*
	 * public boolean isNotProcessing() { return !processing.get(); }
	 * 
	 * public void setProcessing( boolean processing ) { this.processing.set(
	 * processing ); }
	 */

	public MovecraftLocation[] getBlockList() {
		synchronized (blockList) {
			return blockList.clone();
		}
	}

	public void setBlockList(MovecraftLocation[] blockList) {
		synchronized (this.blockList) {
			this.blockList = blockList;
		}
	}

	public CraftType getType() {
		return type;
	}

	public World getW() {
		return w;
	}

	public int[][][] getHitBox() {
		return hitBox;
	}

	public void setHitBox(int[][][] hitBox) {
		this.hitBox = hitBox;
	}

	public void detect(Player p, MovecraftLocation startPoint) {
		if (SQSpace.spaceWorlds.contains(p.getWorld().getName().toLowerCase())) {
			if (this.getType().getCanPilotSpace()) {
				pilot = p;
				AsyncTask task;
				if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
					task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				} else {
					task = new DetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				}
				AsyncManager.getInstance().submitTask(task, this);
			} else {
				p.sendMessage(ChatColor.RED + "This craft type cannot be piloted in space");
			}
		} else {
			if (this.getType().getCanPilotPlanet()) {
				pilot = p;
				AsyncTask task;
				if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
					task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				} else {
					task = new DetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				}
				AsyncManager.getInstance().submitTask(task, this);
			} else {
				p.sendMessage(ChatColor.RED + "This craft type cannot be piloted on a planet");
			}
		}
	}
	
	public void redetect(Player p, MovecraftLocation startPoint){
		if (SQSpace.spaceWorlds.contains(p.getWorld().getName().toLowerCase())) {
			if (this.getType().getCanPilotSpace()) {
				pilot = p;
				AsyncTask task;
				if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
					task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				} else {
					task = new RedetectTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				}
				AsyncManager.getInstance().submitTask(task, this);
			} else {
				p.sendMessage(ChatColor.RED + "This craft type cannot be piloted in space");
			}
		} else {
			if (this.getType().getCanPilotPlanet()) {
				pilot = p;
				AsyncTask task;
				if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
					task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				} else {
					task = new RedetectTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
				}
				AsyncManager.getInstance().submitTask(task, this);
			} else {
				p.sendMessage(ChatColor.RED + "This craft type cannot be piloted on a planet");
			}
		}
	}
	
	public void translate(int dx, int dy, int dz) {
		if(getType().isGroundVehicle() && dy != 0){
			int newHeight = hoverHeight;
			if(dy > 0){
				newHeight += 1;
			} else {
				newHeight -= 1;
			}
			if(newHeight >= 0 && newHeight <= getType().getHoverHeight()){
				pilot.sendMessage("New hover height: " + newHeight + " blocks above the ground.");
				hoverHeight = newHeight;
			}
			return;
		} else if(getType().isFlagship()){
			pilot.sendMessage("Flagships cannot move through realspace.");
			return;
		} else {
			//playersRidingLock.acquire();
			for(int i = 0; i < this.playersRidingShip.size(); i++){
				UUID u = this.playersRidingShip.get(i);
				Player p = Movecraft.getPlayer(u);
				FakeBlockUtils.sendFakeBlocks(p, p.getLocation());
			}
			//playersRidingLock.release();
			TranslationTaskData data = new TranslationTaskData(dx, dz, dy, getBlockList(), getHitBox(), minZ, minX, type.getMaxHeightLimit(), type.getMinHeightLimit());
			CraftSyncTranslateEvent event = new CraftSyncTranslateEvent(this, data);
			if (event.call()) {
				if(!checkChunks(getW(), getMinX(), getMinZ(), getHitBox(), dx, dz)){
					pilot.sendMessage("You're going a bit fast and the chunks can't render fast enough.");
					return;
				}
				lastMove = System.currentTimeMillis();
				AsyncManager.getInstance().submitTask(new TranslationTask(this, data), this);
			}
		}
	}

	public void rotate(Rotation rotation, MovecraftLocation originPoint) {
		AsyncManager.getInstance().submitTask(new RotationTask(this, originPoint, this.getBlockList(), rotation, this.getW()), this);
	}
	
	public void messageShipPlayers(String message){
		try{
			for(int i = 0; i < playersRidingShip.size(); i++){
				Player p = Movecraft.getPlayer(playersRidingShip.get(i));
				if(p != null){
					p.sendMessage(message);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMinZ() {
		return minZ;
	}

	public void setMinZ(int minZ) {
		this.minZ = minZ;
	}

	public int getMoveTaskId() {
		return moveTaskID;
	}

	public void setMoveTaskId(int taskID) {
		moveTaskID = taskID;
	}
	public boolean isProcessing(){
		return processing.get();
	}
	public void setProcessing(boolean processing){
		this.processing.set(processing);
	}
	public boolean processingCompareAndSet(boolean expect, boolean set){
		return this.processing.compareAndSet(expect, set);
	}
	public boolean isProcessingTeleport(){
		return processingTeleport.get();
	}
	public void setProcessingTeleport(boolean processingTeleport){
		this.processingTeleport.set(processingTeleport);
	}
	public boolean processingTeleportCompareAndSet(boolean expect, boolean set){
		return this.processingTeleport.compareAndSet(expect, set);
	}

	@SuppressWarnings("deprecation")
	public void extendLandingGear() {
		for (MovecraftLocation l : getBlockList()) {
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			if (b.getType() == Material.PISTON_BASE && b.getData() == 0) {
				Block above = b.getRelative(BlockFace.UP);
				if (above.getType() == Material.LAPIS_BLOCK) {
					Block below = b.getRelative(BlockFace.DOWN);
					if (below.getType() == Material.AIR) {
						Block belowTwo = below.getRelative(BlockFace.DOWN);
						if (belowTwo.getType() != Material.AIR) {
							above.setTypeIdAndData(152, (byte) 0, true);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void retractLandingGear() {
		for (MovecraftLocation l : getBlockList()) {
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			if (b.getType() == Material.PISTON_BASE && b.getData() == 8) {
				Block above = b.getRelative(BlockFace.UP);
				if (above.getType() == Material.REDSTONE_BLOCK) {
					above.setTypeIdAndData(22, (byte) 0, true);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void shootGuns(final Player p) {

		if (cannonCooldown == 0) {
			
			BlockFace playerFacing = GunUtils.yawToFace(p.getLocation().getYaw());
			int datavalue = GunUtils.getIntegerDirection(playerFacing);
			Material type = p.getEyeLocation().getBlock().getType();
			if (!this.processing.get() && type != Material.GLASS && type != Material.STAINED_GLASS) {
				this.processing.set(true);
				int cannonCount = 0;
				int allowed = this.getType().getAllowedCannons(p);
				
				cannonCooldown = getType().getCannonCooldown();
				
				for (MovecraftLocation l : getBlockList()) {

					final Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());

					if (b.getType() == Material.PISTON_BASE) {

						if (b.getData() == datavalue) {
							
							final Block behind = GunUtils.getBlockBehind(b);
							if (behind.getType() == Material.SPONGE) {
									
								cannonCount++;
									
								if(cannonCount <= allowed){
		
									CraftShootEvent event = new CraftShootEvent(this);
									event.call();
									
									if(!event.isCancelled()){
									
										double pitch = p.getLocation().getPitch();
										
										if (pitch > getType().getCannonMaxDegress()) {
											
											pitch = getType().getCannonMaxDegress();
											
										} else if (pitch < -getType().getCannonMaxDegress()) {
											
											pitch = -getType().getCannonMaxDegress();
											
										}
										
										double yaw = p.getLocation().getYaw();
										
										if (yaw < 0) {
											
											yaw = yaw + 360;
											
										}
										
										if (playerFacing.equals(BlockFace.NORTH)) {
											
											if (yaw > 180 + getType().getCannonMaxDegress()) {
												
												yaw = 180 + getType().getCannonMaxDegress();
												
											} else if (yaw < 180 - getType().getCannonMaxDegress()) {
												
												yaw = 180 - getType().getCannonMaxDegress();
												
											}
											
										} else if (playerFacing.equals(BlockFace.EAST))	 {
											
											if (yaw > 270 + getType().getCannonMaxDegress()) {
												
												yaw = 270 + getType().getCannonMaxDegress();
												
											} else if (yaw < 270 - getType().getCannonMaxDegress()) {
												
												yaw = 270 - getType().getCannonMaxDegress();
												
											}
											
										} else if (playerFacing.equals(BlockFace.SOUTH))	 {
											
											if (yaw > getType().getCannonMaxDegress()) {
												
												if (yaw > 180) {
													
													if (yaw < 360 - getType().getCannonMaxDegress()) {
														
														yaw = 360 - getType().getCannonMaxDegress();
														
													}
													
												} else {
													
													yaw = getType().getCannonMaxDegress();
													
												}

											}
											
										} else if (playerFacing.equals(BlockFace.WEST))	 {
											
											if (yaw > 90 + getType().getCannonMaxDegress()) {
												
												yaw = 90 + getType().getCannonMaxDegress();
												
											} else if (yaw < 90 - getType().getCannonMaxDegress()) {
												
												yaw = 90 - getType().getCannonMaxDegress();
												
											}
											
										}
										
										double x = Math.sin(Math.toRadians(yaw)) * -.25 * Math.cos(Math.toRadians(pitch));
										double y = Math.sin(Math.toRadians(pitch)) * -.25;
										double z = Math.cos(Math.toRadians(yaw)) * .25 * Math.cos(Math.toRadians(pitch));

										Location blockLocation = b.getRelative(playerFacing).getLocation();
										
										blockLocation.add(.5, .5, .5);
										
										double currentX = blockLocation.getX();
										double currentY = blockLocation.getY();
										double currentZ = blockLocation.getZ();
		
										b.getWorld().playSound(b.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0F, 1.0F);
		
										MovecraftLocation firstBlock = null;
										
										for (int i = 0; i < 400; i ++) {
											
											final Location location = new Location(b.getWorld(), currentX, currentY, currentZ);
											
											List<Entity> entites = new ArrayList<Entity>();
											entites.addAll(location.getWorld().getNearbyEntities(location, 1, 1, 1));
											
											for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
													
												if (entity instanceof LivingEntity) {
														
													if (entity != p) {
														
														i = 400;
														
														((LivingEntity) entity).damage(getType().getCannonDamage(), p);
														
														location.getWorld().playSound(location, Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
														
													}
	
												}
													
											}
												
											if (location.getBlock().getType().equals(Material.AIR)) {
												
												EmpirePlayer ep = EmpirePlayer.getOnlinePlayer(p);
												
												if (ep.getEmpire().equals(Empire.ARATOR)) {
													
													ParticleEffect.REDSTONE.sendColor(Bukkit.getOnlinePlayers(),location.getX(), location.getY(),location.getZ(), Color.BLUE);
													
												} else if (ep.getEmpire().equals(Empire.REQUIEM)) {
													
													ParticleEffect.REDSTONE.sendColor(Bukkit.getOnlinePlayers(),location.getX(), location.getY(),location.getZ(), Color.RED);
													
												} else if (ep.getEmpire().equals(Empire.YAVARI)) {
													
													ParticleEffect.REDSTONE.sendColor(Bukkit.getOnlinePlayers(),location.getX(), location.getY(),location.getZ(), Color.PURPLE);
													
												} else {
													
													ParticleEffect.REDSTONE.sendColor(Bukkit.getOnlinePlayers(),location.getX(), location.getY(),location.getZ(), Color.WHITE);
													
												}
						
												currentX = currentX + x;
												currentY = currentY + y;
												currentZ = currentZ + z;
												
											} else {
												
												boolean contains = false;
												
												for (MovecraftLocation ml : blockList) {
													
													if (ml.equals(new MovecraftLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()))) {
															
														if (firstBlock == null) {
														
															firstBlock = ml;
															
															currentX = currentX + x;
															currentY = currentY + y;
															currentZ = currentZ + z;

														} else if (firstBlock != ml) {
															
															i = 400;
															
														}
														
														contains = true;

													}
													
												}
												
												if (firstBlock != null) {
												
													if (firstBlock.equals(new MovecraftLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()))) {
													
														contains = true;
														
														currentX = currentX + x;
														currentY = currentY + y;
														currentZ = currentZ + z;
														
													}
													
												}
												
												if (!contains) {
													
													CraftProjectileDetonateEvent detonateEvent = new CraftProjectileDetonateEvent(p, location.getBlock());
													detonateEvent.call();
													
													if(!detonateEvent.isCancelled()){
														
														i = 400;
														
														Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
															
															public void run(){
																
																b.getWorld().createExplosion(location, getType().getLaserPower());
			
															}
															
														}, 1L);
														
													}

												}
		
											}
											
										}
										
									}
										
									/*Block twoinfront = b.getRelative(playerFacing).getRelative(playerFacing);
		
									behind.setType(Material.REDSTONE_BLOCK);
		
									// Location fLoc =
									// twoinfront.getLocation().toVector().add(GunUtils.getFireBallVelocity(playerFacing).multiply(2)).toLocation(twoinfront.getWorld(),
									// 0, 0);
									Fireball f = ((Fireball) twoinfront.getLocation().getWorld().spawn(twoinfront.getLocation(), Fireball.class));
		
									f.setDirection(GunUtils.getFireBallVelocity(playerFacing).multiply(2));
									f.setShooter(pilot);
									f.setIsIncendiary(true);
									f.setYield(2.5F);
									f.setBounce(false);
									if(twoinfront.getType() == Material.AIR){
										CraftShootEvent event = new CraftShootEvent(this);
										event.call();
										if(!event.isCancelled()){
											new LaserBolt(twoinfront, playerFacing, this.pilot);
											twoinfront.getWorld().playSound(twoinfront.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0F, 1.0F);
										}
									}
									Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
		
										@Override
										public void run() {
											if(behind.getType() == Material.REDSTONE_BLOCK){
												behind.setType(Material.SPONGE);
											}
										}
									}, 5L);*/
								}
							}
						}
					}
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						processing.set(false);
					}
				}, 10L);
			}
			
		} else {
			
			p.sendMessage(ChatColor.RED + "The cannons are on cooldown");
			
		}

	}

	public String getGivenName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Player getPilot() {
		// TODO Auto-generated method stub
		return pilot;
	}

	public ArrayList<UUID> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setReleased(boolean released){
		this.released = released;
	}
	
	public boolean isReleased(){
		return released;
	}
	
	public ArrayList<MovecraftLocation> getSignLocations(){
		ArrayList<MovecraftLocation> retval = new ArrayList<MovecraftLocation>();
		for(MovecraftLocation l : getBlockList()){
			int id = getW().getBlockTypeIdAt(l.getX(), l.getY(), l.getZ());
			if(id == 68 || id == 63){
				retval.add(l);
			}
		}
		return retval;
	}
	
	public boolean hasMovedInLastSecond(){
		return System.currentTimeMillis() - lastMove <= 1000;
	}
	
	public int getExtraSpeed(){
		if (type.getCanSpeedScale()) {
			if (blockList.length >= 200 && blockList.length < 400) {
				return 2;		
			} else if (blockList.length >= 400 && blockList.length < 600) {
				return 3;
			} else if (blockList.length >= 600 && blockList.length < 800) {
				return 4;
			} else if (blockList.length >= 800 && blockList.length < 1000) {
				return 5;
			} else if (blockList.length >= 1000 && blockList.length < 1200) {
				return 4;
			} else if (blockList.length >= 1200 && blockList.length < 1400) {
				return 3;
			} else if (blockList.length >= 1400 && blockList.length < 1600) {
				return 2;
			} else if (blockList.length >= 1600 && blockList.length < 1800) {
				return 1;
			}
		}
		return 0;
	}
	//a thread safe method that checks the chunks with no chance of crashing
	public boolean checkChunks(World w, int minX, int minZ, int[][][] hitBox, int dx, int dz) {
		if (dx == 0 && dz == 0)
			return true;

		int maxX = minX + hitBox.length;
		int maxZ = minZ + hitBox[0].length;
		
		int minChunkX = minX >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkX = maxX >> 4;
		int maxChunkZ = maxZ >> 4;

		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				if (!w.isChunkLoaded(x, z)) {
					System.out.println("Chunks not loaded caught!");
					return false;
				}
			}
		}
		return true;
	}
}
