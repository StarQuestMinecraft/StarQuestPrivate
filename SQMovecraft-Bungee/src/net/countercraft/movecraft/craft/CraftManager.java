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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.database.StarshipData;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.task.AutopilotRunTask;
import net.countercraft.movecraft.utils.JammerUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.ShieldUtils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CraftManager {
	private static final CraftManager ourInstance = new CraftManager();
	private CraftType[] craftTypes;
	private final Map<World, Set<Craft>> craftList = new ConcurrentHashMap<World, Set<Craft>>();
	private final HashMap<UUID, Craft> craftPlayerIndex = new HashMap<UUID, Craft>();
	
	private static boolean SAVE_CRAFTS = true;

	public static CraftManager getInstance() {
		return ourInstance;
	}

	private CraftManager() {
		initCraftTypes();
	}

	public CraftType[] getCraftTypes() {
		return craftTypes;
	}

	void initCraftTypes() {
		File craftsFile = new File( Movecraft.getInstance().getDataFolder().getAbsolutePath() + "/types" );

		if ( !craftsFile.exists() ) {
			craftsFile.mkdirs();
		}

		HashSet<CraftType> craftTypesSet = new HashSet<CraftType>();

		for ( File file : craftsFile.listFiles() ) {
			if ( file.isFile() ) {

				if ( file.getName().contains( ".craft" ) ) {
					CraftType type = new CraftType( file );
					craftTypesSet.add( type );
				}
			}
		}

		craftTypes = craftTypesSet.toArray( new CraftType[1] );
		Movecraft.getInstance().getLogger().log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Startup - Number of craft files loaded" ), craftTypes.length ) );
	}

	public void addCraft( Craft c, Player p ) {
		Set<Craft> crafts = craftList.get( c.getW() );
		if ( crafts == null ) {
			craftList.put( c.getW(), new HashSet<Craft>() );
		}
		craftList.get( c.getW() ).add( c );
		craftPlayerIndex.put( p.getUniqueId(), c );
	}
	
	public void removeCraft( final Craft c){
		removeCraft(c, true);
	}

	public void removeCraft( final Craft c, final boolean save) {
		c.extendLandingGear();
		Player p = c.pilot;
		
		if(c.getMoveTaskId() != -1){
			Bukkit.getScheduler().cancelTask(c.getMoveTaskId());
			c.setMoveTaskId(-1);
		}
		ArrayList<MovecraftLocation> signLocations = c.getSignLocations();
		JammerUtils.disableJammer(c, signLocations);
		ShieldUtils.enableShield(c, signLocations);
		try{
			if(!MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))){
				p.teleport(c.originalPilotLoc);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		craftList.get( c.getW() ).remove( c );
		AutopilotRunTask.stopAutopiloting(c, p, signLocations);
		craftPlayerIndex.remove( p.getUniqueId() );
		if (p.isOnline()) {
			p.sendMessage( String.format( I18nSupport.getInternationalisedString( "Release - Craft has been released message" ) ) );
			Movecraft.getInstance().getLogger().log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Release - Player has released a craft console" ), p.getName(), c.getType().getCraftName(), c.getBlockList().length, c.getMinX(), c.getMinZ() ) );
			//process and update bedspawns
		}
		c.messageShipPlayers("The ship has been released, you are no longer riding on it.");
		updateBedspawns(c);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				if(SAVE_CRAFTS && save){
					Movecraft.getInstance().getStarshipDatabase().saveStarshipAtLocation(c);
				}
			}
		},4L);
	}
	
	public void updateBedspawns(Craft c){
		for (String s : c.playersWithBedspawnsOnShip){
			Bedspawn b = Bedspawn.getBedspawn(s);
			b.x = b.x + c.xDist;
			b.y = b.y + c.yDist;
			b.z = b.z + c.zDist;
			Bedspawn.saveBedspawn(b);
		}
	}

	public Craft[] getCraftsInWorld( World w ) {
		Set<Craft> crafts = craftList.get( w );
		if ( crafts == null || crafts.isEmpty() ) {
			return null;
		} else {
			return craftList.get( w ).toArray( new Craft[1] );
		}
	}

	public Craft getCraftByPlayer( Player p ) {
		return craftPlayerIndex.get( p.getUniqueId() );
	}

	public Player getPlayerFromCraft( Craft c ) {
		for ( Map.Entry<UUID, Craft> playerCraftEntry : craftPlayerIndex.entrySet() ) {

			if ( playerCraftEntry.getValue() == c ) {
				return Movecraft.getPlayer(playerCraftEntry.getKey());
			}

		}

		return null;
	}
	
	public void releaseAllCrafts(){
		for(World w  : craftList.keySet()){
			Craft[] cset = (Craft[]) craftList.get(w).toArray();
			for(int i = 0; i < cset.length; i++){
				Craft c = cset[i];
				removeCraft(c);
			}
		}
	}
}
