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
	byte data;
	public String line0 = null;
	public String line1 = null;
	public String line2 = null;
	public String line3 = null;
	
	
	public SaveableBlock(Block block){
		world = block.getWorld().getName();
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		type = block.getTypeId();
		data = block.getData();
		if (type == 63 || type == 68) {
			Sign s = (Sign) block.getState();
			line0 = s.getLine(0);
			line1 = s.getLine(1);
			line2 = s.getLine(2);
			line3 = s.getLine(3);
		}
	}
	
	public SaveableBlock(String world, int x, int y, int z, int type, byte data){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.data = data;
	}
	
	public SaveableBlock(String world, int x, int y, int z, int type, byte data, String line0, String line1, String line2, String line3){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.data = data;
		this.line0 = line0;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
	}
	
	public SaveableBlock(World w, MovecraftLocation l) {
		this.world = w.getName();
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
		Block b = w.getBlockAt(x,y,z);
		this.type = b.getTypeId();
		this.data = b.getData();
		if (type == 63 || type == 68) {
			Sign s = (Sign) b.getState();
			line0 = s.getLine(0);
			line1 = s.getLine(1);
			line2 = s.getLine(2);
			line3 = s.getLine(3);
		}
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
		//door
		if(this.type == 64 || this.type == 71 || this.type == 96){
			return (this.type == other.getTypeId());
		}
		//landing gear extension
		if(this.type == 0 || this.type == 34){
			return(other.getTypeId() == 0 || other.getTypeId() == 34);
		}
		
		//pistons should be good
		if(this.type == 33){
			return this.type == other.getTypeId();
		}
		
		//also redstone block sponge is interchangable
		if(this.type == 17 || this.type == 152){
			return(other.getTypeId() == 152 || other.getTypeId() == 17);
		}
		
		//make sure doors signs match
		if (type == 63 || type == 68) {
			if(this.type == other.getTypeId() && this.data == other.getData()){
				if(line0 == null || line1 == null || line2 == null || line3 == null) return false;
				System.out.println(line0);
				System.out.println(line1);
				System.out.println(line2);
				System.out.println(line3);
				System.out.println("");
				
				
				Sign s = (Sign) other.getState();
				return (line0.equals(s.getLine(0))
						&& line1.equals(s.getLine(1))
						&& line2.equals(s.getLine(2))
						&& line3.equals(s.getLine(3)));
			}

		}
		
		
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
