package net.countercraft.movecraft.database;

import java.util.ArrayList;
import java.util.UUID;

import net.countercraft.movecraft.bungee.LocAndBlock;

public class StarshipData {
	LocAndBlock[] blocksList;
	String type;
	UUID captain;
	String shipname;
	ArrayList<UUID> pilots = new ArrayList<UUID>();
	ArrayList<UUID> members = new ArrayList<UUID>();
	
	public StarshipData(LocAndBlock[] lb, String ty, UUID c, String sn, ArrayList<UUID> p, ArrayList<UUID> m){
		blocksList = lb;
		type = ty;
		captain = c;
		shipname = sn;
		pilots = p;
		members = m;
	}
	public LocAndBlock[] getLBBA(){
		return blocksList;
	}
	
	public String getName(){
		return shipname;
	}
	
	public String getType(){
		return type;
	}
	
	public ArrayList<UUID> getPilots(){
		return pilots;
	}
	
	public ArrayList<UUID> getMembers(){
		return members;
	}
	
	public UUID getCaptain(){
		return captain;
	}
}
