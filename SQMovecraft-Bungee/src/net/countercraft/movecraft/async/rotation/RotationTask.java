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

package net.countercraft.movecraft.async.rotation;

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
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.EntityUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateCommand;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;

import org.apache.commons.collections.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RotationTask extends AsyncTask {
	
	private ArrayList<MovecraftLocation> signLocations;
    private final MovecraftLocation originPoint;
    private boolean failed = false;
    private String failMessage;
    private MovecraftLocation[] blockList;    // used to be final, not sure why. Changed by Mark / Loraxe42
    private MapUpdateCommand[] updates;
    private EntityUpdateCommand[] entityUpdates;
    private int[][][] hitbox;
    private Integer minX, minZ;
    private final Rotation rotation;
    private final World w;
    private final Vector ZERO = new Vector(0,0,0);

    public RotationTask( Craft c, MovecraftLocation originPoint, MovecraftLocation[] blockList, Rotation rotation, World w) {
            super( c );
            this.originPoint = originPoint;
            this.blockList = blockList;
            this.rotation = rotation;
            this.w = w;
            signLocations = new ArrayList<MovecraftLocation>();
    }

    @Override
    public void excecute() {
            
        // Rotate the block set
        MovecraftLocation[] centeredBlockList = new MovecraftLocation[blockList.length];
        MovecraftLocation[] originalBlockList = blockList.clone();
        HashSet<MovecraftLocation> existingBlockSet = new HashSet<MovecraftLocation>( Arrays.asList( originalBlockList ) );
        Set<MapUpdateCommand> mapUpdates = new HashSet<MapUpdateCommand>();
        HashSet<EntityUpdateCommand> entityUpdateSet = new HashSet<EntityUpdateCommand>();

        // make the centered block list, and check for a cruise control sign to reset to off
        for ( int i = 0; i < blockList.length; i++ ) {
                centeredBlockList[i] = blockList[i].subtract( originPoint );
        }

        for ( int i = 0; i < blockList.length; i++ ) {

                blockList[i] = MathUtils.rotateVec( rotation, centeredBlockList[i] ).add( originPoint );
                //signs
                if ( w.getBlockTypeIdAt( originalBlockList[i].getX(), originalBlockList[i].getY(), originalBlockList[i].getZ() ) == 68){
                	signLocations.add(originalBlockList[i]);
                }
                if ( w.getBlockTypeIdAt( blockList[i].getX(), blockList[i].getY(), blockList[i].getZ() ) != 0 && !existingBlockSet.contains( blockList[i] ) ) {
                        failed = true;
                        failMessage = String.format( I18nSupport.getInternationalisedString( "Rotation - Craft is obstructed" ) );
                        getCraft().setProcessing(true);
                        break;
                } else {
                        int id = w.getBlockTypeIdAt( originalBlockList[i].getX(), originalBlockList[i].getY(), originalBlockList[i].getZ() );
                        mapUpdates.add( new MapUpdateCommand( originalBlockList[i], blockList[i], id, rotation, getCraft()) );
                } 

        }

        if ( !failed ) {
                //rotate entities in the craft
                Location tOP = new Location( getCraft().getW(), originPoint.getX(), originPoint.getY(), originPoint.getZ() );
                Iterator<UUID> i= getCraft().playersRidingShip.iterator();
                while (i.hasNext()) {
                        Player pTest = Movecraft.playerIndex.get(i.next());
                        if (pTest != null && MathUtils.playerIsWithinBoundingPolygon( getCraft().getHitBox(), getCraft().getMinX(), getCraft().getMinZ(), MathUtils.bukkit2MovecraftLoc( pTest.getLocation() ) ) ) {
                                if(pTest.getType()!=org.bukkit.entity.EntityType.DROPPED_ITEM ) {
                                        // Player is onboard this craft
                                        tOP.setX(tOP.getBlockX()+0.5);
                                        tOP.setZ(tOP.getBlockZ()+0.5);
                                        Location playerLoc = pTest.getLocation();
                                        Location adjustedPLoc = playerLoc.subtract( tOP ); 

                                        double[] rotatedCoords = MathUtils.rotateVecNoRound( rotation, adjustedPLoc.getX(), adjustedPLoc.getZ() );
                                        Location rotatedPloc = new Location( getCraft().getW(), rotatedCoords[0], playerLoc.getY(), rotatedCoords[1] );
                                        Location newPLoc = rotatedPloc.add( tOP );

                                        newPLoc.setPitch(playerLoc.getPitch());
                                        float newYaw=playerLoc.getYaw();
                                        if(rotation==Rotation.CLOCKWISE) {
                                                newYaw=newYaw+90.0F;
                                                if(newYaw>=360.0F) {
                                                        newYaw=newYaw-360.0F;
                                                }
                                        }
                                        if(rotation==Rotation.ANTICLOCKWISE) {
                                                newYaw=newYaw-90;
                                                if(newYaw<0.0F) {
                                                        newYaw=newYaw+360.0F;
                                                }
                                        }
                                        newPLoc.setYaw(newYaw);
                                        EntityUpdateCommand eUp=new EntityUpdateCommand(playerLoc, newPLoc, pTest, pTest.getVelocity(), getCraft());
                                        entityUpdateSet.add(eUp);
                                        pTest.setVelocity(ZERO);
                                        pTest.teleport(newPLoc);
                                } else {
                                        pTest.remove();
                                }
                                
                                
                        }

                }
                 //rotate the center point
    			Location adjustedCenter = getCraft().originalPilotLoc.subtract(tOP);
    			double[] rotatedCoords = MathUtils.rotateVecNoRound( rotation, adjustedCenter.getX(), adjustedCenter.getZ() );
    			Location rotatedCenter = new Location( getCraft().getW(), rotatedCoords[0], getCraft().originalPilotLoc.getY(), rotatedCoords[1] );
    			getCraft().originalPilotLoc = rotatedCenter.add( tOP );
                
                // Calculate air changes
                List<MovecraftLocation> airLocation = ListUtils.subtract( Arrays.asList( originalBlockList ), Arrays.asList( blockList ) );
                
                for ( MovecraftLocation l1 : airLocation ) {
                	mapUpdates.add( new MapUpdateCommand( l1, 0, getCraft() ) );
                }
                
    			MapUpdateCommand[] temp = mapUpdates.toArray(new MapUpdateCommand[1]);
    			boolean lastUpdateFound = false;
    			for (int i2 = temp.length - 1; i2 >= 0; i2--){
    				MapUpdateCommand temp2 = temp[i2];
    				if(Arrays.binarySearch(MapUpdateManager.getInstance().fragileBlocks,temp2.getTypeID())>=0){
    					temp2.setLastUpdate(true);
    					lastUpdateFound = true;
    					break;
    				}
    			}
    			if (!lastUpdateFound) temp[temp.length - 1].setLastUpdate(true);
    			
                this.updates = temp;
                
                EntityUpdateCommand[] l2 = entityUpdateSet.toArray( new EntityUpdateCommand[1] );
                this.entityUpdates = l2;

                Integer maxX = null;
                Integer maxZ = null;
                minX = null;
                minZ = null;
                
                int maxY, minY;

                for ( MovecraftLocation l : blockList ) {
                        if ( maxX == null || l.getX() > maxX ) {
                                maxX = l.getX();
                        }
                        if ( maxZ == null || l.getZ() > maxZ ) {
                                maxZ = l.getZ();
                        }
                        if ( minX == null || l.getX() < minX ) {
                                minX = l.getX();
                        }
                        if ( minZ == null || l.getZ() < minZ ) {
                                minZ = l.getZ();
                        }
                }

                // Rerun the polygonal bounding formula for the newly formed craft
                int sizeX, sizeZ;
                sizeX = ( maxX - minX ) + 1;
                sizeZ = ( maxZ - minZ ) + 1;


                int[][][] polygonalBox = new int[sizeX][][];


                for ( MovecraftLocation l : blockList ) {
                        if ( polygonalBox[l.getX() - minX] == null ) {
                                polygonalBox[l.getX() - minX] = new int[sizeZ][];
                        }


                        if ( polygonalBox[l.getX() - minX][l.getZ() - minZ] == null ) {

                                polygonalBox[l.getX() - minX][l.getZ() - minZ] = new int[2];
                                polygonalBox[l.getX() - minX][l.getZ() - minZ][0] = l.getY();
                                polygonalBox[l.getX() - minX][l.getZ() - minZ][1] = l.getY();

                        } else {
                                minY = polygonalBox[l.getX() - minX][l.getZ() - minZ][0];
                                maxY = polygonalBox[l.getX() - minX][l.getZ() - minZ][1];

                                if ( l.getY() < minY ) {
                                        polygonalBox[l.getX() - minX][l.getZ() - minZ][0] = l.getY();
                                }
                                if ( l.getY() > maxY ) {
                                        polygonalBox[l.getX() - minX][l.getZ() - minZ][1] = l.getY();
                                }

                        }


                }

                   this.hitbox = polygonalBox;
            }
        }
        

        public MovecraftLocation getOriginPoint() {
                return originPoint;
        }

        public boolean isFailed() {
                return failed;
        }

        public String getFailMessage() {
                return failMessage;
        }

        public MovecraftLocation[] getBlockList() {
                return blockList;
        }

        public MapUpdateCommand[] getUpdates() {
                return updates;
        }

        public EntityUpdateCommand[] getEntityUpdates() {
                return entityUpdates;
        }

        public int[][][] getHitbox() {
                return hitbox;
        }

        public int getMinX() {
                return minX;
        }

        public int getMinZ() {
                return minZ;
        }

        public Rotation getRotation() {
                return rotation;
        }
        public ArrayList<MovecraftLocation> getSignLocations(){
        	return signLocations;
        }

}