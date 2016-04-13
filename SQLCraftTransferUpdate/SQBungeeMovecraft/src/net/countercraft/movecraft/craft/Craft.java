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
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.detection.PodDetectionTask;
import net.countercraft.movecraft.async.detection.RedetectTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.async.translation.TranslationTaskData;
import net.countercraft.movecraft.event.CraftShootEvent;
import net.countercraft.movecraft.event.CraftSyncTranslateEvent;
import net.countercraft.movecraft.projectile.LaserBolt;
import net.countercraft.movecraft.slip.WarpUtils;
import net.countercraft.movecraft.utils.FakeBlockUtils;
import net.countercraft.movecraft.utils.GunUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.PlayerFlightUtil;
import net.countercraft.movecraft.utils.Rotation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
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
	public int vX, vZ;
	public Vector<UUID> playersRidingShip = new Vector<UUID>();
	//public Semaphore playersRidingLock = new Semaphore(1);
	public int warpCoordsX;
	public int warpCoordsZ;
	public Location originalPilotLoc = null;
	public Player pilot;
	private boolean released = false;
	public boolean isJamming = false;
	private long lastMove = 0;

	public Craft(CraftType type, World world) {
		this.type = type;
		this.w = world;
		this.blockList = new MovecraftLocation[1];
		this.moveTaskID = -1;
		xDist = 0;
		yDist = 0;
		zDist = 0;
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
		pilot = p;
		AsyncTask task;
		if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
			task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
		} else {
			task = new DetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
		}
		AsyncManager.getInstance().submitTask(task, this);
	}
	
	public void redetect(Player p, MovecraftLocation startPoint){
		pilot = p;
		AsyncTask task;
		if(this.getType().getCraftName().equalsIgnoreCase("Pod")){
			task = new PodDetectionTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
		} else {
			task = new RedetectTask(this, startPoint, type.getMinSize(), type.getMaxSize(), type.getAllowedBlocks(), type.getForbiddenBlocks(), p.getName(), w);
		}
		AsyncManager.getInstance().submitTask(task, this);
	}
	
	public void translate(int dx, int dy, int dz)
	{
		translate(dx, dy, dz, true);
	}
	
	public void translate(int dx, int dy, int dz, boolean doAsyncTeleport) {
		if (w.getEnvironment() == Environment.THE_END){
			WarpUtils.translate(this, dx, dy, dz);
		} else if(getType().isGroundVehicle() && dy != 0){
			pilot.sendMessage("Ground Vehicles cannot move up and down.");
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
			TranslationTaskData data = new TranslationTaskData(dx, dz, dy, getBlockList(), getHitBox(), minZ, minX, type.getMaxHeightLimit(), type.getMinHeightLimit(), doAsyncTeleport);
			CraftSyncTranslateEvent event = new CraftSyncTranslateEvent(this, data);
			if (event.call()) {
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
		Material type = p.getEyeLocation().getBlock().getType();
		if (!this.processing.get() && type != Material.GLASS && type != Material.STAINED_GLASS) {
			this.processing.set(true);
			int cannonCount = 0;
			int allowed = this.getType().getAllowedCannons(p);
			for (MovecraftLocation l : getBlockList()) {

				Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());

				if (b.getType() == Material.PISTON_BASE) {

					if (b.getData() == datavalue) {
						final Block behind = GunUtils.getBlockBehind(b);
							if (behind.getType() == Material.SPONGE) {
								
								cannonCount++;
								
								if(cannonCount <= allowed){
	
								Block twoinfront = b.getRelative(playerFacing).getRelative(playerFacing);
	
								behind.setType(Material.REDSTONE_BLOCK);
	
								// Location fLoc =
								// twoinfront.getLocation().toVector().add(GunUtils.getFireBallVelocity(playerFacing).multiply(2)).toLocation(twoinfront.getWorld(),
								// 0, 0);
								/*Fireball f = ((Fireball) twoinfront.getLocation().getWorld().spawn(twoinfront.getLocation(), Fireball.class));
	
								f.setDirection(GunUtils.getFireBallVelocity(playerFacing).multiply(2));
								f.setShooter(pilot);
								f.setIsIncendiary(true);
								f.setYield(2.5F);
								f.setBounce(false);*/
								if(twoinfront.getType() == Material.AIR){
									CraftShootEvent event = new CraftShootEvent(this);
									event.call();
									if(!event.isCancelled()){
										new LaserBolt(twoinfront, playerFacing, this.pilot);
										twoinfront.getWorld().playSound(twoinfront.getLocation(), Sound.SHOOT_ARROW, 2.0F, 1.0F);
									}
								}
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable() {
	
									@Override
									public void run() {
										if(behind.getType() == Material.REDSTONE_BLOCK){
											behind.setType(Material.SPONGE);
										}
									}
								}, 5L);
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
}
