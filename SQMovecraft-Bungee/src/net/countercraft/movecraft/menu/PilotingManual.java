package net.countercraft.movecraft.menu;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class PilotingManual {
	
	static ItemStack manual;
	
	public static void setup(){
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta)book.getItemMeta();
        bm.setAuthor("dibujaron");
        bm.setTitle("How to Fly a Starship");
        ArrayList<String> pages = new ArrayList<String>();
        
        String page1 = 
        		"Welcome to StarQuest! If you've forgotten, this is how you fly a starship."
        book.setItemMeta(bm);
	}
	
	public static ItemStack getPilotManual(){
		return manual.clone();
	}
}
