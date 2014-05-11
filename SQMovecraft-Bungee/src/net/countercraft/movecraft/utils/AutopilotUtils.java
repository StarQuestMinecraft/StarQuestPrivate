package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;


public class AutopilotUtils {
	final static int SQRT_2 = (int) Math.round(Math.sqrt(2));

	public static void startAutopiloting(Craft c, Player p){
		calculateVelocity(c);
		c.isAutopiloting = true;
		p.sendMessage(ChatColor.RED + "Autopilot Engaged.");
	}
	public static void stopAutopiloting(Craft c, Player p){
		c.isAutopiloting = false;
		if (p != null){
			p.sendMessage(ChatColor.RED + "Your autopilot has turned off.");
		}
		for(MovecraftLocation l : c.getBlockList()){
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
			if (b.getType() == Material.WALL_SIGN){
				Sign s = (Sign) b.getState();
				if (s.getLine(0).equals(ChatColor.BLUE + "AUTOPILOT")){
					if (s.getLine(1).equals(ChatColor.RED + "{ENGAGED}")){
						s.setLine(1, ChatColor.GREEN + "{DISABLED}");
						s.setLine(2, "");
						s.update();
						break;
					}
				}
			}
		}
	}
	public static void stopAutopiloting(Craft c, Player p, Sign s){
		c.isAutopiloting = false;
		p.sendMessage(ChatColor.RED + "Autopilot disabled.");
		s.setLine(1, ChatColor.GREEN + "{DISABLED}");
		s.update();
	}

	private static void calculateVelocity(Craft c){
		Player p = c.pilot;
		CraftType type = c.getType();
		int modif = 2 * ((int) Math.round(type.getSpeed()));
		int modifroot = Math.round(modif / SQRT_2);
		
        float deg = p.getLocation().getYaw() + 180;
        if (deg >= 360) deg -= 360;
        if (deg < 0) deg += 360;
        
        int degrees = Math.round(deg);
        
        if (degrees <= 22){ c.vX = 0; c.vZ = -modif;} // north part one
        else if (degrees <= 67){ c.vX = modifroot; c.vZ = -modifroot;} //return "Northeast";
        else if (degrees <= 112){ c.vX = modif; c.vZ = 0;} //return "East";
        else if (degrees <= 157){ c.vX = modifroot; c.vZ = modifroot;} //return "Southeast";
        else if (degrees <= 202){ c.vX = 0; c.vZ = modif;} //return "South";
        else if (degrees <= 247){ c.vX = -modifroot; c.vZ = modifroot;} //return "Southwest";
        else if (degrees <= 292){ c.vX = -modif; c.vZ = 0;} //return "West";
        else if (degrees <= 337){ c.vX = -modifroot; c.vZ = -modifroot;} //return "Northwest";
        else if (degrees <= 359){ c.vX = 0; c.vZ = -modif;} //return "North";
        else {c.vX = 0; c.vZ = 0;}
       
	}
}
