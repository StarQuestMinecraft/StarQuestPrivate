package net.countercraft.movecraft.event;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.Rotation;

public class CraftRotateEvent extends CraftEvent{
	
	Rotation r;
	
	public CraftRotateEvent(Craft c, Rotation rot){
		super(c);
		r = rot;
	}
	
	public Rotation getRotation(){
		return r;
	}
}
