package net.countercraft.movecraft.async.detection;

import java.io.Serializable;

import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SaveableBlock implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	String world;
	int x, y, z;
	int type;
	
	
	public SaveableBlock(Block block){
		world = block.getWorld().getName();
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		type = block.getTypeId();
	}
	
	public SaveableBlock(String world, int x, int y, int z, int type){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}
	
	public SaveableBlock(World w, MovecraftLocation l) {
		this.world = w.getName();
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
		this.type = w.getBlockTypeIdAt(x,y,z);
	}

	public boolean matches(SaveableBlock other){
		return equals(other);
	}
	
	public boolean matches(Block other){
		return(this.x == other.getX()
			&& this.y == other.getY()
			&& this.z == other.getZ()
			&& this.type == other.getTypeId()
			&& this.world.equals(other.getWorld().getName()));
	}
	
	public boolean matchesTypeData(World w){
		int id = w.getBlockTypeIdAt(x, y, z);
		//door
		if(this.type == 64 || this.type == 71 || this.type == 96){
			return (this.type == id);
		}
		//landing gear extension
		if(this.type == 0 || this.type == 34){
			return(id == 0 || id == 34);
		}
		
		//pistons should be good
		if(this.type == 33){
			return this.type == id;
		}
		
		//also redstone block sponge is interchangable
		if(this.type == 17 || this.type == 152){
			return(id == 152 || id == 17);
		}
		
		return (this.type == id);
	}
	
	public Block getBlockObject(){
		return Bukkit.getWorld(world).getBlockAt(x, y, z);
	}
	
	public Block getBlockObject(World w){
		return w.getBlockAt(x,y,z);
	}
	
	public String getWorld(){
		return world;
	}
	
	public MovecraftLocation toMovecraftLocation(){
		return new MovecraftLocation(x, y, z);
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public int getType() {
		return type;
	}
}
