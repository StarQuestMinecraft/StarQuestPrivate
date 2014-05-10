package net.countercraft.movecraft.utils;

import java.util.ArrayList;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BomberUtils {
	
	final int TICKS_INITIAL_FIREBALL = 20;
	final int TICKS_INTERVAL_SPREADOUT = 20;
	
	//precondition: l is the location one block below the dropper block
	public static void fireCarpet(Player p, Location l){
		Craft c = CraftManager.getInstance().getCraftByPlayer(p);
		if(c != null && c.getType().getCraftName().equalsIgnoreCase("bomber")){
			World w = p.getWorld();
			Fireball f = ((Fireball) w.spawnEntity(l, EntityType.FIREBALL));
			f.setDirection(new Vector(0, -20, 0));
			f.setShooter(p);
			BomberUtils bu = new BomberUtils();
			bu.new BomberDiffuseRunnable(f, p, bu);
			p.sendMessage("Bombs away...");
		}
	}
	
	
	
	class BomberDiffuseRunnable extends BukkitRunnable{
		
		Fireball f;
		Player p;
		BomberUtils bu;
		BomberDiffuseRunnable(Fireball f, Player p, BomberUtils bu){
			this.f = f;
			this.p = p;
			this.bu = bu;
			this.runTaskLater(Movecraft.getInstance(), TICKS_INITIAL_FIREBALL);
		}
		@Override
		public void run() {
			Location l = f.getLocation();
			World w = l.getWorld();
			//w.createExplosion(l, 0F);
			f.remove();
			
			final int LENGTH = 18;
			
			Location start = new Location(w, l.getX() - LENGTH, l.getY(), l.getZ() - LENGTH);
			for(int x = 0; x < 2 * LENGTH; x += 3){
				for(int z = 0; z < 2 * LENGTH; z += 3){
					Location loc = start.clone().add(x, 0, z);
					Fireball f = (Fireball) w.spawnEntity(loc, EntityType.FIREBALL);
					f.setDirection(new Vector(0, -40, 0));
					f.setShooter(p);
					w.createExplosion(loc, 0F);
				}
			}
		}
	}
}
