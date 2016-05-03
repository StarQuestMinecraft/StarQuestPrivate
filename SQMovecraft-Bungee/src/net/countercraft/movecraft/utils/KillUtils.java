package net.countercraft.movecraft.utils;


import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.database.StarshipData;
import net.countercraft.movecraft.event.CraftSignBreakEvent;
import net.countercraft.movecraft.shield.Compression;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class KillUtils {

	static long MAX_COOLDOWN = 1000 * 60 * 5;

	public static boolean onBreakShipSign(Sign s, Player breaker) {

		/*boolean cancel = false;
		
		StarshipData d = Movecraft.getInstance().getStarshipDatabase().getStarshipByLocation(s.getLocation());
		
		System.out.print(d);
		
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
		else {
			if (!breaker.getUniqueId().equals(Compression.str15ToUuid(s.getLine(3).substring(2)))) {
				cancel = false;
				CraftSignBreakEvent event = new CraftSignBreakEvent(EMPUtils.dataMap.get(Compression.str15ToUuid(s.getLine(3).substring(2))), false, true, breaker);
				event.call();
			}
			else {
				breaker.sendMessage("No kills were credited for this sign break.");
				cancel = true;
			}
		}*/
		
		return false;
	}

}
