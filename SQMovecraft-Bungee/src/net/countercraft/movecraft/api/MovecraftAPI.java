package net.countercraft.movecraft.api;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.slip.WarpUtils;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

public class MovecraftAPI {
	public static Location getOverworldCoordinatesOfSlipPlayer(Player p){
		if(p.getWorld().getEnvironment() == Environment.NORMAL) return p.getLocation();
		Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
		if(crafts != null){
			for(Craft c : crafts){
				if(c.playersRidingShip.contains(p.getUniqueId())){
					int x = c.warpCoordsX;
					int z = c.warpCoordsZ;
					return new Location(WarpUtils.getNormal(p.getWorld()), x, 100, z);
				}
			}
		}
		return new Location(WarpUtils.getNormal(p.getWorld()), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
	}
}
