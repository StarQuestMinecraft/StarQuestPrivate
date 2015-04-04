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
import com.regalphoenixmc.SQRankup.CC3Wrapper;
import com.regalphoenixmc.SQRankup.Database;
import com.regalphoenixmc.SQRankup.SQRankup;
import com.regalphoenixmc.SQRankup.CC3Wrapper.CC3Currency;

public class KillUtils {

	static long MAX_COOLDOWN = 1000 * 60 * 5;

	public static void onBreakShipSign(Sign s, Player breaker) {

		StarshipData d = Movecraft.getInstance().getStarshipDatabase().getStarshipByLocation(s.getLocation());
		if (d != null){
			boolean breakerOwner;
			boolean withinKillTimer;
			if (d.getCaptain().equals(breaker.getUniqueId())) {
				breaker.sendMessage("No kills were credited for this sign break.");
				breakerOwner = true;
				withinKillTimer = true;
			} else {
				breakerOwner = false;
				long lastFlew = Movecraft.getInstance().getStarshipDatabase().getFileLastModified(s.getLocation());
				if (lastFlew < 0){
					withinKillTimer = true;
				} else {	
					long timeGap = System.currentTimeMillis() - lastFlew;
					if (timeGap > MAX_COOLDOWN) {
						breaker.sendMessage("This ship has been parked for more than 5 minutes, so this kill did not count.");
						withinKillTimer = false;
					} else {
						withinKillTimer = true;
					}
				}
				if(withinKillTimer){
					OfflinePlayer pilot = Bukkit.getOfflinePlayer(d.getCaptain());
					boolean success = creditKill(breaker, pilot);
					if (success)
						Bukkit.broadcastMessage(ChatColor.RED + breaker.getName() + " destroyed a ship last flown by " + pilot.getName() + "!");
				}
			}
			CraftSignBreakEvent event = new CraftSignBreakEvent(d, breakerOwner, withinKillTimer, breaker);
			event.call();
		}
	}

	public static boolean creditKill(Player killer, OfflinePlayer killed) {
		// if it's a suicide
		if (killer == killed)
			return false;

		if (killer instanceof Player) {
			boolean cooldown = Database.isInCooldown(killer, killed);
			System.out.println(cooldown);
			if (!cooldown) {
				int infamy = rankToKills(killed);
				CC3Wrapper.deposit(infamy, killer.getName(), CC3Currency.INFAMY, Cause.PLUGIN, "Rankup Kill");
				killer.sendMessage(ChatColor.RED + "You were awarded " + infamy + " infamy for that kill. You now have " + CC3Wrapper.getBalance(killer.getName(), CC3Currency.INFAMY) + " infamy");
				Database.addKill(killer, killed);
				return true;
			} else {
				killer.sendMessage(ChatColor.RED + "You already killed that player in the last 20 minutes! Lay off for a bit...");
				return false;
			}
		}
		return false;
	}

	private static int rankToKills(OfflinePlayer killed) {

		int i = 0;
		String[] groups = SQRankup.permission.getPlayerGroups(null, Bukkit.getOfflinePlayer(killed.getUniqueId()));
		System.out.println(Arrays.toString(groups));
		for (String p : groups) {
			if (SQRankup.infamyGainMap.containsKey(p.toLowerCase())) {
				i = SQRankup.infamyGainMap.get(p.toLowerCase());
			}
		}
		int cost = i < 0 ? i : i * SQRankup.MULTIPLIER;
		return cost;
	}

}
