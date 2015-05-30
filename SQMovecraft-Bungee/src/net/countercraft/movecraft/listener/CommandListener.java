/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.shield.DockUtils;
import net.countercraft.movecraft.shield.ShieldUtils;
import net.countercraft.movecraft.slip.WarpUtils;
import net.countercraft.movecraft.utils.MathUtils;

import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CommandListener implements Listener {

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {

		String newMessage = e.getMessage().toLowerCase();
		if (newMessage.equals("/release")) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer(e.getPlayer());

			if (pCraft != null) {
				CraftManager.getInstance().removeCraft(pCraft);
				e.getPlayer().sendMessage(String.format(I18nSupport.getInternationalisedString("Player- Craft has been released")));
			} else {
				e.getPlayer().sendMessage(String.format(I18nSupport.getInternationalisedString("Player- Error - You do not have a craft to release!")));
			}

			e.setCancelled(true);
		}

		else if (newMessage.equals("/warpstart")) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer(e.getPlayer());

			if (pCraft != null) {
				WarpUtils.enterWarp(e.getPlayer(), pCraft);
			}

			e.setCancelled(true);
		}

		else if (newMessage.equals("/warpstop")) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer(e.getPlayer());

			if (pCraft != null) {
				WarpUtils.leaveWarp(e.getPlayer(), pCraft, true);
			}

			e.setCancelled(true);
		}

		else if (newMessage.equals("/ride")) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			if (p.getWorld().getEnvironment() == Environment.THE_END) {
				Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
				if (crafts != null) {
					for (Craft c : crafts) {
						if (MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))) {
							try {
								c.playersRidingLock.acquire();
								c.playersRidingShip.add(p.getUniqueId());
								c.playersRidingLock.release();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							p.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
							return;
						}
					}
				}
				p.sendMessage("No craft found at your location for you to ride.");
			} else {
				p.sendMessage("/ride is now disabled except for in slipspace, to ride a ship ask the pilot to release and repilot the ship. /stopriding still works.");
			}
		} else if (e.getMessage().toLowerCase().startsWith("/region claim")) {
			boolean success = checkForTownyFactionsInSel(e.getPlayer());
			if (!success) {
				e.getPlayer().sendMessage("You cannot claim a worldguard in faction or town territory.");
				e.setCancelled(true);
			}
		}

		else if (newMessage.equals("/stopriding")) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
			if (crafts == null)
				return;
			for (Craft c : crafts) {
				try {
					c.playersRidingLock.acquire();
					if (c.playersRidingShip.contains(p.getUniqueId())) {
						c.playersRidingShip.remove(p.getUniqueId());
						c.playersRidingLock.release();
						p.sendMessage("You get off the craft.");
						return;
					}
					c.playersRidingLock.release();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			p.sendMessage("You aren't on a craft, you have nothing to stop riding.");
		} else if (newMessage.startsWith("/announce say")){
			e.setCancelled(true);
			e.getPlayer().sendMessage("This command is disabled. Nice try.");
		} else if (newMessage.startsWith("/an say")){
			e.setCancelled(true);
			long timeFuture = 1432862014;
			System.out.println(timeFuture);
			timeFuture = timeFuture * 1000;
			System.out.println(timeFuture);
			timeFuture = timeFuture + (1000 * 60 * 60 * 24 * 4);
			//future time = current timestamp + millis * seconds * minutes * days * 4
			System.out.println(System.currentTimeMillis());
			System.out.println(timeFuture);
			if(System.currentTimeMillis() < timeFuture){
				//we don't want to nuke them outside of the four day window
				e.getPlayer().getWorld().strikeLightning(e.getPlayer().getLocation());
				e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 60 * 60, 1));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eb janesudo " + e.getPlayer().getName() + " has used a command that"
						+ " they knew they shouldn't be able to access, and they did not report it. As a result, I did not report"
						+ " to them when or how I chose to fix it. They have been cursed for an hour. If you find an exploit,"
						+ " report what you found - or suffer the consequences when I fix it my way! :)");
			}
		}
	}

	private static boolean checkForTownyFactionsInSel(Player p) {
		Selection sel = null;
		try {
			sel = ShieldUtils.wg.getWorldEdit().getSelection(p);
		} catch (CommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
		if(sel == null) return true;
		int[] cmin = DockUtils.getChunkCoordinates(p.getWorld(), sel.getMinimumPoint().getBlockX(), sel.getMinimumPoint().getBlockZ());
		int[] cmax = DockUtils.getChunkCoordinates(p.getWorld(), sel.getMaximumPoint().getBlockX(), sel.getMaximumPoint().getBlockZ());

		// iterate over every chunk that the region contains
		for (int x = cmin[0]; x <= cmax[0]; x++) {
			for (int z = cmin[1]; z <= cmax[1]; z++) {

				// create a block to test
				Block b = p.getWorld().getBlockAt(x, 100, z);
				boolean success = DockUtils.checkTownyBuild(b, p);
				if(success) success = DockUtils.checkFactionsBuild(b, p);
				if (!success){
					return false;
				}
			}
		}
		return true;
	}
}
