package net.countercraft.movecraft.event;

import net.countercraft.movecraft.craft.Craft;

public class CraftJoinServerEvent extends CraftSyncDetectEvent {

	public CraftJoinServerEvent(Craft c, String previousServer) {
		super(c);
	}
}
