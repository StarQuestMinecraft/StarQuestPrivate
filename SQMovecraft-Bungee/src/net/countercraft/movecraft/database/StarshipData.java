package net.countercraft.movecraft.database;

import java.util.ArrayList;
import java.util.UUID;

import net.countercraft.movecraft.async.detection.SaveableBlock;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MovecraftLocation;

public class StarshipData {
	SaveableBlock[] blocksList;
	String type;
	UUID captain;
	String shipname;
	ArrayList<UUID> pilots = new ArrayList<UUID>();
	ArrayList<UUID> members = new ArrayList<UUID>();
	
	public StarshipData(SaveableBlock[] lb, String ty, UUID c, String sn, ArrayList<UUID> p, ArrayList<UUID> m){
		blocksList = lb;
		type = ty;
		captain = c;
		shipname = sn;
		pilots = p;
		members = m;
	}
	
	public Craft toCraft(){
		return null;
		//TODO
	}
	
	public static StarshipData fromCraft(Craft c){
		SaveableBlock[] lb = new SaveableBlock[c.getBlockList().length];
		for(int i = 0; i < c.getBlockList().length; i++){
			lb[i] = new SaveableBlock(c.getW(), c.getBlockList()[i]);
		}
		return new StarshipData(lb, c.getType().getCraftName(), c.pilot.getUniqueId(), c.getGivenName(), c.getPilot(), c.getMembers());
	}
	
	public SaveableBlock[] getBlockList(){
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
