package net.countercraft.movecraft.async;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.detection.DetectionTaskData;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.AutopilotRunTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.BlockUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;
import net.countercraft.movecraft.utils.map.EntityUpdateCommand;
import net.countercraft.movecraft.utils.map.MapUpdateCommand;
import net.countercraft.movecraft.utils.map.MapUpdateManager;
import net.countercraft.movecraft.utils.mechanism.MovingPartUtils;

import org.apache.commons.collections.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class AsyncManager extends BukkitRunnable {
	private static final AsyncManager instance = new AsyncManager();
	private final HashMap<AsyncTask, Craft> ownershipMap = new HashMap<AsyncTask, Craft>();
	private final BlockingQueue<AsyncTask> finishedAlgorithms = new LinkedBlockingQueue<AsyncTask>();
	private final HashSet<Craft> clearanceSet = new HashSet<Craft>();
//	private final HashMap<World, ArrayList<MovecraftLocation>> sinkingBlocks = new HashMap<World, ArrayList<MovecraftLocation>>();
//	private final HashMap<World, HashSet<MovecraftLocation>> waterFillBlocks = new HashMap<World, HashSet<MovecraftLocation>>();
//	private long lastSinkingUpdate = 0;

	public static AsyncManager getInstance() {
		return instance;
	}

	private AsyncManager() {
	}

	public void submitTask( AsyncTask task, Craft c ) {
		if ( !c.isProcessing() ) {
			c.setProcessing( true );
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
			boolean sentMapUpdate=false;
			AsyncTask poll = finishedAlgorithms.poll();
			Craft c = ownershipMap.get( poll );

			if ( poll instanceof DetectionTask ) {
				// Process detection task

				DetectionTask task = ( DetectionTask ) poll;
				DetectionTaskData data = task.getData();

				Player p = Movecraft.getInstance().getServer().getPlayer( data.getPlayername() );
				Craft pCraft = CraftManager.getInstance().getCraftByPlayer( p );

				if ( pCraft != null && p != null ) {
					//Player is already controlling a craft
					p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Detection - Failed - Already commanding a craft" ) ) );
				} else {
					if ( data.failed() ) {
						if(p!=null)
							p.sendMessage( data.getFailMessage() );
						else
							Movecraft.getInstance().getLogger().log( Level.INFO,"NULL Player Craft Detection failed:"+data.getFailMessage());

					} else {
						Craft[] craftsInWorld = CraftManager.getInstance().getCraftsInWorld( c.getW() );
						boolean failed = false;
						
						if (numcannons(data.getBlockList(), c.getW()) > c.getType().getAllowedCannons()){
							p.sendMessage(ChatColor.RED + "Your ship has too many cannons!");
							failed = true;
						}
						
						if ( craftsInWorld != null ) {
							for ( Craft craft : craftsInWorld ) {

								if ( BlockUtils.arrayContainsOverlap( craft.getBlockList(), data.getBlockList() ) && p!=null ) {
									p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Detection - Failed Craft is already being controlled" ) ) );
									failed = true;
								}

							}
						}
						
						boolean foundMainSign = false;
						for (MovecraftLocation l : data.getSignLocations()){
							Location loc = new Location(c.getW(), l.getX(), l.getY(), l.getZ());
							Sign s = (Sign) loc.getBlock().getState();
							//check for [private] signs
							if (s.getLine(0).equals("[private]")){
								if (!Movecraft.signContainsPlayername(s, data.getPlayername())){
									failed = true;
									p.sendMessage(ChatColor.RED + "You are attatched to a door that is locked to someone besides you.");
								}
							}
							//check each sign to see if it's a craft sign
							boolean isCraftType = (InteractListener.getCraftTypeFromString(s.getLine(0)) != null);
							if (isCraftType){
								//special rules for carriers!
								if (c.getType().equals(InteractListener.getCraftTypeFromString("Carrier")) || c.getType().equals(InteractListener.getCraftTypeFromString("Flagship"))){
									if (!Movecraft.signContainsPlayername(s, data.getPlayername())){
										failed = true;
										p.sendMessage(ChatColor.RED + "Your ship seems to be attatched to another ship that isn't yours.");
									}
									
								//other ships
								} else {
									
									//don't count the main sign as a different ship.
									if(!foundMainSign){
										if(c.getType().equals(InteractListener.getCraftTypeFromString(s.getLine(0)))){
											if (Movecraft.signContainsPlayername(s, data.getPlayername())){
												foundMainSign = true;
											}
										}
									}else{
										failed = true;
										p.sendMessage(ChatColor.RED + "Your ship seems to have more than one ship sign, or is attatched to another ship.");
										break;
									}
								}
							}
						}
						
						if ( !failed ) {
							c.setBlockList( data.getBlockList() );
							c.setHitBox( data.getHitBox() );
							c.setMinX( data.getMinX() );
							c.setMinZ( data.getMinZ() );
							c.originalPilotLoc = p.getLocation();
							c.retractLandingGear();
							if(p!=null) {
								p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Detection - Successfully piloted craft" ) ) );
								Movecraft.getInstance().getLogger().log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Detection - Success - Log Output" ), p.getName(), c.getType().getCraftName(), c.getBlockList().length, c.getMinX(), c.getMinZ() ) );
							} else {
								Movecraft.getInstance().getLogger().log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Detection - Success - Log Output" ), "NULL PLAYER", c.getType().getCraftName(), c.getBlockList().length, c.getMinX(), c.getMinZ() ) );								
							}
							CraftManager.getInstance().addCraft( c, p );
						}
					}
				}


			} else if ( poll instanceof TranslationTask ) {
				//Process translation task

				TranslationTask task = ( TranslationTask ) poll;
				Player p = c.pilot;

				// Check that the craft hasn't been sneakily unpiloted
		//		if ( p != null ) {     cruiseOnPilot crafts don't have player pilots

					if ( task.getData().failed() ) {
						//The craft translation failed
						if( p != null && p.isOnline())
							p.sendMessage( task.getData().getFailMessage() );
							
					} else {
						//The craft is clear to move, perform the block updates

						MapUpdateCommand[] updates = task.getData().getUpdates();
						EntityUpdateCommand[] eUpdates=task.getData().getEntityUpdates();
						boolean failed = MapUpdateManager.getInstance().addWorldUpdate( c.getW(), updates, eUpdates);

						if ( !failed ) {
							sentMapUpdate=true;
							c.setBlockList( task.getData().getBlockList() );


							c.setMinX( task.getData().getMinX() );
							c.setMinZ( task.getData().getMinZ() );
							c.setHitBox( task.getData().getHitbox() );

							//update distance traveled
							c.xDist += task.getData().getDx();
							c.yDist += task.getData().getDy();
							c.zDist += task.getData().getDz();
							
						} else {
							if(AutopilotRunTask.autopilotingCrafts.contains(task.getCraft())){
								AutopilotRunTask.stopAutopiloting(c, c.pilot);
							}
							Movecraft.getInstance().getLogger().log( Level.SEVERE, String.format( I18nSupport.getInternationalisedString( "Translation - Craft collision" ) ) );

						}
					}

	//			}


			} else if ( poll instanceof RotationTask ) {
				// Process rotation task
				RotationTask task = ( RotationTask ) poll;
				Player p = CraftManager.getInstance().getPlayerFromCraft( c );

				// Check that the craft hasn't been sneakily unpiloted
				if ( p != null && p.isOnline()) {

					if ( task.isFailed() ) {
						//The craft translation failed, don't try to notify them if there is no pilot
						if(p!=null)
							p.sendMessage( task.getFailMessage() );
						else
							Movecraft.getInstance().getLogger().log( Level.INFO,"NULL Player Rotation Failed: "+task.getFailMessage());
					} else {
						MapUpdateCommand[] updates = task.getUpdates();
						EntityUpdateCommand[] eUpdates=task.getEntityUpdates();

						boolean failed = MapUpdateManager.getInstance().addWorldUpdate( c.getW(), updates, eUpdates);
 
						if ( !failed ) {
							for(MovecraftLocation l : task.getSignLocations()){
								Sign s = (Sign) c.getW().getBlockAt(l.getX(), l.getY(), l.getZ()).getState();
								if (s.getLine(0).equals(ChatColor.GREEN + "Moving Part")){
									MovingPartUtils.rotateSchematic(s, p, task.getRotation());
								}
							}
							
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
							sentMapUpdate=true;
							
							c.setBlockList( task.getBlockList() );
							c.setMinX( task.getMinX() );
							c.setMinZ( task.getMinZ() );
							c.setHitBox( task.getHitbox() );

						} else {

							Movecraft.getInstance().getLogger().log( Level.SEVERE, String.format( I18nSupport.getInternationalisedString( "Rotation - Craft Collision" ) ) );

						}
					}
				}
			}

			ownershipMap.remove( poll );
			
			// only mark the craft as having finished updating if you didn't send any updates to the map updater. Otherwise the map updater will mark the crafts once it is done with them.
			if(!sentMapUpdate) {
				clear( c ); 
			}
		}
	}

	public void run() {
		clearAll();
		processAlgorithmQueue();
	}

	private void clear( Craft c ) {
		clearanceSet.add( c );
	}

	private void clearAll() {
		for ( Craft c : clearanceSet ) {
			c.setProcessing( false );
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
