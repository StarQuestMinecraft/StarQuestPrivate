package net.countercraft.movecraft.async.detection;

import java.io.Serializable;

import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SaveableBlock implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	String world;
	int x, y, z;
	int type;
	byte data;
	
	
	public SaveableBlock(Block block){
		world = block.getWorld().getName();
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		type = block.getTypeId();
		data = block.getData();
	}
	
	public SaveableBlock(String world, int x, int y, int z, int type, byte data){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.data = data;
	}
	
	public SaveableBlock(World w, MovecraftLocation l) {
		this.world = w.getName();
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
		Block b = w.getBlockAt(x,y,z);
		this.type = b.getTypeId();
		this.data = b.getData();
	}

	public boolean matches(SaveableBlock other){
		return equals(other);
	}
	
	public boolean matches(Block other){
		return(this.x == other.getX()
			&& this.y == other.getY()
			&& this.z == other.getZ()
			&& this.type == other.getTypeId()
			&& this.data == other.getData()
			&& this.world.equals(other.getWorld().getName()));
	}
	
	public boolean matchesTypeData(Block other){
		return (this.type == other.getTypeId() && this.data == other.getData());
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
	
	public byte getData() {
		return data;
	}
}
