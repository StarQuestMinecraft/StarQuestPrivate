package com.dibujaron.cardboardbox.meta;

import java.io.Serializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardMetaBook2 implements CardboardItemMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4470835095022165767L;
	int id;
	String title;
	String author;
	String[] pages;
	
	public CardboardMetaBook2(ItemStack stack){
		BookMeta meta = (BookMeta) stack.getItemMeta();
		this.id = stack.getTypeId();
		this.title = meta.getTitle();
		this.author = meta.getAuthor();
		this.pages = meta.getPages().toArray(new String[0]);
	}
	@Override
	public ItemMeta unbox() {
		BookMeta meta = (BookMeta) new ItemStack(this.id).getItemMeta();
		meta.setTitle(this.title);
		meta.setAuthor(this.author);
		meta.setPages(this.pages);
		// TODO Auto-generated method stub
		return meta;
	}
	//recreating CardboardMetaBook because I don't know what's wrong with it
	
}
