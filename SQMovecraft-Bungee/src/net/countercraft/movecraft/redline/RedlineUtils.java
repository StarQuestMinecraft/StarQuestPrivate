package net.countercraft.movecraft.redline;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.World;

public class RedlineUtils {
	
	public static void executeJump(Craft c, RedlineJump r){
		c.translate(r.dx, r.dy, r.dz);
	}
}
