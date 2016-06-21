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

package net.countercraft.movecraft.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;

/**
 * Class that stores the data about a single blocks changes to the map in an unspecified world. The world is retrieved contextually from the submitting craft.
 */
public class MapUpdateCommand {
	private MovecraftLocation blockLocation;
	private final MovecraftLocation newBlockLocation;
	private final int newTypeID;
	private final Rotation rotation;
	private final Craft myCraft;
	private boolean isLastUpdate;
	private boolean drill;
	private boolean finalBlockFragile;
	private boolean initialBlockFragile;
	Block block;
	private net.minecraft.server.v1_10_R1.Chunk chunk = null;
	
	public List<String> oldMetadataName = new ArrayList<String>();
	public List<MetadataValue> oldMetadataValue = new ArrayList<MetadataValue>();

	public MapUpdateCommand( MovecraftLocation blockLocation, MovecraftLocation newBlockLocation, int newTypeID, Rotation rotation, Craft c) {
		this.blockLocation = blockLocation;
		this.newBlockLocation = newBlockLocation;
		this.newTypeID = newTypeID;
		this.rotation = rotation;
		this.myCraft = c;
		this.isLastUpdate = false;		

        Block block = c.getW().getBlockAt(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
        for (String metadata : Movecraft.blockMetadataTransfer) {
        	if (block.hasMetadata(metadata)) { 
            	oldMetadataValue.add(block.getMetadata(metadata).get(0));
            	oldMetadataName.add(metadata);
        	}
        }
	}

	public MapUpdateCommand( MovecraftLocation blockLocation, MovecraftLocation newBlockLocation, int newTypeID, Craft c, boolean drill) {
		this.blockLocation = blockLocation;
		this.newBlockLocation = newBlockLocation;
		this.newTypeID = newTypeID;
		this.rotation = Rotation.NONE;
		this.myCraft = c;
		this.isLastUpdate = false;
		this.drill = drill;	
		
        Block block = c.getW().getBlockAt(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
        for (String metadata : Movecraft.blockMetadataTransfer) {
        	if (block.hasMetadata(metadata)) { 
            	oldMetadataValue.add(block.getMetadata(metadata).get(0));
            	oldMetadataName.add(metadata);
        	}
        }
	}

	public MapUpdateCommand( MovecraftLocation newBlockLocation, int typeID, Craft c, boolean drill) {
		this.newBlockLocation = newBlockLocation;
		this.newTypeID = typeID;
		this.rotation = Rotation.NONE;
		this.myCraft = c;
		this.isLastUpdate = false;
		this.drill = drill;
	}
	
	public boolean isLastUpdate(){
		return isLastUpdate;
	}
	
	public void setLastUpdate(boolean isLastUpdate){
		this.isLastUpdate = isLastUpdate;
	}
	
	public Craft getCraft(){
		return myCraft;
	}
	
	public int getTypeID() {
		return newTypeID;
	}

	public MovecraftLocation getOldBlockLocation() {
		return blockLocation;
	}

	public MovecraftLocation getNewBlockLocation() {
		return newBlockLocation;
	}

	public Rotation getRotation() {
		return rotation;
	}
	
	public boolean shouldDrill(){
		return drill;
	}
	
	public boolean isFinalBlockFragile(){
		return finalBlockFragile;
	}
	
	public void setFinalBlockFragile(boolean fragile){
		finalBlockFragile = fragile;
	}
	
	public boolean isInitialBlockFragile(){
		return initialBlockFragile;
	}
	
	public void setInitialBlockFragile(boolean fragile){
		initialBlockFragile = fragile;
	}
	
	public void setChunk(net.minecraft.server.v1_10_R1.Chunk chunk){
		this.chunk = chunk;
	}
	public net.minecraft.server.v1_10_R1.Chunk getChunk(){
		return chunk;
	}
	public Block getBlock(){
		return block;
	}
	public void setBlock(Block b){
		block = b;
	}
}
