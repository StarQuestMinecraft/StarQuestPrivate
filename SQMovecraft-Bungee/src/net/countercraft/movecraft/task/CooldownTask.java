package net.countercraft.movecraft.task;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;

public class CooldownTask extends BukkitRunnable {

	public CooldownTask(){
		
	}
	
	@Override
	public void run(){
		
		for (World world : Bukkit.getWorlds()) {
			
			if (CraftManager.getInstance().getCraftsInWorld(world) != null) {
				
				for (Craft craft : CraftManager.getInstance().getCraftsInWorld(world)) {
					
					if (craft.cannonCooldown > 0) {
						
						craft.cannonCooldown --;
						
					}
					
				}
				
			}
			
		}
		
	}
	
}
