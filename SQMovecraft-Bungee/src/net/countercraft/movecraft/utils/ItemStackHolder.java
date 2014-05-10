package net.countercraft.movecraft.utils;

import org.bukkit.inventory.ItemStack;

public class ItemStackHolder {
	public ItemStack i;
	public int slot;
	
	public ItemStackHolder(ItemStack i, int slot){
		this.i = i;
		this.slot = slot;
	}
}
