package net.countercraft.movecraft.async.translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.event.CraftAsyncTranslateEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.task.AutopilotRunTask;
import net.countercraft.movecraft.task.RepeatTryServerJumpTask;
import net.countercraft.movecraft.utils.BorderUtils;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.CarUtils;
import net.countercraft.movecraft.utils.EntityUpdateCommand;
import net.countercraft.movecraft.utils.FakeBlockUtils;
import net.countercraft.movecraft.utils.LocationUtils;
import net.countercraft.movecraft.utils.MapUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.PlayerFlightUtil;
import net.countercraft.movecraft.utils.StargateJumpHolder;
import net.countercraft.movecraft.vapor.VaporUtils;

import org.apache.commons.collections.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author AJCStriker, Dibujaron
 * The main "heavy lifting" class of movecraft; this task calculates the craft movements.
 * @see TranslationTaskData
 */
public class TranslationTask extends AsyncTask {
	private TranslationTaskData data;
	
	private static final int[] ACCEPTABLE_PLAYER_BLOCKS = new int[]{
		0,
		6,
		30,
		31,
		32,
		37,
		38,
		39,
		40,
		50,
		55,
		59,
		63,
		66,
		68,
		69,
		70,
		71,
		72,
		75,
		76,
		77,
		78,
		83,
		141,
		142,
		143,
	};
	public static final Location STANDARD_SPAWN = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	public TranslationTask(Craft c, TranslationTaskData data) {
		super(c);
		this.data = data;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute() {
		try {
			if(!getCraft().pilot.isOnline()){
				fail("Pilot is not online!");
			}
			//int maxX=getCraft().getMinX()+getCraft().getHitBox().length;
			//int maxZ=getCraft().getMinZ()+getCraft().getHitBox()[0].length;  // safe because if the first x array doesn't have a z array, then it wouldn't be the first x array
			//int minX=getCraft().getMinX();
			//int minZ=getCraft().geftMinZ();
						
			MovecraftLocation[] blocksList = data.getBlockList();

			// canfly=false means an ocean-going vessel
			//boolean waterCraft = !getCraft().getType().canFly();
			//no water craft for now!
			
			/*String sys = getCraft().getW().getName();
			
			if(sys.equals("Regalis") || sys.equals("Defalos") || sys.equals("Digitalia") || sys.equals("AsteroidBelt")){
				waterCraft = false;
			}*/
			
			if (getCraft().getType().isGroundVehicle()) {
				data.setDy(CarUtils.getNewdY(getCraft(), data.getDx(), data.getDz()));
			}

			MovecraftLocation[] newBlockList = new MovecraftLocation[blocksList.length];
			HashSet<MovecraftLocation> existingBlockSet = new HashSet<MovecraftLocation>(Arrays.asList(blocksList));
			HashSet<EntityUpdateCommand> entityUpdateSet = new HashSet<EntityUpdateCommand>();
			Set<MapUpdateCommand> updateSet = new HashSet<MapUpdateCommand>();
			
			if(!checkChunks(getCraft().getW(), getCraft().getMinX(), getCraft().getMinZ(), getCraft().getHitBox(), data.getDx(), data.getDz())){
				fail("You're going a bit fast and the chunks can't render fast enough.");
				data.setChunksFail(true);
			}
			
			
			
			if(!data.failed()){
				Location l = getCraft().pilot.getLocation();
				int nx = l.getBlockX() + data.getDx();
				int nz = l.getBlockZ() + data.getDz();
				if(AutopilotRunTask.autopilotingCrafts.contains(getCraft())){
					if(!BorderUtils.isWithinBorderIncludePadding(nx, nz, 30)){
						fail("You left the autopilot on a bit too long and are almost at the worldborder!");
					} else if(LocationUtils.isBeingJammed(getCraft().getW(), getCraft().getMinX(), getCraft().getMinZ())){
						fail("Your autopilot drive was interrupted by a jamming device! Be careful, you may be under attack!");
					}
				} else {
					if (!BorderUtils.isWithinBorderIncludePadding(nx, nz)) {
						fail("You have almost reached the world border! Turn back now!");
					}
				}
			}
			if(!data.failed()){
				for (int i = 0; i < blocksList.length; i++) {
					MovecraftLocation oldLoc = blocksList[i];
					MovecraftLocation newLoc = oldLoc.translate(data.getDx(), data.getDy(), data.getDz());
					newBlockList[i] = newLoc;
	
					if (newLoc.getY() >= data.getMaxHeight() && newLoc.getY() > oldLoc.getY()) {
						fail(String.format(I18nSupport.getInternationalisedString("Translation - Failed Craft hit height limit")));
						break;
					} else if (newLoc.getY() <= data.getMinHeight() && newLoc.getY() < oldLoc.getY()) {
						fail(String.format(I18nSupport.getInternationalisedString("Translation - Failed Craft hit minimum height limit")));
						break;
					}
					try{
						int testID = getCraft().getW().getBlockTypeIdAt(newLoc.getX(), newLoc.getY(), newLoc.getZ());
						int oldID = getCraft().getW().getBlockTypeIdAt(oldLoc.getX(), oldLoc.getY(), oldLoc.getZ());
						boolean drillable = canDrillBlock(getCraft(), oldID, testID, getCraft().getW(), newLoc);

						//THIS LINE.
						
						if (testID != 0 && testID != 36 && testID != 30 && testID != 31 && testID != 78 && !existingBlockSet.contains(newLoc) && !drillable) {
							// New block is not air and is not part of the existing
							// ship
	
							fail("Craft is obstructed! Blocked by " +  Material.getMaterial(testID) + " at coordinates "
									+ newLoc.getX() + " , " + newLoc.getY() + " , " + newLoc.getZ());
							break;
						}
						
	
						updateSet.add(new MapUpdateCommand(blocksList[i], newBlockList[i], oldID, getCraft(), drillable));
					} catch (Exception e){
						fail("Unexpected exception! We'll try to save your ship...");
					}
				}
			}

			if (!data.failed()) {
				data.setBlockList(newBlockList);

				// Move entities within the craft
				//boolean isAutopiloting = AutopilotRunTask.autopilotingCrafts.contains(getCraft());
				try{
					for(int i = 0; i < getCraft().playersRidingShip.size(); i++) {
						Player pTest = Movecraft.getPlayer(getCraft().playersRidingShip.get(i));
						if(pTest != null){
							if (MathUtils.playerIsWithinBoundingPolygon(getCraft().getHitBox(), getCraft().getMinX(), getCraft().getMinZ(), MathUtils.bukkit2MovecraftLoc(pTest.getLocation()))) {
								Location tempLoc = pTest.getLocation();
								tempLoc=tempLoc.add( data.getDx(), data.getDy(), data.getDz() );
								Location newPLoc=new Location(getCraft().getW(), tempLoc.getX(), tempLoc.getY(), tempLoc.getZ());
								newPLoc.setPitch(pTest.getLocation().getPitch());
								newPLoc.setYaw(pTest.getLocation().getYaw());
								//if(data.getDy() < 0 || isAutopiloting || isStandingInBlock(pTest)){
								if (data.getDoAsyncTeleport())
								{
									FakeBlockUtils.sendFakeBlocks(pTest, newPLoc);
									pTest.teleport(newPLoc);
								}
								//}
								EntityUpdateCommand eUp=new EntityUpdateCommand(pTest.getLocation().clone(),newPLoc,pTest, pTest.getVelocity(), getCraft());
								entityUpdateSet.add(eUp);
								continue;
							}
						}
					}
				} catch (Exception e){
					e.printStackTrace();
				}

				getCraft().originalPilotLoc = getCraft().originalPilotLoc.add(data.getDx(), data.getDy(), data.getDz());
				//getCraft().setPilotSignLocation(getRelativeLocation(getCraft().getPilotSignLocation(), data.getDx(), data.getDy(), data.getDz()));

				// Set blocks that are no longer craft to air
				List<MovecraftLocation> airLocation = ListUtils.subtract(Arrays.asList(blocksList), Arrays.asList(newBlockList));

				data.setAirLocations(airLocation);
				for (MovecraftLocation l1 : airLocation) {
					updateSet.add(new MapUpdateCommand(l1, 0, getCraft(), false));
				}

				MapUpdateCommand[] temp = updateSet.toArray(new MapUpdateCommand[1]);
				boolean lastUpdateFound = false;
				for (int i2 = temp.length - 1; i2 >= 0; i2--) {
					MapUpdateCommand temp2 = temp[i2];
					if (Arrays.binarySearch(MapUpdateManager.getInstance().fragileBlocks, temp2.getTypeID()) >= 0) {
						temp2.setLastUpdate(true);
						lastUpdateFound = true;
						break;
					}
				}
				if (!lastUpdateFound)
					temp[temp.length - 1].setLastUpdate(true);
				data.setUpdates(temp);

				EntityUpdateCommand[] temp2 = entityUpdateSet.toArray(new EntityUpdateCommand[entityUpdateSet.size()]);
				data.setEntityUpdates(temp2);

				if (data.getDy() != 0) {
					data.setHitbox(BoundingBoxUtils.translateBoundingBoxVertically(data.getHitbox(), data.getDy()));
				}

				data.setMinX(data.getMinX() + data.getDx());
				data.setMinZ(data.getMinZ() + data.getDz());

				// if it should be warping, initiate a warp.
				Craft c = getCraft();
				Player p = c.pilot;
				if(!p.isOnline()) return;
				String s = LocationUtils.locationCheck(p);
				if (s != null) {
					// if(PingUtils.isOnline(s)){
					double angle = LocationUtils.getAngleFromGivenPointTo(LocationUtils.locationOfPlanet(s), p.getLocation());
					Location target = LocationUtils.getSpawnLocationFromAngle(angle, STANDARD_SPAWN, 1500);
					c.setProcessingTeleport(true);
					RepeatTryServerJumpTask.createServerJumpTask(p, c, s, target.getBlockX(), 205, target.getBlockY());
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
						Location loc = LocationUtils.getWarpLocation(p.getWorld().getName(), p.getLocation());
						c.setProcessingTeleport(true);
						RepeatTryServerJumpTask.createServerJumpTask(p, c, LocationUtils.getSystem(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
						return;
						/*
						 * } else { p.sendMessage(
						 * "This planet's space server is offline at the moment, you cannot enter."
						 * ); }
						 */
					}
				}

				// if they are near a stargate initialize solar system jump
				StargateJumpHolder jump = LocationUtils.checkStargateJump(p, c);
				if (jump != null) {
					for(UUID u : c.playersRidingShip){
						Player plr = Movecraft.getPlayer(u);
						plr.playSound(plr.getLocation(), Sound.PORTAL_TRAVEL, 2.0F, 1.0F);
					}
					c.setProcessingTeleport(true);
					RepeatTryServerJumpTask.createServerJumpTask(jump.p, jump.c, jump.server, jump.x, jump.y, jump.z);
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private boolean isStandingInBlock(Player pTest) {
		int feet = pTest.getWorld().getBlockTypeIdAt(pTest.getLocation());
		int head = pTest.getWorld().getBlockTypeIdAt(pTest.getEyeLocation());
		if(head != 0 && Arrays.binarySearch(ACCEPTABLE_PLAYER_BLOCKS, head) <= 0){
			return true;
		}
		if(feet != 0 && Arrays.binarySearch(ACCEPTABLE_PLAYER_BLOCKS, feet) <= 0){
			return true;
		}
		return false;
	}

	private void fail(String message) {
		data.setFailed(true);
		data.setFailMessage(message);
	}

	public TranslationTaskData getData() {
		return data;
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
	
	private boolean canDrillBlock(Craft c, int oldID, int newID, World w, MovecraftLocation l){
		try{
			//oldID is the block at the old location, newID is the block at the new location (not updated yet)
			if(c.getType().getDrillHeadID() == oldID && c.getType().getDrilledBlocks().contains(newID)){
				BlockBreakEvent event = new BlockBreakEvent(w.getBlockAt(l.getX(), l.getY(), l.getZ()), c.pilot);
				Bukkit.getServer().getPluginManager().callEvent(event);
				return !event.isCancelled();
			}
			return false;

		} catch (Exception e){
			c.pilot.sendMessage("Whoa there, your drill got stuck for a minute. If you try again it may go through.");
			return false;
		}
	}
}