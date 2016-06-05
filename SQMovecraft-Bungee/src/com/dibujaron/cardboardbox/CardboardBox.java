
package com.dibujaron.cardboardbox;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.dibujaron.cardboardbox.meta.CardboardItemMeta;
import com.dibujaron.cardboardbox.meta.CardboardMetaBook;
import com.dibujaron.cardboardbox.meta.CardboardMetaBook2;
import com.dibujaron.cardboardbox.meta.CardboardMetaEnchantment;
import com.dibujaron.cardboardbox.meta.CardboardMetaFirework;
import com.dibujaron.cardboardbox.meta.CardboardMetaFireworkEffect;
import com.dibujaron.cardboardbox.meta.CardboardMetaLeatherArmor;
import com.dibujaron.cardboardbox.meta.CardboardMetaMap;
import com.dibujaron.cardboardbox.meta.CardboardMetaSkull;
import com.dibujaron.cardboardbox.utils.CardboardEnchantment;
import com.sk89q.util.ReflectionUtil;

import net.minecraft.server.v1_9_R1.NBTBase;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;
import net.minecraft.server.v1_9_R1.NBTTagString;

/**
 * A serializable ItemStack
 */
public class CardboardBox implements Serializable {
	
	private static final long serialVersionUID = 729890133797629668L;
	
	private final int type, amount;
	private final short damage;
	private String name;
	private List<String> lore;
	private String potionNBT;
	
	private final HashMap<CardboardEnchantment, Integer> enchants;
	private CardboardItemMeta meta;
	
	public CardboardBox(ItemStack item) {
		
		this.type = item.getTypeId();
		this.amount = item.getAmount();
		this.damage = item.getDurability();
		
		net.minecraft.server.v1_9_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		
		if(nmsStack != null) {
			
			if (nmsStack.hasTag()) {
				
				if(item.getType().equals(Material.POTION)) {
					
					this.potionNBT = nmsStack.getTag().getString("Potion");
					Bukkit.getServer().broadcastMessage(this.potionNBT);
				}
			}   
			
		}
		
		if (item.hasItemMeta()) {
			ItemMeta m = item.getItemMeta();
			System.out.println("has item meta: " + item.getType());
			if (m.hasDisplayName()) {
				this.name = m.getDisplayName();
			}
			
			if (m.hasLore()) {
				this.lore = m.getLore();
			}
			
		}
		
		HashMap<CardboardEnchantment, Integer> map = new HashMap<CardboardEnchantment, Integer>();
		
		Map<Enchantment, Integer> enchantments = item.getEnchantments();
		
		for (Enchantment enchantment : enchantments.keySet()) {
			map.put(new CardboardEnchantment(enchantment), enchantments.get(enchantment));
		}
		
		this.enchants = map;
		
		// I call the below switch after the regular item parsing because
		// enchantment books register as having enchantments
		
		// IDs of items that have meta. Future items will need to be added here
		switch (item.getTypeId()) {
		case 386: // Book and quill
		case 387: // Written Book
			System.out.println("Creating new cardboard meta book.");
			this.meta = (new CardboardMetaBook2(item));
			break;
		case 403: // Enchanted Book
			this.meta = (new CardboardMetaEnchantment(item));
			break;
		case 402: // firework star
			if (!item.hasItemMeta())
				break;
			this.meta = (new CardboardMetaFireworkEffect(item));
			break;
		case 401: // firework
			this.meta = (new CardboardMetaFirework(item));
			break;
		case 298: // Leather armor
		case 299:
		case 300:
		case 301:
			this.meta = (new CardboardMetaLeatherArmor(item));
			break;
		case 358: // map
			this.meta = (new CardboardMetaMap(item));
			break;
		case 397: // player head
			this.meta = (new CardboardMetaSkull(item));
			break;
		default:
			this.meta = null;
			break;
			
		}
		
	}
	
	public ItemStack unbox() {
		
		ItemStack item = new ItemStack(type, amount, damage);
		
		
		
		// These metas below will never be null because of the if/else during
		// the packing
		
		ItemFactory factory = Bukkit.getServer().getItemFactory();
		ItemMeta itemMeta = factory.getItemMeta(Material.getMaterial(item.getTypeId()));
		
		// Should only have one specific item meta at a time
		if ((this.meta != null)) {
			itemMeta = this.meta.unbox();
		}
		if (this.name != null) {
			itemMeta.setDisplayName(this.name);
		}
		if (this.lore != null) {
			itemMeta.setLore(this.lore);
		}
		// Apply item meta
		item.setItemMeta(itemMeta);
		
		HashMap<Enchantment, Integer> map = new HashMap<Enchantment, Integer>();
		
		for (CardboardEnchantment cEnchantment : enchants.keySet()) {
			map.put(cEnchantment.unbox(), enchants.get(cEnchantment));
		}
		
		item.addUnsafeEnchantments(map);
		
		net.minecraft.server.v1_9_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		
		if(nmsStack != null) {
			
			if(this.potionNBT != null) {
				
				if(item.getType().equals(Material.POTION)) {
					
					NBTTagCompound compound = nmsStack.getTag();
					compound.set("Potion", new NBTTagString(this.potionNBT));
					nmsStack.setTag(compound);
					item = CraftItemStack.asBukkitCopy(nmsStack);
					
				}
			}
		}
		
		return item;
	}
	
}