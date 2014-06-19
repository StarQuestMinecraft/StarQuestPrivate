package net.countercraft.movecraft.utils.map;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;

/**
 * Class that stores the data about a single blocks changes to the map in an unspecified world. The world is retrieved contextually from the submitting craft.
 */
public class MapUpdateCommand {
	private MovecraftLocation blockLocation;
	private final MovecraftLocation newBlockLocation;
	private final int typeID;
	private final Rotation rotation;
	private Craft craft;
	private int smoke;

	public MapUpdateCommand( MovecraftLocation blockLocation, MovecraftLocation newBlockLocation, int typeID, Rotation rotation, Craft craft ) {
		this.blockLocation = blockLocation;
		this.newBlockLocation = newBlockLocation;
		this.typeID = typeID;
		this.rotation = rotation;
		this.craft = craft;
		this.smoke = 0;
	}

	public MapUpdateCommand( MovecraftLocation blockLocation, MovecraftLocation newBlockLocation, int typeID, Craft craft ) {
		this.blockLocation = blockLocation;
		this.newBlockLocation = newBlockLocation;
		this.typeID = typeID;
		this.rotation = Rotation.NONE;
		this.craft = craft;
		this.smoke = 0;
	}

	public MapUpdateCommand( MovecraftLocation newBlockLocation, int typeID, Craft craft ) {
		this.newBlockLocation = newBlockLocation;
		this.typeID = typeID;
		this.rotation = Rotation.NONE;
		this.craft = craft;
		this.smoke = 0;
	}

	public MapUpdateCommand( MovecraftLocation newBlockLocation, int typeID, Craft craft, int smoke ) {
		this.newBlockLocation = newBlockLocation;
		this.typeID = typeID;
		this.rotation = Rotation.NONE;
		this.craft = craft;
		this.smoke = smoke;
	}

	public int getTypeID() {
		return typeID;
	}

	public int getSmoke() {
		return smoke;
	}

	public MovecraftLocation getOldBlockLocation() {
		return blockLocation;
	}

	public MovecraftLocation getNewBlockLocation() {
		return newBlockLocation;
	}

	public Rotation getRotation() {
		return rotation;
	}

	public Craft getCraft() {
		return craft;
	}
}
