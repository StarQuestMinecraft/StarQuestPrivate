package net.countercraft.movecraft.redline;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.block.Sign;
//import org.bukkit.entity.Player;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.dynmap.DynmapAPI;
//import org.dynmap.markers.MarkerAPI;
//
//import net.countercraft.movecraft.Movecraft;
//import net.countercraft.movecraft.craft.Craft;
//import net.countercraft.movecraft.craft.CraftManager;
//import net.countercraft.movecraft.utils.MovecraftLocation;
//
public class RedlineUtils 
{
//	
//	private static DynmapAPI api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("Dynmap");
//	MarkerAPI mapi = api.getMarkerAPI();
//	
//	public static void displayJumpToMap(Player p, Location from, Location to){
//		command("dmarker addcorner " + from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + " " + p.getWorld().getName());
//		command("dmarker addcorner " + to.getBlockX() + " " + to.getBlockY() + " " + to.getBlockZ() + " " + p.getWorld().getName());
//		command("dmarker addline id:" + createWarpLineId(p) + " " + createWarpLineName(p));
//	}
//	
//	public static void removeJumpFromMap(Player p){
//		command("dmarker deleteline id:" + createWarpLineId(p));
//	}
//	
//	private static void command(String s){
//		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
//	}
//	
//	private static String createWarpLineId(Player p){
//		return p.getName() + "-slipjump";
//	}
//	
//	private static String createWarpLineName(Player p){
//		return p.getName() + "'s Slipdrive Trajectory";
//	}
//	
//	public static int getNumComputers(Craft craft)
//	{
//		int count = 0;
//		for (MovecraftLocation mLoc : craft.getBlockList())
//		{
//			Block block = craft.getW().getBlockAt(mLoc.getX(), mLoc.getY(), mLoc.getZ());
//			if (block.getType().equals(Material.ENCHANTMENT_TABLE) && isComputer(block))
//				count++;
//		}	
//		return count;
//	}
//	
//	private static boolean isComputer(Block enchTable)
//	{
//		return enchTable.getRelative(1, 0, 0).getType().equals(Material.STEP) &&
//				enchTable.getRelative(-1, 0, 0).getType().equals(Material.STEP) &&
//				enchTable.getRelative(0, 0, 1).getType().equals(Material.STEP) &&
//				enchTable.getRelative(0, 0, -1).getType().equals(Material.STEP) &&
//				enchTable.getRelative(1, 2, 0).getType().equals(Material.STEP) &&
//				enchTable.getRelative(-1, 2, 0).getType().equals(Material.STEP) &&
//				enchTable.getRelative(0, 2, 1).getType().equals(Material.STEP) &&
//				enchTable.getRelative(0, 2, -1).getType().equals(Material.STEP) &&
//				enchTable.getRelative(0, 3, 0).getType().equals(Material.GLOWSTONE);
//	}
//	
//	public static boolean isRedlineConsoleSign(Sign sign)
//	{
//		return sign.getLine(0).equals(ChatColor.RED + "" + ChatColor.UNDERLINE + "Redline");
//	}
//	
//	public static void openConsole(PlayerInteractEvent event)
//	{
//		List<String> contrabandLore = new ArrayList<String>();
//		contrabandLore.add(ChatColor.RED + "" + ChatColor.MAGIC + "Contraband");
//		Inventory inv = Bukkit.createInventory(event.getPlayer(), 9, ChatColor.AQUA + "Select a destination");
//		
//		addItem(inv, 0, Material.GRASS, 0, "Planet", contrabandLore);
//		addItem(inv, 1, Material.FIREWORK_CHARGE, 0, "System", contrabandLore);
//		addItem(inv, 2, Material.COMPASS, 0, "Coordinates", contrabandLore);
//		
//		event.getPlayer().openInventory(inv);
//	}
//	
//	private static ItemStack addItem(Inventory inv, int slot, Material material, int damage, String name, List<String> lore)
//	{
//		ItemStack stack = new ItemStack(material, 1, (short) damage);
//		ItemMeta meta = stack.getItemMeta();
//		meta.setDisplayName(name);
//		meta.setLore(lore);
//		stack.setItemMeta(meta);
//		
//		inv.setItem(slot, stack);
//		return stack;
//	}
//	
//	public static void processCoordinatesInput(Player player)
//	{
//		player.sendMessage(ChatColor.AQUA + "To jump to specific coordiantes, type \"/redline <x> <y> <z>\"");
//	}
//	
//	public static boolean onRedlineCommand(Player player, String[] args)
//	{
//		if (CraftManager.getInstance().getCraftByPlayer(player) == null)
//		{
//			player.sendMessage(ChatColor.RED + "You must be piloting a ship to use this command!");
//			return true;
//		}
//		
//		if (args.length == 1)
//		{
//			return false;
//		}
//		else if (args.length == 2)
//		{
//			int x = 0;
//			int z = 0;
//			
//			try
//			{
//				x = Integer.parseInt(args[0]);
//				z = Integer.parseInt(args[1]);
//			}
//			catch (NumberFormatException e)
//			{
//				return false;
//			}
//			
//			if (CraftManager.getInstance().getCraftByPlayer(player).pilot.equals(player))
//			{
//				if (CraftManager.getInstance().getCraftByPlayer(player).isProcessingTeleport())
//				{
//					player.sendMessage(ChatColor.RED + "Your ship is already jumping!");
//					return true;
//				}			
//				if (getNumComputers(CraftManager.getInstance().getCraftByPlayer(player)) == 0)
//				{
//					player.sendMessage(ChatColor.RED + "You need at least one redline computer on your ship to do this!");
//					return true;
//				}
//				if (CraftManager.getInstance().getCraftByPlayer(player).getMinX() - x == 0 &&
//						CraftManager.getInstance().getCraftByPlayer(player).getMinZ() - z == 0)
//				{
//					player.sendMessage(ChatColor.RED + "You are already there!");
//					return true;
//				}
//				
//				initializeJumpSequence(player, x, z);
//			}
//			else 
//				player.sendMessage(ChatColor.RED + "You must be piloting a ship to use this command!");
//			return true;
//		}
//		return false;
//	}
//	
//	private static void initializeJumpSequence(Player player, int x, int z)
//	{
//		int maxJumpDist = 250;
//		
//		Craft craft = CraftManager.getInstance().getCraftByPlayer(player);
//		int xDist = x - craft.getMinX();
//		int zDist = z - craft.getMinZ();
//		int totalDist = (int) Math.sqrt(xDist * xDist + zDist * zDist);
//		
//		int numJumps = (totalDist / maxJumpDist) + 1;
//	
//		int ticksPerJump = 1200 / getNumComputers(craft);
//		
//		player.sendMessage(ChatColor.AQUA + "Initializing slipdrive warmup. Each jump will take "
//		+ (ticksPerJump / 20) + " seconds, for a total travel time of " + (numJumps * (ticksPerJump / 20)) + " seconds. " 
//		+ "You will not be able to move your ship until all of these jumps have been completed.");
//		craft.setProcessingTeleport(true);
//		
//		int xJumpDist;
//		int zJumpDist;
//		
//		if (xDist == 0)
//		{
//			xJumpDist = 0;
//			zJumpDist = (int) (Math.signum(z) * 250);
//		}
//		else
//		{
//			double angle = Math.atan(zDist/xDist);
//			xJumpDist = (int) (Math.cos(angle) * maxJumpDist);
//			zJumpDist = (int) (Math.sin(angle) * maxJumpDist);
//			if (xDist < 0)
//			{
//				xJumpDist = -xJumpDist;
//				zJumpDist = -zJumpDist;
//			}
//		}
//		
//		Location start = new Location(craft.getW(), craft.getMinX(), 65, craft.getMinZ());
//		displayJumpToMap(player, start, start.add(xJumpDist, 0, zJumpDist));
//		for (int i = 1; i < numJumps; i++)
//		{
//			Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable()
//			{
//				private Craft craft;
//				private int x;
//				private int z;
//				
//				@Override
//				public void run()
//				{
//					removeJumpFromMap(craft.pilot);
//					craft.getW().getChunkAt(craft.getMinX() + x, craft.getMinZ() + z).load();
//					craft.translate(x, 0, z, false);
//					Location start = new Location(craft.getW(), craft.getMinX(), 65, craft.getMinZ());
//					displayJumpToMap(craft.pilot, start, start.add(x, 0, z));
//				}
//				
//				private Runnable setVariables(Craft craft, int x, int z)
//				{
//					this.craft = craft;
//					this.x = x;
//					this.z = z;
//					return this;
//				}
//			}.setVariables(craft, xJumpDist, zJumpDist), ticksPerJump * i);
//		}
//		
//		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable()
//				{
//					Craft craft;
//					int x;
//					int z;
//					
//					@Override
//					public void run() 
//					{
//						removeJumpFromMap(craft.pilot);
//						craft.translate(x - craft.getMinX(), 0, z - craft.getMinZ(), false);
//						craft.setProcessingTeleport(false);
//						craft.pilot.sendMessage(ChatColor.AQUA + "You have reached your destination.");
//					}
//					
//					private Runnable setVariables(Craft craft, int x, int z)
//					{
//						this.craft = craft;
//						this.x = x;
//						this.z = z;
//						return this;
//					}
//					
//				}.setVariables(craft, x, z), ticksPerJump * numJumps);
//	}
}