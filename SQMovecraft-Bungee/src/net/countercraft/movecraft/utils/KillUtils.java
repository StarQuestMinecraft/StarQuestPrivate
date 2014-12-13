package net.countercraft.movecraft.utils;

import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.database.FileDatabase;
import net.countercraft.movecraft.database.StarshipData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.regalphoenixmc.SQRankup.Database;
import com.regalphoenixmc.SQRankup.RankupPlayer;
import com.regalphoenixmc.SQRankup.SQRankup;

public class KillUtils {
	
	static long MAX_COOLDOWN = 1000 * 60 * 5;
	public static void onBreakShipSign(Sign s, Player breaker){
		UUID u = getPlayerLastFlew(s.getLocation());
		if(u == null || u.equals(breaker.getUniqueId())){
			breaker.sendMessage("No kills were credited for this sign break.");
			return;
		} else {
			long lastFlew = FileDatabase.getFileLastModified(s.getLocation());
			if(lastFlew < 0) return;
			long timeGap = System.currentTimeMillis() - lastFlew;
			if(timeGap > MAX_COOLDOWN){
				breaker.sendMessage("This ship has been parked for more than 5 minutes, so this kill did not count.");
				return;
			}
			OfflinePlayer pilot = Bukkit.getOfflinePlayer(u);
			boolean success = creditKill(breaker, pilot);
			if(success) Bukkit.broadcastMessage(ChatColor.RED + breaker.getName() + " destroyed a ship last flown by " + pilot.getName() + "!");
		}
	}
	public static boolean creditKill(Player killer, OfflinePlayer killed) {
		RankupPlayer entry;
		if (!Database.hasKey(killer.getName())) {
			entry = new RankupPlayer(killer.getName(), 0L, "", 0);
			entry.saveNew();
		} else {
			entry = Database.getEntry(killer.getName());
			String lastKill = entry.getLastKillName();
			if ((lastKill != null) && (lastKill.equals(killed.getName()) && (System.currentTimeMillis() - entry.getLastKillTime() < 1800000))) {
				killer.sendMessage("This kill did not count because you killed that player within the last half hour.");
				return false;
			}
		}
		int kills = rankToKills(killed.getName());
		entry.setAsyncKills(kills);
		entry.setLastKillName(killed.getName());
		entry.setLastKillTime(System.currentTimeMillis());
		entry.saveData();
		return true;
	}
	
	private static UUID getPlayerLastFlew(Location sign){
		StarshipData d = Movecraft.getInstance().getStarshipDatabase().getStarshipByLocation(sign);
		if(d == null) return null;
		return d.getCaptain();
	}

	private static int rankToKills(String name) {

		int i = 1;
		
		OfflinePlayer plr = Bukkit.getOfflinePlayer(name);
		if(plr == null) return 1;
		String[] groups = SQRankup.permission.getPlayerGroups(null, plr);
		for (String p : groups) {
			switch (p.toLowerCase()) {
			case "refugee":
				i = -3;
				break;
			case "settler":
				i = 1;
				break;
			case "colonist":
			case "pirate":
				i = 2;
				break;
			case "corsair":
			case "citizen":
				i = 3;
				break;
			case "buccaneer":
			case "affluent":
				i = 4;
				break;
			case "warlord":
			case "tycoon":
				i = 4;
				break;
			default:
				break;

			}
		}

		return i;
	}
}
