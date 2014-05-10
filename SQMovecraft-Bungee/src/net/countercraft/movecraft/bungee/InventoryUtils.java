package net.countercraft.movecraft.bungee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
 












import net.countercraft.movecraft.utils.external.CardboardBox;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtils {
	
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
	public static Inventory readInventory(DataInputStream ips){
		int length = 27;
		try{ length = ips.readInt(); } catch (Exception e){ e.printStackTrace();};
		Inventory inv = getInventory(length);
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
	public static void writePlayerInventory(DataOutputStream os, PlayerInventory inventory){
		try {
			ObjectOutputStream out = new ObjectOutputStream(os);
			ItemStack[] is = inventory.getContents();
			CardboardBox[] cardboardBoxes = new CardboardBox[is.length + 4];
			//do their armor first
			
			cardboardBoxes[0] = pack(inventory.getHelmet());
			cardboardBoxes[1] = pack(inventory.getChestplate());
			cardboardBoxes[2] = pack(inventory.getLeggings());
			cardboardBoxes[3] = pack(inventory.getBoots());
			
			for(int i = 0; i < is.length; i++){
				cardboardBoxes[i+4] = pack(is[i]);
			}
			out.writeObject(cardboardBoxes);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	public static ItemStack[] readPlayerInventory(DataInputStream ips){
		ItemStack[] data = new ItemStack[40];
		try {
			ObjectInputStream in = new ObjectInputStream(ips);
			CardboardBox[] cardboardBoxes = (CardboardBox[]) in.readObject();
			in.close();
			data[0] = (unpack(cardboardBoxes[0]));
			data[1] = (unpack(cardboardBoxes[1]));
			data[2] = (unpack(cardboardBoxes[2]));
			data[3] = (unpack(cardboardBoxes[3]));
			for(int i = 4; i < cardboardBoxes.length; i++){
				data[i] = unpack(cardboardBoxes[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	public static void applyInventory(ItemStack[] items, Player p){
		PlayerInventory inv = p.getInventory();
		inv.setHelmet(items[0]);
		inv.setChestplate(items[1]);
		inv.setLeggings(items[2]);
		inv.setBoots(items[3]);
		ItemStack[] invcontents = Arrays.copyOfRange(items, 4, items.length);
		inv.setContents(invcontents);
	}
   
   /* public static Inventory StringToInventory (String invString)
    {
        String[] serializedBlocks = invString.split(";");
        String invInfo = serializedBlocks[0];
        Inventory deserializedInventory = Bukkit.getServer().createInventory(null, Integer.valueOf(invInfo));
       
        for (int i = 1; i < serializedBlocks.length; i++)
        {
            String[] serializedBlock = serializedBlocks[i].split("#");
            int stackPosition = Integer.valueOf(serializedBlock[0]);
           
            if (stackPosition >= deserializedInventory.getSize())
            {
                continue;
            }
           
            ItemStack is = null;
            Boolean createdItemStack = false;
           
            String[] serializedItemStack = serializedBlock[1].split(":");
            for (String itemInfo : serializedItemStack)
            {
                String[] itemAttribute = itemInfo.split("@");
                if (itemAttribute[0].equals("t"))
                {
                    is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
                    createdItemStack = true;
                }
                else if (itemAttribute[0].equals("d") && createdItemStack)
                {
                    is.setDurability(Short.valueOf(itemAttribute[1]));
                }
                else if (itemAttribute[0].equals("a") && createdItemStack)
                {
                    is.setAmount(Integer.valueOf(itemAttribute[1]));
                }
                else if (itemAttribute[0].equals("e") && createdItemStack)
                {
                    is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
                }
            }
            deserializedInventory.setItem(stackPosition, is);
        }
       
        return deserializedInventory;
    }*/
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
	private static Inventory getInventory(int length){
		InventoryType type = getInventoryType(length);
		if(type != null){
			return Bukkit.createInventory(null, type);
		} else {
			System.out.println("LENGTH: " + length);
			return Bukkit.createInventory(null, length);
		}
	}
	
	private static InventoryType getInventoryType(int length){
		switch(length){
		case 3: return InventoryType.FURNACE;
		case 4: return InventoryType.BREWING;
		case 5: return InventoryType.HOPPER;
		default: return null;
		}
	}
}
