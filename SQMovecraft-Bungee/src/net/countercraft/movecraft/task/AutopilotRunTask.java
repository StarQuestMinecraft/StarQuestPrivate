package net.countercraft.movecraft.task;

import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutopilotRunTask extends BukkitRunnable{
	public static ArrayList<Craft> autopilotingCrafts = new ArrayList<Craft>();
	final static int SQRT_2 = (int) Math.round(Math.sqrt(2));
	
	public AutopilotRunTask() {
		
		runTaskTimer(Movecraft.getInstance(), 40, 40);
		
	}
	
	public void run(){
		for (Craft c : autopilotingCrafts){
			if (!c.isProcessing() && !c.isProcessingTeleport() && c.pilot != null && c.pilot.isOnline()){
				c.translate(c.vX, 0, c.vZ);
			}
		}
	}
	public static void startAutopiloting(Sign clicked, Craft c, Player p){
		int speed = speedFromSign(clicked, c);
		double craftMax = c.getType().getSpeed();
		if(craftMax < speed){
			int craftSpeed = (int) craftMax;
			speedToSign(clicked, craftSpeed);
			speed = craftSpeed;
		}
		calculateVelocity(c, speed);
		autopilotingCrafts.add(c);
		p.sendMessage(ChatColor.RED + "Autopilot Engaged.");
	}
	public static void stopAutopiloting(Craft c, Player p, ArrayList<MovecraftLocation> signLocations){
		if (autopilotingCrafts.contains(c)){
			autopilotingCrafts.remove(c);
			if (p != null){
				p.sendMessage(ChatColor.RED + "Your autopilot has turned off.");
			}
		}
		for(MovecraftLocation l : signLocations){
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
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
	public static void stopAutopiloting(Craft c, Player p){
		if (autopilotingCrafts.contains(c)){
			autopilotingCrafts.remove(c);
			if (p != null){
				p.sendMessage(ChatColor.RED + "Your autopilot has turned off.");
			}
		}
		for(MovecraftLocation l : c.getBlockList()){
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
				if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
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
		if (autopilotingCrafts.contains(c)) autopilotingCrafts.remove(c);
		p.sendMessage(ChatColor.RED + "Autopilot disabled.");
		s.setLine(1, ChatColor.GREEN + "{DISABLED}");
		s.update();
	}

	private static void calculateVelocity(Craft c, int speed){
		Player p = c.pilot;
		int modif = 2 * ((int) Math.round(speed));
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
	
	public static void incrementSpeed(Sign s, Craft c){
		int speed = speedFromSign(s, c);
		int max = (int) c.getType().getSpeed();
		if(speed < max){
			speed++;
		} else {
			speed = 0;
		}
		speedToSign(s, speed);
	}
	
	public static void incrementSpeed(Sign s, Player p){
		Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if(c == null){
			p.sendMessage("You must be flying the ship to modify the autopilot.");
			return;
		} else {
			incrementSpeed(s, c);
		}
	}
	
	/*private static int speedFromSign(Sign s){
		String l3 = s.getLine(2);
		if(l3 == null || l3 == ""){
			s.setLine(2, "5 b/s");
			s.update();
			return 5;
		} else {
			String speed = l3.substring(0, 1);
			return Integer.parseInt(speed);
		}
	}*/
	
	private static int speedFromSign(Sign s, Craft c){
		String line = s.getLine(2);
		if(line == null || line.equals("")){
			int speed = (int) c.getType().getSpeed();
			speedToSign(s, speed);
			return speed;
		}
		String num = line.split(" ")[1];
		return Integer.parseInt(num);
	}
	
	private static void speedToSign(Sign s, int speed){
		String word = (ChatColor.LIGHT_PURPLE + " " + speed + " b/s");
		s.setLine(2, word);
		s.update();
	}
}
