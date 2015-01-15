
package net.countercraft.movecraft.bungee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.countercraft.movecraft.cardboardbox.CardboardBox;
import net.countercraft.movecraft.cardboardbox.Knapsack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	//@formatting:off
	 /*public void saveInv(Arguments entry) {
		  Player p = getServer().getPlayer(entry.getKey());
		  Inventory armorInv = Bukkit.createInventory(p, 9);
		  ItemStack[] armor = p.getInventory().getArmorContents();
		  for(int i=0; i<armor.length;i++){
		   armorInv.setItem(i, armor[i]);
		  }
		  entry.setValue("Inventory", InventoryStringDeSerializer.InventoryToString(p.getInventory()));
		  System.out.println("InvDone");
		  entry.setValue("Armor",InventoryStringDeSerializer.InventoryToString(armorInv));
		  System.out.println("ArmorDone");
		}
	 public void restoreInv(Arguments entry) {
		  Player p = getServer().getPlayer(entry.getKey());
		  System.out.println(p.getName());
		  Inventory inv = InventoryStringDeSerializer.StringToInventory(entry.getValue("Inventory"));
		  Inventory armorInv = InventoryStringDeSerializer.StringToInventory(entry.getValue("Armor"));
		  ItemStack[] armor = new ItemStack[4];
		  for(int i=0; i<armor.length;i++){
		   armor[i]=armorInv.getItem(i);
		  }
		  p.getInventory().setContents(inv.getContents());
		  p.getInventory().setArmorContents(armor);
	}*/
	
	
    /*public static String InventoryToString (Inventory invInventory)
    {
        String serialization = invInventory.getSize() + ";";
        for (int i = 0; i < invInventory.getSize(); i++)
        {
            ItemStack is = invInventory.getItem(i);
            if (is != null)
            {
                String serializedItemStack = new String();
               
                String isType = String.valueOf(is.getType().getId());
                serializedItemStack += "t@" + isType;
               
                if (is.getDurability() != 0)
                {
                    String isDurability = String.valueOf(is.getDurability());
                    serializedItemStack += ":d@" + isDurability;
                }
               
                if (is.getAmount() != 1)
                {
                    String isAmount = String.valueOf(is.getAmount());
                    serializedItemStack += ":a@" + isAmount;
                }
               
                Map<Enchantment,Integer> isEnch = is.getEnchantments();
                if (isEnch.size() > 0)
                {
                    for (Entry<Enchantment,Integer> ench : isEnch.entrySet())
                    {
                        serializedItemStack += ":e@" + ench.getKey().getId() + "@" + ench.getValue();
                    }
                }
               
                serialization += i + "#" + serializedItemStack + ";";
            }
        }
        return serialization;
    }*/
	
	public static void writePlayer(DataOutputStream os, Player p){
		try {
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(new Knapsack(p));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public static Knapsack readPlayer(DataInputStream ips){
		Knapsack playerKnap = null;
		try {
			ObjectInputStream in = new ObjectInputStream(ips);
			playerKnap = (Knapsack) in.readObject();
			in.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playerKnap;
	}
	
	public static void writeInventory(DataOutputStream os, Inventory inventory){
		try {
			os.writeInt(inventory.getSize());
			ObjectOutputStream out = new ObjectOutputStream(os);
			ItemStack[] is = inventory.getContents();
			CardboardBox[] cardboardBoxes = new CardboardBox[is.length];
			for(int i = 0; i < is.length; i++){
				cardboardBoxes[i] = pack(is[i]);
			}
			out.writeObject(cardboardBoxes);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	public static Inventory readInventory(DataInputStream ips, InventoryType type){
		int length = 27;
		try{ length = ips.readInt(); } catch (Exception e){ e.printStackTrace();};
		Inventory inv = getInventory(length, type);
		try {
			ObjectInputStream in = new ObjectInputStream(ips);
			CardboardBox[] cardboardBoxes = (CardboardBox[]) in.readObject();
			in.close();
			ItemStack[] is = new ItemStack[cardboardBoxes.length];
			for(int i = 0; i < cardboardBoxes.length; i++){
				is[i] = unpack(cardboardBoxes[i]);
			}
			inv.setContents(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inv;
	}
	
	private static CardboardBox pack(ItemStack itm){
		if(itm != null){
			return new CardboardBox(itm);
		} else {
			return null;
		}
	}
	private static ItemStack unpack(CardboardBox box){
		if(box != null){
			return box.unbox();
		} else {
			return null;
		}
	}
	private static Inventory getInventory(int length, InventoryType type){
		if(type != null){
			return Bukkit.createInventory(null, type);
		} else {
			return Bukkit.createInventory(null, length);
		}
	}
}
