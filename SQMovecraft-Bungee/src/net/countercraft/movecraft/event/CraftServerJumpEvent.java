package net.countercraft.movecraft.event;

import net.countercraft.movecraft.craft.Craft;

public class CraftServerJumpEvent extends CraftEvent{

	String origin;
	String target;
	public CraftServerJumpEvent(Craft c, String origin, String target) {
		super(c);
		this.origin = origin;
		this.target = target;
		// TODO Auto-generated constructor stub
	}
	
	public String getOriginServer(){
		return origin;
	}
	
	public String getTargetServer(){
		return target;
	}

}
