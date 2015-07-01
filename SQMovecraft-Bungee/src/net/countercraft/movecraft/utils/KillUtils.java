package net.countercraft.movecraft.utils;

import java.util.Arrays;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.database.FileDatabase;
import net.countercraft.movecraft.database.StarshipData;
import net.countercraft.movecraft.event.CraftSignBreakEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.greatmancode.craftconomy3.Cause;

public class KillUtils {

	static long MAX_COOLDOWN = 1000 * 60 * 5;

	public static boolean onBreakShipSign(Sign s, Player breaker) {

		boolean cancel = false;
		
		StarshipData d = Movecraft.getInstance().getStarshipDatabase().getStarshipByLocation(s.getLocation());
		
		if (d != null){
			long lastFlew = Movecraft.getInstance().getStarshipDatabase().getFileLastModified(s.getLocation());
			long timeGap = System.currentTimeMillis() - lastFlew;
			boolean breakerIsOwner;
			boolean withinKillCooldown;
			if (d.getCaptain().equals(breaker.getUniqueId())) {
				breaker.sendMessage("No kills were credited for this sign break.");
				breakerIsOwner = true;
				withinKillCooldown = true;
				if(timeGap < MAX_COOLDOWN){
					System.out.println("Cancel!");
					cancel = true;
				}
			} else {
				breakerIsOwner = false;
				if (lastFlew < 0){
					withinKillCooldown = false;
				} else {		
					if (timeGap > MAX_COOLDOWN) {
						breaker.sendMessage("This ship has been parked for more than 5 minutes, so this kill did not count.");
						withinKillCooldown = false;
					} else {
						withinKillCooldown = true;
					}
				}
			}
			CraftSignBreakEvent event = new CraftSignBreakEvent(d, breakerIsOwner, withinKillCooldown, breaker);
			event.call();
		}
		
		return cancel;
	}

}
