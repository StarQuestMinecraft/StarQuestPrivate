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

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.craft.Torpedo;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.AutopilotUtils;
import net.countercraft.movecraft.utils.BoardingRampUtils;
import net.countercraft.movecraft.utils.BomberUtils;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.Rotation;
import net.countercraft.movecraft.utils.WarpUtils;
import net.countercraft.movecraft.utils.WarpStartTask;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class InteractListener implements Listener {
	private static final Map<Player, Long> timeMap = new HashMap<Player, Long>();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {

		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
			Material m = event.getClickedBlock().getType();
			if ( m.equals( Material.SIGN_POST ) || m.equals( Material.WALL_SIGN ) ) {
				onSignRightClick( event );
			}
			if (event.getItem() != null && event.getItem().getType() == Material.BONE) {
				event.getPlayer().sendMessage("Name: " + event.getClickedBlock().getType());
				event.getPlayer().sendMessage("ID: " + event.getClickedBlock().getTypeId());
				event.getPlayer().sendMessage("Data: " + event.getClickedBlock().getData());
				event.setCancelled(true);
			}
		} else if ( event.getAction() == Action.LEFT_CLICK_BLOCK ) {
			Material m = event.getClickedBlock().getType();
			if ( m.equals( Material.SIGN_POST ) || m.equals( Material.WALL_SIGN ) ) {
				if ( event.getClickedBlock() == null ) {
					return;
				}
				Sign sign = ( Sign ) event.getClickedBlock().getState();
				String signText = sign.getLine( 0 );

				if ( signText == null ) {
					return;
				}

				if ( sign.getLine( 0 ).equals( "\\  ||  /" ) && sign.getLine( 1 ).equals( "==      ==" ) && sign.getLine( 2 ).equals( "/  ||  \\" ) ) {
					Craft craft = CraftManager.getInstance().getCraftByPlayer( event.getPlayer() );
					if ( craft != null ) {
						if ( event.getPlayer().hasPermission( "movecraft." + craft.getType().getCraftName() + ".rotate" ) ) {

							Long time = timeMap.get( event.getPlayer() );
							if ( time != null ) {
								long ticksElapsed = ( System.currentTimeMillis() - time ) / 50;
								if ( Math.abs( ticksElapsed ) < craft.getType().getTickCooldown() ) {
									event.setCancelled( true );
									return;
								}
							}

							if ( MathUtils.playerIsWithinBoundingPolygon( craft.getHitBox(), craft.getMinX(), craft.getMinZ(), MathUtils.bukkit2MovecraftLoc( event.getPlayer().getLocation() ) ) ) {
								if (!craft.shipAttemptingTeleport) {
									CraftManager.getInstance().getCraftByPlayer( event.getPlayer() ).rotate( Rotation.ANTICLOCKWISE, MathUtils.bukkit2MovecraftLoc( sign.getLocation() ) );
	
									timeMap.put( event.getPlayer(), System.currentTimeMillis() );
									event.setCancelled( true );
								}

							}

						}
					}
				}
			}
		}
	}

	private void onSignRightClick( PlayerInteractEvent event ) {
		Sign sign = ( Sign ) event.getClickedBlock().getState();
		String signText = sign.getLine( 0 );

		if ( signText == null ) {
			return;
		}


		if ( getCraftTypeFromString( sign.getLine( 0 ) ) != null ) {
			if (Movecraft.signContainsPlayername(sign, event.getPlayer().getName()) || event.getPlayer().isOp()) {
				// Valid sign prompt for ship command.
				if ( event.getPlayer().hasPermission( "movecraft." + sign.getLine( 0 ) + ".pilot" ) ) {
					// Attempt to run detection
					Location loc = event.getClickedBlock().getLocation();
					MovecraftLocation startPoint = new MovecraftLocation( loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );
					Craft c = new Craft( getCraftTypeFromString( sign.getLine( 0 ) ), loc.getWorld() );
	
					if ( CraftManager.getInstance().getCraftByPlayer( event.getPlayer() ) == null ) {
						c.detect( event.getPlayer().getName(), startPoint );
					} else {
						Craft pCraft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
						CraftManager.getInstance().removeCraft(pCraft);
						pCraft.extendLandingGear();
						event.getPlayer().sendMessage(String.format(I18nSupport.getInternationalisedString("Player- Craft has been released")));
					}
	
					event.setCancelled( true );
				}
				return;
			}
			event.getPlayer().sendMessage("You aren't a captain of this ship.");

		} else if ( sign.getLine( 0 ).equalsIgnoreCase( "[helm]" ) ) {
			sign.setLine( 0, "\\  ||  /" );
			sign.setLine( 1, "==      ==" );
			sign.setLine( 2, "/  ||  \\" );
			sign.update( true );
			event.setCancelled( true );
		} else if ( sign.getLine( 0 ).equals( "\\  ||  /" ) && sign.getLine( 1 ).equals( "==      ==" ) && sign.getLine( 2 ).equals( "/  ||  \\" ) ) {
			Craft craft = CraftManager.getInstance().getCraftByPlayer( event.getPlayer() );
			if ( craft != null ) {
				if ( event.getPlayer().hasPermission( "movecraft." + craft.getType().getCraftName() + ".rotate" ) ) {
					Long time = timeMap.get( event.getPlayer() );
					if ( time != null ) {
						long ticksElapsed = ( System.currentTimeMillis() - time ) / 50;
						if ( Math.abs( ticksElapsed ) < craft.getType().getTickCooldown() ) {
							event.setCancelled( true );
							return;
						}
					}

					if ( MathUtils.playerIsWithinBoundingPolygon( craft.getHitBox(), craft.getMinX(), craft.getMinZ(), MathUtils.bukkit2MovecraftLoc( event.getPlayer().getLocation() ) ) ) {
						if (!craft.shipAttemptingTeleport) {
							CraftManager.getInstance().getCraftByPlayer( event.getPlayer() ).rotate( Rotation.CLOCKWISE, MathUtils.bukkit2MovecraftLoc( sign.getLocation() ) );
	
							timeMap.put( event.getPlayer(), System.currentTimeMillis() );
							event.setCancelled( true );
						}
					}
				}
			}
		}else if (sign.getLine(0).equalsIgnoreCase("[autopilot]")) {
				sign.setLine(0, ChatColor.BLUE + "AUTOPILOT");
				sign.setLine(1, ChatColor.GREEN + "{DISABLED}");
				sign.update();
				return;
			} else if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "AUTOPILOT")) {
				if (sign.getLine(1).equals(ChatColor.GREEN + "{DISABLED}")) {
					Craft c = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
					if (c != null) {
						if(c.isAutopiloting){
							sign.setLine(1, ChatColor.RED + "{ENGAGED}");
							sign.update();
							AutopilotUtils.startAutopiloting(CraftManager.getInstance().getCraftByPlayer(event.getPlayer()), event.getPlayer());
							return;
						}
						event.getPlayer().sendMessage("Your craft is already autopiloting!");
						return;
					}
					event.getPlayer().sendMessage("You are not currently piloting a craft, you cannot start the autopilot.");
					return;
				} else if (sign.getLine(1).equals(ChatColor.RED + "{ENGAGED}")) {
					AutopilotUtils.stopAutopiloting(CraftManager.getInstance().getCraftByPlayer(event.getPlayer()), event.getPlayer(), sign);
				}
			} else if (sign.getLine(0).equalsIgnoreCase("[boardingramp]")) {
				sign.setLine(0, ChatColor.RED + "Boarding Ramp");
				sign.setLine(1, "{" + ChatColor.GREEN + "SHUT" + ChatColor.BLACK + "}");
				sign.update();
				return;

			} else if (sign.getLine(1).equals("{" + ChatColor.GREEN + "SHUT" + ChatColor.BLACK + "}")) {
				Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
				if (craft == null) {
					if (event.getPlayer().getLocation().getPitch() > 0F) {
						sign.setLine(1, "{" + ChatColor.RED + "OPEN" + ChatColor.BLACK + "}");
						sign.update();
						BoardingRampUtils.openRamp(sign);
						sign.getBlock().getWorld().playSound(sign.getBlock().getLocation(), Sound.PISTON_EXTEND, 2.0F, 1.0F);
						return;
					}
					event.getPlayer().sendMessage("You cannot toggle the boarding ramp from outside the ship.");
					return;
				}
				event.getPlayer().sendMessage("You cannot toggle the boarding ramp while piloting the ship.");
				return;

			} else if (sign.getLine(1).equals("{" + ChatColor.RED + "OPEN" + ChatColor.BLACK + "}")) {
				Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
				if (craft == null) {
					if (event.getPlayer().getLocation().getPitch() > 0F) {
						boolean success = BoardingRampUtils.closeRamp(sign, event.getPlayer());
						if (success) {
							sign.setLine(1, "{" + ChatColor.GREEN + "SHUT" + ChatColor.BLACK + "}");
							sign.update();
							sign.getBlock().getWorld().playSound(sign.getBlock().getLocation(), Sound.PISTON_RETRACT, 2.0F, 1.0F);
							return;
						}
						return;
					}
					event.getPlayer().sendMessage("You cannot toggle the boarding ramp unless you are inside the ship.");
					return;
				}
				event.getPlayer().sendMessage("You cannot toggle the boarding ramp while piloting the ship.");
				return;
			} else if (sign.getLine(0).equals("[torpedo]")){
				sign.setLine(0, ChatColor.RED + "Proton");
				sign.setLine(1, ChatColor.RED + "Torpedo");
				sign.setLine(2, ChatColor.RED + "Launcher");
				sign.update();
			} else if (sign.getLine(0).equals(ChatColor.RED + "Proton")){
				Torpedo.testAndLaunch(sign, event.getPlayer());
			} else if (sign.getLine(0).equals("[slipdrive]")){
				sign.setLine(0, ChatColor.AQUA + "Slipspace");
				sign.setLine(1, ChatColor.AQUA + "Drive");
				sign.setLine(2, ChatColor.GREEN + "Disabled.");
				sign.update();
			} else if (sign.getLine(2).equals(ChatColor.GREEN + "Disabled.")){
				new WarpStartTask(CraftManager.getInstance().getCraftByPlayer(event.getPlayer()), event.getPlayer(), sign);
			} else if (sign.getLine(2).equals(ChatColor.BLUE + "Stable Slip.")){
				WarpUtils.leaveWarp(event.getPlayer(), CraftManager.getInstance().getCraftByPlayer(event.getPlayer()));
				sign.setLine(2, ChatColor.GREEN + "Disabled.");
				sign.update();
			
			}else if (sign.getLine(0).equals("[bombchute]") && event.getPlayer().hasPermission("movecraft.bomber.move")){
				sign.setLine(0, ChatColor.RED + "Bomb");
				sign.setLine(1, ChatColor.RED + "Chute");
				sign.update();
			} else if (sign.getLine(0).equals(ChatColor.RED + "Bomb") && event.getPlayer().hasPermission("movecraft.bomber.move")){
				BlockFace direction = Torpedo.getFacingBlockFace(sign);
				Block sb = sign.getBlock();
				if(sb.getRelative(direction).getType() == Material.SPONGE){
					Block b = sb.getRelative(direction);
					if(b.getRelative(BlockFace.DOWN).getType() == Material.SPONGE){
						BomberUtils.fireCarpet(event.getPlayer(), b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation());
						return;
					}
				}
				event.getPlayer().sendMessage("Improperly built bombchute.");
				return;
			}
	}

	public static CraftType getCraftTypeFromString( String s ) {
		for ( CraftType t : CraftManager.getInstance().getCraftTypes() ) {
			if ( s.equalsIgnoreCase( t.getCraftName() ) ) {
				return t;
			}
		}

		return null;
	}


	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteractStick(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_AIR) {
			if (event.getItem() != null && event.getItem().getTypeId() == 347 && !event.getPlayer().isSneaking()) {
				Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
				if (craft != null) {
					craft.shootGuns(event.getPlayer());
					event.setCancelled(true);
				}
			}
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getItem() != null && event.getItem().getTypeId() == 347 && event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST
					&& !event.getPlayer().isSneaking()) {
				Craft craft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
				if (craft != null) {
					craft.shootGuns(event.getPlayer());
					event.setCancelled(true);
				}
			}
		}
	}

}
