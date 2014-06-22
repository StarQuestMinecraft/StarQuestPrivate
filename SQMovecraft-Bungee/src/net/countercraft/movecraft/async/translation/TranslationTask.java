package net.countercraft.movecraft.async.translation;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.bungee.RepeatTryServerJumpTask;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.CarUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.map.EntityUpdateCommand;
import net.countercraft.movecraft.utils.map.LocationUtils;
import net.countercraft.movecraft.utils.map.MapUpdateCommand;
import net.countercraft.movecraft.utils.mechanism.BorderUtils;

import org.apache.commons.collections.ListUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;

public class TranslationTask extends AsyncTask {
	private TranslationTaskData data;

	public TranslationTask( Craft c, TranslationTaskData data ) {
		super( c );
		this.data = data;
	}

	@Override
	public void excecute() {
		MovecraftLocation[] blocksList = data.getBlockList();

		int [][][] hb=getCraft().getHitBox();

		
		if (getCraft().getType().isGroundVehicle()) {
			data.setDy(CarUtils.getNewdY(getCraft(), data.getDx(), data.getDz()));
		}
		
		if(!checkChunks(getCraft().getW(), getCraft().getMinX(), getCraft().getMinZ(), getCraft().getHitBox(), data.getDx(), data.getDz())){
			fail("You're going a bit fast and the chunks can't render fast enough.");
		}
		
		if(!data.failed()){
			Location l = getCraft().pilot.getLocation();
			int nx = l.getBlockX() + data.getDx();
			int nz = l.getBlockZ() + data.getDz();
			if(AutopilotRunTask.autopilotingCrafts.contains(getCraft())){
				if(!BorderUtils.isWithinBorderIncludePadding(nx, nz, 30)){
					fail("You left the autopilot on a bit too long and are almost at the worldborder!");
				}
			} else {
				if (!BorderUtils.isWithinBorderIncludePadding(nx, nz)) {
					fail("You have almost reached the world border! Turn back now!");
				}
			}
		}
		
		List<MovecraftLocation> tempBlockList=new ArrayList<MovecraftLocation>();
		HashSet<MovecraftLocation> existingBlockSet = new HashSet<MovecraftLocation>( Arrays.asList( blocksList ) );
		HashSet<EntityUpdateCommand> entityUpdateSet = new HashSet<EntityUpdateCommand>();
		Set<MapUpdateCommand> updateSet = new HashSet<MapUpdateCommand>();
		
		if(! data.failed()){
	        for ( int i = 0; i < blocksList.length; i++ ) {
				MovecraftLocation oldLoc = blocksList[i];
				MovecraftLocation newLoc = oldLoc.translate( data.getDx(), data.getDy(), data.getDz() );
	//			newBlockList[i] = newLoc;
	
				if ( newLoc.getY() > data.getMaxHeight() && newLoc.getY() > oldLoc.getY() ) {
					fail( String.format( I18nSupport.getInternationalisedString( "Translation - Failed Craft hit height limit" ) ) );
					break;
				} else if ( newLoc.getY() < data.getMinHeight()  && newLoc.getY() < oldLoc.getY()) {
					fail( String.format( I18nSupport.getInternationalisedString( "Translation - Failed Craft hit minimum height limit" ) ) );
					break;
				}
	            
	            int testID = getCraft().getW().getBlockTypeIdAt(newLoc.getX(), newLoc.getY(), newLoc.getZ());
	            boolean blockObstructed = (testID != 0 && testID != 36 && !existingBlockSet.contains( newLoc ));
				
				if ( blockObstructed ) {
	 				 	// Explode if the craft is set to have a CollisionExplosion. Also keep moving for spectacular ramming collisions
					 		fail( String.format( I18nSupport.getInternationalisedString( "Translation - Failed Craft is obstructed" )+" @ %d,%d,%d", oldLoc.getX(), oldLoc.getY(), oldLoc.getZ() ) );
					 		break;
				} else {
					int oldID = getCraft().getW().getBlockTypeIdAt( oldLoc.getX(), oldLoc.getY(), oldLoc.getZ() );
					// remove water from sinking crafts
	
					updateSet.add( new MapUpdateCommand( oldLoc, newLoc, oldID, getCraft() ) );
					tempBlockList.add(newLoc);
					
				}
	
			}
		}

		if ( !data.failed() ) {
			MovecraftLocation[] newBlockList = (MovecraftLocation[]) tempBlockList.toArray(new MovecraftLocation[0]);
			data.setBlockList( newBlockList );

			//prevents torpedo and rocket pilots :)
			// Move entities within the craft

			Iterator<UUID> i = getCraft().playersRidingShip.iterator();
			while (i.hasNext()) {
				UUID uid = i.next();
				Player pTest = Movecraft.playerIndex.get(uid);
				if(pTest != null){
					if ( MathUtils.playerIsWithinBoundingPolygon( getCraft().getHitBox(), getCraft().getMinX(), getCraft().getMinZ(), MathUtils.bukkit2MovecraftLoc( pTest.getLocation() ) ) ) {
						Location tempLoc = pTest.getLocation(); 
						tempLoc=tempLoc.add( data.getDx(), data.getDy(), data.getDz() );
						Location newPLoc=new Location(getCraft().getW(), tempLoc.getX(), tempLoc.getY(), tempLoc.getZ());
						newPLoc.setPitch(pTest.getLocation().getPitch());
						newPLoc.setYaw(pTest.getLocation().getYaw());
						
						EntityUpdateCommand eUp=new EntityUpdateCommand(pTest.getLocation().clone(),newPLoc,pTest);
						entityUpdateSet.add(eUp);
					}
				}
			}
			
			getCraft().originalPilotLoc = getCraft().originalPilotLoc.add(data.getDx(), data.getDy(), data.getDz());
			
			//Set blocks that are no longer craft to air
			List<MovecraftLocation> airLocation = ListUtils.subtract( Arrays.asList( blocksList ), Arrays.asList( newBlockList ) );

			for ( MovecraftLocation l1 : airLocation ) {
				updateSet.add( new MapUpdateCommand( l1, 0, null) );						
			}

			data.setUpdates(updateSet.toArray( new MapUpdateCommand[1] ) );
			data.setEntityUpdates(entityUpdateSet.toArray( new EntityUpdateCommand[1] ) );
			
			if ( data.getDy() != 0 ) {
				data.setHitbox( BoundingBoxUtils.translateBoundingBoxVertically( data.getHitbox(), data.getDy() ) );
			}

			data.setMinX( data.getMinX() + data.getDx() );
			data.setMinZ( data.getMinZ() + data.getDz() );
			
			// if it should be warping, initiate a warp.
			Craft c = getCraft();
			Player p = c.pilot;
			if(!p.isOnline()) return;
			String s = LocationUtils.locationCheck(p);
			if (s != null) {
				// if(PingUtils.isOnline(s)){
				c.setProcessingTeleport(true);
				RepeatTryServerJumpTask task2 = new RepeatTryServerJumpTask(p, c, s, 0, 205, 0);
				task2.runTaskTimer(Movecraft.getInstance(), 0, 1);
				/*
				 * } else { p.sendMessage(
				 * "This planet's server is offline at the moment, you cannot enter."
				 * ); }
				 */
			}
			// if they aren't in space and are above the warp altitude,
			// initiate planet leaving.
			else if (p.getLocation().getY() > 220) {
				if (!LocationUtils.spaceCheck(p, false)) {
					// if(PingUtils.isOnline(s)){
					p.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + " Leaving the atmosphere!");
					Location loc = LocationUtils.getWarpLocation(p.getWorld().getName());
					c.setProcessingTeleport(true);
					RepeatTryServerJumpTask task2 = new RepeatTryServerJumpTask(p, c, LocationUtils.getSystem(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					task2.runTaskTimer(Movecraft.getInstance(), 0, 1);
					return;
					/*
					 * } else { p.sendMessage(
					 * "This planet's space server is offline at the moment, you cannot enter."
					 * ); }
					 */
				}
			}

			// if they are near a stargate initialize solar system jump
			RepeatTryServerJumpTask task2 = LocationUtils.checkStargateJump(p, c);
			if (task2 != null) {
				c.setProcessingTeleport(true);
				task2.runTaskTimer(Movecraft.getInstance(), 0, 1);
			}
		}
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
					return false;
				}
			}
		}
		return true;
	}

	private void fail( String message ) {
		data.setFailed( true );
		data.setFailMessage( message );
	}

	public TranslationTaskData getData() {
		return data;
	}
}