package net.countercraft.movecraft.utils;

import java.text.DecimalFormat;

import net.countercraft.movecraft.craft.CraftManager;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShipSizeUtils
{
	public static boolean printPlayerShipSize(Player player, boolean color)
	{
		if (CraftManager.getInstance().getCraftByPlayer(player) != null)
		{
			double numBlocks = 0; //double so that percentages don't truncate to 0
			int[] numWoolTypes = new int[16];
			int numArmor = 0;
			int numDroppers = 0;
			int numDispensers = 0;
			int numCannons = 0;
			int numOther = 0;
			
			for (MovecraftLocation loc : CraftManager.getInstance().getCraftByPlayer(player).getBlockList())
			{
				Material block = (player).getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ()).getType();
				
				if(BlastUtils.getBlastResistance(block) >= CraftManager.getInstance().getCraftByPlayer(player).getType().getArmorResistance())
					numArmor++;
				
				if (block.equals(Material.WOOL))
					numWoolTypes[player.getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ()).getData()]++;
				else if (block.equals(Material.DROPPER))
					numDroppers++;
				else if (block.equals(Material.DISPENSER))
					numDispensers++;
				else if (block.equals(Material.SPONGE))
				{
					if (player.getWorld().getBlockAt(loc.getX() + 1, loc.getY(), loc.getZ()).getType().equals(Material.PISTON_BASE) ||
						player.getWorld().getBlockAt(loc.getX() - 1, loc.getY(), loc.getZ()).getType().equals(Material.PISTON_BASE) ||
						player.getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ() + 1).getType().equals(Material.PISTON_BASE) ||
						player.getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ() - 1).getType().equals(Material.PISTON_BASE))
						numCannons++;
				}
				else if (BlastUtils.getBlastResistance(block) < CraftManager.getInstance().getCraftByPlayer(player).getType().getArmorResistance())
					numOther++;
				
				numBlocks++;
			}
			
			int numWool = 0;
			for (int i : numWoolTypes)
				numWool += i;
				
			DecimalFormat format = new DecimalFormat("#0.000");
			
			player.sendMessage(ChatColor.AQUA + "Size: " + numBlocks + " blocks");
			player.sendMessage(ChatColor.AQUA + "Wool: " + numWool + " (" + format.format((numWool/numBlocks) * 100) + "%) blocks");
			player.sendMessage(ChatColor.AQUA + "Armor: " + numArmor + " (" + format.format((numArmor/numBlocks) * 100) + "%) blocks");
			player.sendMessage(ChatColor.AQUA + "Storage: " + numDroppers + " (" + format.format((numDroppers/numBlocks) * 100) + "%) blocks");
			player.sendMessage(ChatColor.AQUA + "Cannons: " + numCannons + " (" + format.format((numCannons/numBlocks) * 100) + "%) blocks");
			player.sendMessage(ChatColor.AQUA + "Dispensers: " + numDispensers + " (" + format.format((numDispensers/numBlocks) * 100) + "%) blocks");
			player.sendMessage(ChatColor.AQUA + "Other: " + numOther + " (" + format.format((numOther/numBlocks) * 100) + "%) blocks");
			if (color)
			{
				for (int i = 0; i < numWoolTypes.length; i++)
				{
					if (i == 0)
						continue;
					
					switch (i)
					{
					case 0: player.sendMessage(ChatColor.WHITE + "White Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 1: player.sendMessage(ChatColor.GOLD + "Orange Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 2: player.sendMessage(ChatColor.LIGHT_PURPLE + "Magents Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 3: player.sendMessage(ChatColor.AQUA + "Light Blue Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 4: player.sendMessage(ChatColor.YELLOW + "Yellow Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 5: player.sendMessage(ChatColor.GREEN + "Lime Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 6: player.sendMessage(ChatColor.LIGHT_PURPLE + "Pink Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 7: player.sendMessage(ChatColor.DARK_GRAY + "Gray Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 8: player.sendMessage(ChatColor.GRAY + "Light Gray Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 9: player.sendMessage(ChatColor.DARK_AQUA + "Cyan Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 10: player.sendMessage(ChatColor.DARK_PURPLE + "Purple Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 11: player.sendMessage(ChatColor.DARK_BLUE + "Blue Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 12: player.sendMessage(ChatColor.GOLD + "Brown Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 13: player.sendMessage(ChatColor.DARK_GREEN + "Green Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 14: player.sendMessage(ChatColor.DARK_RED + "Red Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					case 15: player.sendMessage(ChatColor.BLACK + "Black Wool: " + numWoolTypes[i] + " (Total:" + ((numWoolTypes[i]/numBlocks) * 100) + "%, Wool: " + ((numWoolTypes[i]/numBlocks) * 100) + "%) blocks"); break;
					}
				}
			}
			
			return true;
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You must be piloting a ship to use this command!");
			return false;
		}
	}
}
