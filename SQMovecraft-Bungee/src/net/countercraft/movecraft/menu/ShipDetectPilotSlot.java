package net.countercraft.movecraft.menu;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import edu.vccs.email.amm28053.inventoryMenuAPI.slot.AbstractSlot;

public class ShipDetectPilotSlot extends AbstractSlot{

	Sign sign;
	
	public ShipDetectPilotSlot(String id, Material mat, Sign sign) {
		super(id, mat);
		this.sign = sign;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Player player) {
		if ( getCraftTypeFromString( sign.getLine( 0 ) ) != null ) {
			if (Movecraft.signContainsPlayername(sign, player.getName()) || player.hasPermission("movecraft.override")) {
				// Valid sign prompt for ship command.
				if ( player.hasPermission( "movecraft." + sign.getLine( 0 ) + ".pilot" ) || player.hasPermission("movecraft.override") ) {
					// Attempt to run detection
					Location loc = sign.getLocation();
					MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

					if (CraftManager.getInstance().getCraftByPlayer(player) == null) {
						Craft c = new Craft(getCraftTypeFromString(sign.getLine(0)), loc.getWorld());
						c.detect(player, startPoint);
					} else {
						Craft pCraft = CraftManager.getInstance().getCraftByPlayer(player);
						CraftManager.getInstance().removeCraft(pCraft);
						pCraft.extendLandingGear();
						player.sendMessage(String.format(I18nSupport.getInternationalisedString("Player- Craft has been released")));
						return;
					}
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permission for this type of craft!");
					return;
				}

			}
			player.sendMessage("You aren't a captain of this ship.");

		}
	}
	public static CraftType getCraftTypeFromString(String s) {

		for (CraftType t : CraftManager.getInstance().getCraftTypes()) {
			if (s.equalsIgnoreCase(t.getCraftName())) {
				return t;
			}
		}

		return null;
	}
}
