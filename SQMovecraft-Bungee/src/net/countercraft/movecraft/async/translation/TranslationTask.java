package net.countercraft.movecraft.async.translation;

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
import net.countercraft.movecraft.utils.LocationUtils;
import net.countercraft.movecraft.utils.MapUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

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

public class TranslationTask extends AsyncTask {
	private TranslationTaskData data;
	
	private static final Location STANDARD_SPAWN = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	public TranslationTask(Craft c, TranslationTaskData data) {
		super(c);
		this.data = data;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void excecute() {
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

			CraftAsyncTranslateEvent event = new CraftAsyncTranslateEvent(getCraft(), data);
			if (!event.call()) {
				fail(event.getMessage());
			}
			
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
						if (testID != 0 && testID != 36 && !existingBlockSet.contains(newLoc) && !drillable) {
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
				
				try{
					for(int i = 0; i < getCraft().playersRidingShip.size(); i++) {
						UUID uid = getCraft().playersRidingShip.get(i);
						Player pTest = Movecraft.getPlayer(uid);
						if(pTest != null){
							if (MathUtils.playerIsWithinBoundingPolygon(getCraft().getHitBox(), getCraft().getMinX(), getCraft().getMinZ(), MathUtils.bukkit2MovecraftLoc(pTest.getLocation()))) {
								Location tempLoc = pTest.getLocation();
								tempLoc=tempLoc.add( data.getDx(), data.getDy(), data.getDz() );
								Location newPLoc=new Location(getCraft().getW(), tempLoc.getX(), tempLoc.getY(), tempLoc.getZ());
								newPLoc.setPitch(pTest.getLocation().getPitch());
								newPLoc.setYaw(pTest.getLocation().getYaw());
								pTest.teleport(newPLoc);
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
					RepeatTryServerJumpTask task2 = new RepeatTryServerJumpTask(p, c, s, target.getBlockX(), 205, target.getBlockY());
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
						Location loc = LocationUtils.getWarpLocation(p.getWorld().getName(), p.getLocation());
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
					for(UUID u : c.playersRidingShip){
						Player plr = Movecraft.playerIndex.get(u);
						plr.playSound(plr.getLocation(), Sound.PORTAL_TRAVEL, 2.0F, 1.0F);
					}
					c.setProcessingTeleport(true);
					task2.runTaskTimer(Movecraft.getInstance(), 0, 1);
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return;
		}
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
	
	private boolean canDrillBlock(Craft c,int oldID, int newID, World w, MovecraftLocation l){
		try{
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