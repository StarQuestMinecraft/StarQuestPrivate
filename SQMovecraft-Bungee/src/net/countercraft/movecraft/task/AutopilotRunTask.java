package net.countercraft.movecraft.task;

import java.util.ArrayList;
import java.util.Iterator;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.crafttransfer.SerializableLocation;
import net.countercraft.movecraft.utils.LocationUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutopilotRunTask extends BukkitRunnable{
	public static ArrayList<Craft> autopilotingCrafts = new ArrayList<Craft>();
	public static ArrayList<Craft> verticalAutopilotingCrafts = new ArrayList<Craft>();
	final static int SQRT_2 = (int) Math.round(Math.sqrt(2));
	public static boolean DISABLED = false;
	public AutopilotRunTask() {
		runTaskTimer(Movecraft.getInstance(), 40, 40);
		DISABLED = Movecraft.getInstance().getConfig().getBoolean("autopilot");
	}
	
	@Override
	public void run(){
		for (Craft c : autopilotingCrafts){
			if (!c.isProcessing() && !c.isProcessingTeleport() && c.pilot != null && c.pilot.isOnline()){
				Location l = c.pilot.getLocation();
				//if(testShipBlocks(c.getW(), c.getType().getAllowedBlocks(), l, c.vX, c.vZ )){
					c.translate(c.vX, 0, c.vZ);
				/*} else {
					c.pilot.sendMessage("Path is obstructed!");
				}*/
			}
		}
		Iterator<Craft> verticalAutopilotIterator = verticalAutopilotingCrafts.iterator();
		while (verticalAutopilotIterator.hasNext()){
			Craft c = verticalAutopilotIterator.next();
			Location l = c.pilot.getLocation();
			
			if (l.getY() > 220) {
				if (!LocationUtils.spaceCheck(c.pilot, false)) {
					c.pilot.sendMessage(ChatColor.RED + "[ALERT]" + ChatColor.GOLD + " Leaving the atmosphere!");
					stopVerticalAutopiloting(c, c.pilot, c.getSignLocations());
					Location loc = LocationUtils.getWarpLocation(c.pilot.getWorld().getName(), c.pilot.getLocation());
					System.out.println(loc.getX() + " " + loc.getY() + " " + loc.getZ());
					System.out.println(LocationUtils.getSystem());
					SerializableLocation destinationLocation = new SerializableLocation(LocationUtils.getSystem(), loc.getX(), loc.getY(), loc.getZ());
					RepeatTryServerJumpTask.createServerJumpTask(c, destinationLocation);
					continue;
				}
			}
			
			c.translate(0, c.vY, 0);
		}
	}
	
	private boolean testShipBlocks(World w, Integer[] allowedBlocks, Location l, int vX, int vZ) {
		int x = l.getBlockX();
		int z = l.getBlockZ();
		final int y = l.getBlockY();
		
		//get whether they're positive or negative
		int xUnit = sign(vX);
		int zUnit = sign(vZ);
		
		while(x != vX && z != vZ){
			x += xUnit;
			z += zUnit;
		
			int type = l.getWorld().getBlockTypeIdAt(x,y,z);
			if(type == 0){
				l.getWorld().getBlockAt(x,y,z).setType(Material.LAPIS_BLOCK);
				continue; 
			}
			boolean found = false;
			for(Integer i : allowedBlocks){
				if(i == type){
					found = true;
					break;
				}
			}
			if(!found){
				return false;
			}
		}
		return true;
		
		/*Vector oneDist = tv.clone().subtract(pv);
		System.out.println("Dist length: " + oneDist.length());
		oneDist.normalize();
		System.out.println("pv: " + pv);
		System.out.println("tv: " + tv);
		System.out.println("oneDist: " + oneDist);
		Vector loc = pv;
		ArrayList<Block> tested = new ArrayList<Block>();
		while(!vectorEquals(loc, tv)){
			loc.add(oneDist);
			Block b = v2B(w, loc);
			if(TESTING){
				tested.add(b);
			}
			int type = b.getTypeId();
			System.out.println("testing: " + loc + ", type: " + type);
			if(type == 0){
				continue; 
			}
			boolean found = false;
			for(Integer i : allowedBlocks){
				if(i == type){
					found = true;
					break;
				}
			}
			if(!found){
				return false;
			}
		}
		if(TESTING){
			for(Block b : tested){
				if(b.getType() == Material.AIR){
					b.setType(Material.LAPIS_BLOCK);
				}
			}
		}
		return true;*/
	}
	
	private int sign(int num){
		if(num == 0) return 0;
		if(num < 0) return -1;
		else return 1;
	}

	public static void startAutopiloting(Sign clicked, Craft c, Player p){
		if (c.getType().getCanAutopilot()) {
			if(DISABLED){
				p.sendMessage("Autopilot is disabled on this server!");
				return;
			} else {
				int speed = speedFromSign(clicked, c);
				double craftMax = c.getType().getSpeed(p);
				craftMax = craftMax + c.getExtraSpeed();
				if(craftMax < speed){
					int craftSpeed = (int) craftMax;
					speedToSign(clicked, craftSpeed);
					speed = craftSpeed;
				}
				calculateVelocity(c, speed);
				autopilotingCrafts.add(c);
				p.sendMessage(ChatColor.RED + "Autopilot Engaged.");
			}
		} else {
			p.sendMessage(ChatColor.RED + "This ship type cannot autopilot");
		}
		
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
	
	public static void startVerticalAutopilotingUp(Sign clicked, Craft c, Player p){
		if (c.getType().getCanAutopilot()) {
			if(DISABLED){
				p.sendMessage("Autopilot is disabled on this server!");
				return;
			} else {
				double craftMax = c.getType().getSpeed(p);
				craftMax = craftMax + c.getExtraSpeed();
				c.vY = (int) craftMax;
				verticalAutopilotingCrafts.add(c);
				p.sendMessage(ChatColor.RED + "Thrusters Engaged. Going upwards at " + c.vY + " bps.");
			}
		} else {
			p.sendMessage(ChatColor.RED + "This ship type cannot autopilot");
		}
		
	}
	
	public static void startVerticalAutopilotingDown(Sign clicked, Craft c, Player p){
		if (c.getType().getCanAutopilot()) {
			if(DISABLED){
				p.sendMessage("Autopilot is disabled on this server!");
				return;
			} else {
				double craftMax = c.getType().getSpeed(p);
				craftMax = craftMax + c.getExtraSpeed();
				craftMax = -craftMax; //Invert speed so it goes down
				c.vY = (int) craftMax;
				verticalAutopilotingCrafts.add(c);
				int bps = (int) -craftMax; //Invert it again to turn it into a blocks per second number
				p.sendMessage(ChatColor.RED + "Thrusters Engaged. Going downwards at " + bps + " bps.");
			}
		} else {
			p.sendMessage(ChatColor.RED + "This ship type cannot autopilot");
		}
		
	}
	
	public static void stopVerticalAutopiloting(Craft c, Player p, Sign s){
		if (verticalAutopilotingCrafts.contains(c)) verticalAutopilotingCrafts.remove(c);
		p.sendMessage(ChatColor.RED + "Thrusters disabled.");
		s.setLine(1, ChatColor.GREEN + "{DISABLED}");
		s.update();
	}
	
	public static void stopVerticalAutopiloting(Craft c, Player p, ArrayList<MovecraftLocation> signLocations){
		if (verticalAutopilotingCrafts.contains(c)){
			verticalAutopilotingCrafts.remove(c);
			if (p != null){
				p.sendMessage(ChatColor.RED + "Your thrusters have turned off.");
			}
		}
		for(MovecraftLocation l : signLocations){
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
				Sign s = (Sign) b.getState();
				if (s.getLine(0).equals(ChatColor.AQUA + "THRUSTERS")){
					if (s.getLine(1).equals(ChatColor.RED + "{ENGAGED}")){
						s.setLine(1, ChatColor.GREEN + "{DISABLED}");
						s.setLine(2, "");
						s.update();
						break;
					}
				}
		}
	}
	
	private static void calculateVelocity(Craft c, int speed){
		Player p = c.pilot;
		int modif = 2 * (Math.round(speed));
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
		int max = (int) c.getType().getSpeed(c.pilot);
		max = max + c.getExtraSpeed();
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
			int speed = (int) c.getType().getSpeed(c.pilot);
			speed = speed + c.getExtraSpeed();
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
