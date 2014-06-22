package net.countercraft.movecraft.menu;

import java.util.HashMap;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.CraftManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StarshipMenu implements Listener{
	
	private final String TITLE = "Starship Control Console";
	Inventory menu = null;
	
	private HashMap<Player, String[]> craftNamePlayerMap = new HashMap<Player, String[]>();
	public void setup(){
		Bukkit.getPluginManager().registerEvents(this, Movecraft.getInstance());
		menu = Bukkit.createInventory(null, 9);
		menu.setItem(0, createMenuItem(Material.WATCH, "Pilot Starship"));
		menu.setItem(1, createMenuItem(Material.STONE_AXE, "Redetect Starship"));
		menu.setItem(2, createMenuItem(Material.FEATHER, "Rename Starship"));
		menu.setItem(3, createMenuItem(Material.SKULL_ITEM, "Add Friend", 1, (short) 3));
		menu.setItem(4, createMenuItem(Material.SKULL_ITEM, "Remove Friend"));
		menu.setItem(5, createMenuItem(Material.PAPER, "Starship Information"));
		menu.setItem(6, createMenuItem(Material.BOOK, "Piloting Instructions"));
		menu.setItem(7, createMenuItem(Material.ENDER_PEARL, "Dynmap"));
		menu.setItem(8, createMenuItem(Material.SIGN, "Release Ship"));
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST));
		Sign s = (Sign) event.getClickedBlock().getState();
		event.getPlayer().openInventory(menu);
		craftNamePlayerMap.put(event.getPlayer(), s.getLines());
	}
	
	public void onPlayerInteractInventory(InventoryInteractEvent event){
		if(event.getInventory().getTitle().equals(TITLE)){
			if(event instanceof InventoryClickEvent){
				InventoryClickEvent e = (InventoryClickEvent) event;
				Player p = (Player) event.getWhoClicked();
				event.getView().close();
				switch(e.getSlot()){
				case 0:
					//pilot ship
					return;
				case 1:
					//redetect ship
					return;
				case 2:
					//rename ship
					return;
				case 3:
					//add friend
					return;
				case 4:
					//remove friend
					return;
				case 5:
					//show stats
					return;
				case 6:
					p.getInventory().addItem(PilotingManual.getPilotManual());
					p.sendMessage("Here is a piloting manual!");
					return;
				case 7:
					p.sendMessage(ChatColor.RED + "StarQuest Dynmap: " + ChatColor.GOLD + "http://map.starquestminecraft.com");
					return;
				case 8:
					CraftManager.getInstance().removeCraft(CraftManager.getInstance().getCraftByPlayer(p));
					return;
				}
			}
		}
	}
	
	private ItemStack createMenuItem(Material m, String title, int amount, short damage){
		ItemStack retval = new ItemStack(m, amount, damage);
		retval.getItemMeta().setDisplayName(ChatColor.AQUA + title);
		return retval;
	}
	
	private ItemStack createMenuItem(Material m, String title){
		return createMenuItem(m, title, 1, (short) 0);
	}
}
