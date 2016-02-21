package net.countercraft.movecraft.bungee;

import org.bukkit.inventory.Inventory;

public class LocAndBlock {
	public int X;
	public int Y;
	public int Z;
	public int id;
	public int data;
	public Inventory i;
	public String line1 = null;
	public String line2 = null;
	public String line3 = null;
	public String line4 = null;
	
	public LocAndBlock(int x, int y, int z, int id, int d){
		X = x;
		Y = y;
		Z = z;
		this.id = id;
		data = d;
	}
	public LocAndBlock(int x, int y, int z, int id, int d, String line1, String line2, String line3, String line4){
		X = x;
		Y = y;
		Z = z;
		this.id = id;
		data = d;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.line4 = line4;
	}
	public LocAndBlock(int x, int y, int z, int id , int d, Inventory i){
		X = x;
		Y = y;
		Z = z;
		this.id = id;
		data = d;
		this.i = i;
	}
}
