package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.utils.MovingPartUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;

public class PartListener implements Listener{
	WorldEditPlugin we; 
	public PartListener(){
		we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST){
				Sign s = (Sign) event.getClickedBlock().getState();
				
				String lZero = s.getLine(0);
				String lOne = s.getLine(1);
				String lThree = s.getLine(3);
				
				Player p = event.getPlayer();
				
				//initial setup part one
				if (lZero.equalsIgnoreCase("[movingpart]")){
					if (p.hasPermission("movingpart.use")){
						if (!(lOne == null || lOne.equals("") )){
							s.setLine(0, ChatColor.DARK_RED + "Moving Part");
							s.setLine(2, event.getPlayer().getName());
							s.setLine(3, "Selection Mode");
							s.update();
							p.sendMessage("Now select the toggle area region. When it is selected, come and click this sign again.");
							return;
						}
						p.sendMessage("You need to put an area name on the second line, like 'folding wings' or something.");
						return;
					}
					p.sendMessage("You don't have permission for this.");
					return;
				}
				
				//initial setup part two
				if (lZero.equalsIgnoreCase(ChatColor.DARK_RED + "Moving Part")){
					Selection selection = we.getSelection(p);
					if (selection != null){
						if (selection instanceof CuboidSelection){
							CuboidSelection sel = (CuboidSelection) selection;
							CuboidRegion region = (CuboidRegion) sel.getRegionSelector().getIncompleteRegion();
							
							boolean success = MovingPartUtils.saveSchematic(s, region, we, p, false);
							
							if (success){
								s.setLine(0, ChatColor.RED + "Moving Part");
								s.update();
								p.sendMessage("Success! Now rebuild the area to be in its 'on' state and then click the sign again.");
								return;
							}
							p.sendMessage("Something went wrong. Please report this to dibujaron.");
							return;
						}
						p.sendMessage("Your selection was not cuboid. Select a cuboid region.");
						return;
					}
					p.sendMessage("You do not have a selection currently.");
					return;
				}
				
				//initial setup part three
				if (lZero.equalsIgnoreCase(ChatColor.RED + "Moving Part")){
					Selection selection = we.getSelection(p);
					if (selection != null){
						if (selection instanceof CuboidSelection){
							CuboidSelection sel = (CuboidSelection) selection;
							CuboidRegion region = (CuboidRegion) sel.getRegionSelector().getIncompleteRegion();
							
							boolean success = MovingPartUtils.saveSchematic(s, region, we, p, true);
							
							if (success){
								s.setLine(0, ChatColor.GREEN + "Moving Part");
								s.setLine(3, ChatColor.GREEN + "ON");
								s.update();
								p.sendMessage("Success! If you screwed up your regions, break this sign and make it again with the same area name, it'll reset.");
								return;
							}
							p.sendMessage("Something went wrong. Please report this to dibujaron.");
							return;
						}
						p.sendMessage("Your selection was not cuboid. Select a cuboid region.");
						return;
					}
					p.sendMessage("You do not have a selection currently.");
					return;
				}
				
				//turn it on
				if (lZero.equalsIgnoreCase(ChatColor.GREEN + "Moving Part") && lThree.equalsIgnoreCase(ChatColor.RED + "OFF")){
					if (p.hasPermission("movingpart.use")){
						boolean success = MovingPartUtils.switchSchematic(s, we, p, false);
						if (success){
							s.setLine(0, ChatColor.GREEN + "Moving Part");
							s.setLine(3, ChatColor.GREEN + "ON");
							s.update();
							event.getPlayer().getWorld().playSound(s.getBlock().getLocation(), Sound.PISTON_EXTEND, 2.0F, 1.0F);
							if (CraftManager.getInstance().getCraftByPlayer(p) != null){
								Craft c = CraftManager.getInstance().getCraftByPlayer(p);
								CraftManager.getInstance().removeCraft(c);
								c = new Craft(c.getType(), c.getW());
								c.detect(p.getName(), MathUtils.bukkit2MovecraftLoc(p.getLocation()));
								return;
							}
						}
						return;
					}
					p.sendMessage("You don't have permission for this.");
					return;
				}
				//turn it off
				if (lZero.equalsIgnoreCase(ChatColor.GREEN + "Moving Part") && lThree.equalsIgnoreCase(ChatColor.GREEN + "ON")){
					if (p.hasPermission("movingpart.use")){
						boolean success = MovingPartUtils.switchSchematic(s, we, p, true);
						
						if (success){
							s.setLine(0, ChatColor.GREEN + "Moving Part");
							s.setLine(3, ChatColor.RED + "OFF");
							s.update();
							event.getPlayer().getWorld().playSound(s.getBlock().getLocation(), Sound.PISTON_EXTEND, 2.0F, 1.0F);
							if (CraftManager.getInstance().getCraftByPlayer(p) != null){
								Craft c = CraftManager.getInstance().getCraftByPlayer(p);
								CraftManager.getInstance().removeCraft(c);
								c = new Craft(c.getType(), c.getW());
								c.detect(p.getName(), MathUtils.bukkit2MovecraftLoc(p.getLocation()));
								return;
							}
						}
					}
				}	
			}
		}
	}
}
