package net.countercraft.movecraft.database;

import java.util.UUID;

import net.countercraft.movecraft.async.detection.SaveableBlock;
import net.countercraft.movecraft.craft.Craft;

public class StarshipData {
	SaveableBlock[] blocksList;
	String type;
	UUID captain;
	
	public StarshipData(SaveableBlock[] lb, String ty, UUID c){
		blocksList = lb;
		type = ty;
		captain = c;
	}
	
	public static StarshipData fromCraft(Craft c){
		SaveableBlock[] lb = new SaveableBlock[c.getBlockList().length];
		for(int i = 0; i < c.getBlockList().length; i++){
			lb[i] = new SaveableBlock(c.getW(), c.getBlockList()[i]);
		}
		return new StarshipData(lb, c.getType().getCraftName(), c.pilot.getUniqueId());
	}
	
	public SaveableBlock[] getBlockList(){
		return blocksList;
	}
	
	public String getType(){
		return type;
	}
	
	public UUID getCaptain(){
		return captain;
	}
}
