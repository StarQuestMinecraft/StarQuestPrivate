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

package net.countercraft.movecraft.async;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.detection.DetectionTaskData;
import net.countercraft.movecraft.async.detection.PodDetectionTask;
import net.countercraft.movecraft.async.detection.RedetectTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.shield.PendingActivation;
import net.countercraft.movecraft.shield.ShieldUtils;
import net.countercraft.movecraft.task.AutopilotRunTask;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.EntityUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncManager extends BukkitRunnable {
	private static final AsyncManager instance = new AsyncManager();
	private final HashMap<AsyncTask, Craft> ownershipMap = new HashMap<AsyncTask, Craft>();
	private final BlockingQueue<AsyncTask> finishedAlgorithms = new LinkedBlockingQueue<AsyncTask>();
	private final HashSet<Craft> clearanceSet = new HashSet<Craft>();
	private static CraftType CARRIER = InteractListener.getCraftTypeFromString("Carrier");
	private static CraftType FLAGSHIP = InteractListener.getCraftTypeFromString("Flagship");

	public static AsyncManager getInstance() {
		return instance;
	}

	private AsyncManager() {
	}

	public void submitTask( AsyncTask task, Craft c ) {
		if (task instanceof DetectionTask){
			ownershipMap.put( task, c );
			task.runTaskAsynchronously( Movecraft.getInstance() );
		}
		else if (c.processingCompareAndSet(false, true)) {
			ownershipMap.put( task, c );
			task.runTaskAsynchronously( Movecraft.getInstance() );
		}
	}

	public void submitCompletedTask( AsyncTask task ) {
		finishedAlgorithms.add( task );
	}

	void processAlgorithmQueue() {
		int runLength = 10;
		int queueLength = finishedAlgorithms.size();

		runLength = Math.min( runLength, queueLength );

		for ( int i = 0; i < runLength; i++ ) {
			AsyncTask poll = finishedAlgorithms.poll();
			final Craft c = ownershipMap.get( poll );

			if ( poll instanceof DetectionTask ) {
				// Process detection task

				DetectionTask task = ( DetectionTask ) poll;
				DetectionTaskData data = task.getData();

				Player p = Movecraft.getInstance().getServer().getPlayer( data.getPlayername() );
				Craft pCraft = CraftManager.getInstance().getCraftByPlayer( p );

				if ( pCraft != null ) {
					//Player is already controlling a craft
					p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Detection - Failed - Already commanding a craft" ) ) );
				} else {
					if ( data.failed() ) {
						p.sendMessage( data.getFailMessage() );
					} else {
						Craft[] craftsInWorld = CraftManager.getInstance().getCraftsInWorld( c.getW() );
						boolean failed = false;
						
						if (numcannons(data.getBlockList(), c.getW()) > c.getType().getAllowedCannons()){
							p.sendMessage(ChatColor.RED + "Your ship has too many cannons!");
							failed = true;
						}
						if ( craftsInWorld != null ) {
							for ( Craft craft : craftsInWorld ) {

								if ( BlockUtils.arrayContainsOverlap( craft.getBlockList(), data.getBlockList() ) ) {
									if(craft.pilot == p){
										CraftManager.getInstance().removeCraft(craft);
									} else {
										p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Detection - Failed Craft is already being controlled" ) ) );
										System.out.println("MOVECRAFT-DETECTION: craft already controlled!");
										System.out.println(c.pilot.getName() + " is already controlling part of this ship!");
										System.out.println(c.getType().getCraftName() + " = type, " + c.getBlockList().length + " = size");
										failed = true;
									}
								}

							}
						} else {
							//detect ship signs
						}
						if ( !failed ) {
							
							// add any players to the ship that should be on it
							try{
								c.playersRidingLock.acquire();
								for (Player plr : data.getWorld().getPlayers()) {
									if (MathUtils.playerIsWithinBoundingPolygon(data.getHitBox(), data.getMinX(), data.getMinZ(), MathUtils.bukkit2MovecraftLoc(plr.getLocation()))) {
										if (!c.playersRidingShip.contains(plr.getUniqueId())) {
											c.playersRidingShip.add(plr.getUniqueId());
											plr.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
											ShieldUtils.checkForAndRemoveShield(plr);
											ShieldUtils.removePendingActivationsForPlayer(plr);
										}
									}
								}
							}catch(Exception e){
								e.printStackTrace();
							}
							c.playersRidingLock.release();
							c.setBlockList( data.getBlockList() );
							c.setHitBox( data.getHitBox() );
							c.setMinX( data.getMinX() );
							c.setMinZ( data.getMinZ() );
							c.originalPilotLoc = p.getLocation();
							c.retractLandingGear();
							Movecraft.getInstance().getLogger().log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Detection - Success - Log Output" ), p.getName(), c.getType().getCraftName(), c.getBlockList().length, c.getMinX(), c.getMinZ() ) );
							CraftManager.getInstance().addCraft( c, p );
							if(data.getMainSign() != null){
								Movecraft.getInstance().getStarshipDatabase().removeStarshipAtLocation(data.getMainSign().getLocation());
							}
							if(poll instanceof RedetectTask){
								p.sendMessage("Succesfully piloted existing starship!");
							} else if(poll instanceof PodDetectionTask){
								p.sendMessage("Succesfully piloted a pod!");
							} else {
								p.sendMessage("Succesfully detected and piloted a new starship! Your ship is now saved, you can right-click the sign to pilot it without detecting.");
							}
						}
					}
				}


			} else if ( poll instanceof TranslationTask ) {
				//Process translation task

				TranslationTask task = ( TranslationTask ) poll;
				Player p = c.pilot;

				// Check that the craft hasn't been sneakily unpiloted
				if ( p.isOnline()) {

					if ( task.getData().failed()) {
						//The craft translation failed
						p.sendMessage( task.getData().getFailMessage() );
						if(task.getData().isChunksFail()){
							Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
								public void run(){
									clear(c);
								}
							}, 5L);
						} else {
							clear(c);
						}
						if (AutopilotRunTask.autopilotingCrafts.contains(c)){
							AutopilotRunTask.stopAutopiloting(c, p);
						}
					} else {
						//The craft is clear to move, perform the block updates

						MapUpdateCommand[] updates = task.getData().getUpdates();
						EntityUpdateCommand[] eUpdates=task.getData().getEntityUpdates();
						
						//addWorldUpdate returns false if succesful
						boolean failed = MapUpdateManager.getInstance().addWorldUpdate( c.getW(), updates, eUpdates );

						if ( !failed ) {

							c.setBlockList( task.getData().getBlockList() );

							/*// Move entities
							for ( Entity pTest : c.getW().getPlayers() ) {

								if ( MathUtils.playerIsWithinBoundingPolygon( c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc( pTest.getLocation() ) ) ) {

									// Player is onboard this craft
									Vector velocity = pTest.getVelocity().clone();
									pTest.teleport( pTest.getLocation().add( task.getData().getDx(), task.getData().getDy(), task.getData().getDz() ) );
									pTest.setVelocity( velocity );
									
								}

							}*/
							c.setMinX( task.getData().getMinX() );
							c.setMinZ( task.getData().getMinZ() );
							c.setHitBox( task.getData().getHitbox() );
							
							//update distance traveled
							c.xDist += task.getData().getDx();
							c.yDist += task.getData().getDy();
							c.zDist += task.getData().getDz();

						} else {
							Movecraft.getInstance().getLogger().log( Level.SEVERE, String.format( I18nSupport.getInternationalisedString( "Translation - Craft collision" ) ) );
							clear(c);
						}
					}

				}


			} else if ( poll instanceof RotationTask ) {
				// Process rotation task
				final RotationTask task = ( RotationTask ) poll;
				final Player p = c.pilot;

				// Check that the craft hasn't been sneakily unpiloted
				if ( p != null ) {

					if ( task.isFailed() ) {
						//The craft translation failed
						p.sendMessage( task.getFailMessage() );
						clear(c);
					} else {
						MapUpdateCommand[] updates = task.getUpdates();
						EntityUpdateCommand[] eUpdates=task.getEntityUpdates();
						boolean failed = MapUpdateManager.getInstance().addWorldUpdate( c.getW(), updates, eUpdates );

						if ( !failed ) {
							
							c.setBlockList( task.getBlockList() );

							Location originPoint = new Location( c.getW(), task.getOriginPoint().getX(), task.getOriginPoint().getY(), task.getOriginPoint().getZ() );
							// Move entities
							for (String s : c.playersWithBedspawnsOnShip){
								Bedspawn b = Bedspawn.getBedspawn(s);
								MovecraftLocation oldLoc = new MovecraftLocation(b.x, b.y, b.z);
								Location l = new Location(c.getW(), oldLoc.getX() + c.xDist, oldLoc.getY() + c.yDist, oldLoc.getZ() + c.zDist);
								
								if(task.getRotation() == Rotation.CLOCKWISE){
									int shift = originPoint.getBlockX() - originPoint.getBlockZ();
									double Zsymmetry = originPoint.getBlockZ();
									double x = (l.getZ() - (l.getZ() - Zsymmetry) * 2.0D + shift) + 1;
									double z = l.getX() - shift;
									b.x = (int) x;
									b.z = (int) z;
								}
								else if (task.getRotation() == Rotation.ANTICLOCKWISE){
									int shift = originPoint.getBlockX() - originPoint.getBlockZ();
									double Xsymmetry = originPoint.getBlockX();
									double x = l.getZ() + shift;
									double z = (l.getX() - (l.getX() - Xsymmetry) * 2.0D - shift) + 1;
									b.x = (int) x;
									b.z = (int) z;
								}
								Bedspawn.saveBedspawn(b);
							}
														
							c.xDist = 0;
							c.yDist = 0;
							c.zDist = 0;


							c.setMinX( task.getMinX() );
							c.setMinZ( task.getMinZ() );
							c.setHitBox( task.getHitbox() );

						} else {

							Movecraft.getInstance().getLogger().log( Level.SEVERE, String.format( I18nSupport.getInternationalisedString( "Rotation - Craft Collision" ) ) );
							clear(c);

						}
					}
				}
			}

			ownershipMap.remove( poll );
		}
	}

	public void run() {
		clearAll();
		processAlgorithmQueue();
	}

	public void clear( Craft c ) {
		clearanceSet.add( c );
	}

	private void clearAll() {
		for ( Craft c : clearanceSet ) {
			c.setProcessing(false);
		}

		clearanceSet.clear();
	}
	
	private int numcannons(MovecraftLocation[] blocklist, World w){
		int numcannons = 0;
		for (MovecraftLocation l: blocklist){
			
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			
			if (b.getType() == Material.SPONGE){
				if (b.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE){
					numcannons++;
				} else if(b.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE) {
					numcannons++;
				} else if(b.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE) {
					numcannons++;
				} else if(b.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE) {
					numcannons++;
				}
			}
		}
		return numcannons;
	}
	
}
