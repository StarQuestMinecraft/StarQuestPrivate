package com.dibujaron.cardboardbox.meta;

import java.io.Serializable;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardMetaGlobal implements CardboardItemMeta, Serializable {

	private static final long serialVersionUID = 1709865648401783363L;
	private Map<String, Object> serializedItem;
	
	public CardboardMetaGlobal(ItemStack item) {
		
		this.serializedItem = item.serialize();
		
	}
	
	public ItemMeta unbox() {
		
		return ItemStack.deserialize(this.serializedItem).getItemMeta();
		
	}
	
	
	
}
