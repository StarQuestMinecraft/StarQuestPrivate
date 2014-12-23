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
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.WarpUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class CommandListener implements Listener {

	@EventHandler
	public void onCommand( PlayerCommandPreprocessEvent e ) {

		if ( e.getMessage().equalsIgnoreCase( "/release") ) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer( e.getPlayer() );

			if ( pCraft != null ) {
				CraftManager.getInstance().removeCraft( pCraft );
				e.getPlayer().sendMessage( String.format( I18nSupport.getInternationalisedString( "Player- Craft has been released" ) ) );
			} else {
				e.getPlayer().sendMessage( String.format( I18nSupport.getInternationalisedString( "Player- Error - You do not have a craft to release!" ) ) );
			}

			e.setCancelled( true );
		}
		
		else if ( e.getMessage().equalsIgnoreCase( "/warpstart" ) ) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer( e.getPlayer() );

			if ( pCraft != null ) {
				WarpUtils.enterWarp(e.getPlayer(), pCraft);
			}

			e.setCancelled( true );
		}
		
		else if ( e.getMessage().equalsIgnoreCase( "/warpstop" ) ) {
			final Craft pCraft = CraftManager.getInstance().getCraftByPlayer( e.getPlayer() );

			if ( pCraft != null ) {
				WarpUtils.leaveWarp(e.getPlayer(), pCraft, true, true);
			}

			e.setCancelled( true );
		}
		
		else if ( e.getMessage().equalsIgnoreCase( "/ride" ) ) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			/*Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
			if(crafts != null){
				for(Craft c : crafts){
					if(MathUtils.playerIsWithinBoundingPolygon(c.getHitBox(), c.getMinX(), c.getMinZ(), MathUtils.bukkit2MovecraftLoc(p.getLocation()))){
						try{
							c.playersRidingLock.acquire();
							c.playersRidingShip.add(p.getUniqueId());
							c.playersRidingLock.release();
						}
						catch(Exception ex){
							ex.printStackTrace();
						}
						p.sendMessage("You board a craft of type " + c.getType().getCraftName() + " under the command of captain " + c.pilot.getName() + ".");
						return;
					}
				}
			}
			p.sendMessage("No craft found at your location for you to ride.");*/
			p.sendMessage("/ride is now disabled, to ride a ship ask the pilot to release and repilot the ship. /stopriding still works.");
		}
		
		else if ( e.getMessage().equalsIgnoreCase( "/stopriding" ) ) {
			e.setCancelled( true );
			Player p = e.getPlayer();
			Craft[] crafts = CraftManager.getInstance().getCraftsInWorld(p.getWorld());
			if(crafts == null) return;
			for(Craft c : crafts){
				try{
					c.playersRidingLock.acquire();
					if(c.playersRidingShip.contains(p.getUniqueId())){
						c.playersRidingShip.remove(p.getUniqueId());
						c.playersRidingLock.release();
						p.sendMessage("You get off the craft.");
						return;
					}
					c.playersRidingLock.release();
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
			p.sendMessage("You aren't on a craft, you have nothing to stop riding.");
		}
	}
}
