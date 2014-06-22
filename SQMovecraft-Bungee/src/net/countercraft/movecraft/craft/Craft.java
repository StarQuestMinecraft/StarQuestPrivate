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
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.AsyncManager;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.async.translation.TranslationTaskData;
import net.countercraft.movecraft.event.CraftPilotEvent;
import net.countercraft.movecraft.event.CraftSyncTranslateEvent;
import net.countercraft.movecraft.utils.GunUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;
import net.countercraft.movecraft.utils.WarpUtils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

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
	public int vX, vZ;
	public ArrayList<UUID> playersRidingShip = new ArrayList<UUID>();
	public Semaphore playersRidingLock = new Semaphore(1);
	public int warpCoordsX;
	public int warpCoordsZ;
	public Location originalPilotLoc = null;
	public Player pilot;

	public Craft(CraftType type, World world) {
		this.type = type;
		this.w = world;
		this.blockList = new MovecraftLocation[1];
		this.moveTaskID = -1;
		xDist = 0;
		yDist = 0;
		zDist = 0;
	}

	public void setOriginalPilotLoc(Location l) {
		originalPilotLoc = l;
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

	public void detect(String playerName, MovecraftLocation startPoint) {
		pilot = Bukkit.getServer().getPlayer(playerName);
		CraftPilotEvent event = new CraftPilotEvent(this);
		if (event.call())
			AsyncManager.getInstance().submitTask(new DetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), playerName, w), this);
	}

	public void translate(int dx, int dy, int dz) {
		if (w.getEnvironment() == Environment.THE_END)
			WarpUtils.translate(this, dx, dy, dz);
		else {
			TranslationTaskData data = new TranslationTaskData(dx, dz, dy, getBlockList(), getHitBox(), minZ, minX, type.getMaxHeightLimit(), type.getMinHeightLimit());
			CraftSyncTranslateEvent event = new CraftSyncTranslateEvent(this, data);
			if (event.call()) {
				AsyncManager.getInstance().submitTask(new TranslationTask(this, data), this);
			}
		}
	}

	public void rotate(Rotation rotation, MovecraftLocation originPoint) {
		AsyncManager.getInstance().submitTask(new RotationTask(this, originPoint, this.getBlockList(), rotation, this.getW()), this);
	}

	public void messageShipPlayers(String message) {
		for (UUID u : playersRidingShip) {
			Player p = Movecraft.playerIndex.get(u);
			if (p != null) {
				p.sendMessage(message);
			}
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

	public boolean isProcessing() {
		return processing.get();
	}

	public void setProcessing(boolean processing) {
		this.processing.set(processing);
	}

	public boolean processingCompareAndSet(boolean expect, boolean set) {
		return this.processing.compareAndSet(expect, set);
	}

	public boolean isProcessingTeleport() {
		return processingTeleport.get();
	}

	public void setProcessingTeleport(boolean processingTeleport) {
		this.processingTeleport.set(processingTeleport);
	}

	public boolean processingTeleportCompareAndSet(boolean expect, boolean set) {
		return this.processingTeleport.compareAndSet(expect, set);
	}

	@SuppressWarnings("deprecation")
	public void extendLandingGear() {
		for (MovecraftLocation l : getBlockList()) {
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			if (b.getType() == Material.PISTON_BASE && b.getData() == 0) {
				Block above = b.getRelative(BlockFace.UP);
				if (above.getType() == Material.SPONGE) {
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
				if (above.getTypeId() == 152) {
					above.setTypeIdAndData(19, (byte) 0, true);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void shootGuns(Player p) {

		BlockFace playerFacing = GunUtils.yawToFace(p.getLocation().getYaw());
		int datavalue = GunUtils.getIntegerDirection(playerFacing);

		if (!this.processing.get()) {
			this.processing.set(true);
			for (MovecraftLocation l : getBlockList()) {

				Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());

				if (b.getType() == Material.PISTON_BASE) {

					if (b.getData() == datavalue) {
						final Block behind = GunUtils.getBlockBehind(b);
						if (behind.getType() == Material.SPONGE) {

							Block twoinfront = b.getRelative(playerFacing).getRelative(playerFacing);

							behind.setType(Material.REDSTONE_BLOCK);

							// Location fLoc =
							// twoinfront.getLocation().toVector().add(GunUtils.getFireBallVelocity(playerFacing).multiply(2)).toLocation(twoinfront.getWorld(),
							// 0, 0);
							Fireball f = ((Fireball) twoinfront.getLocation().getWorld().spawn(twoinfront.getLocation(), Fireball.class));/*
																																		 * .
																																		 * spawnEntity
																																		 * (
																																		 * twoinfront
																																		 * .
																																		 * getLocation
																																		 * (
																																		 * )
																																		 * ,
																																		 * Fireball
																																		 * .
																																		 * class
																																		 * )
																																		 * )
																																		 */
							f.setDirection(GunUtils.getFireBallVelocity(playerFacing).multiply(2));
							f.setShooter(pilot);
							twoinfront.getWorld().playSound(twoinfront.getLocation(), Sound.SHOOT_ARROW, 2.0F, 1.0F);

							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {

								@Override
								public void run() {
									behind.setType(Material.SPONGE);
								}
							}, 5L);

							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {

								@Override
								public void run() {
									processing.set(false);
								}
							}, 7L);
						}
					}
				}
			}
		}
	}
}
