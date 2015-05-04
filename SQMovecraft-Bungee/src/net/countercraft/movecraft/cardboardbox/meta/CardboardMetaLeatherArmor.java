
package net.countercraft.movecraft.cardboardbox.meta;

import java.io.Serializable;

import net.countercraft.movecraft.cardboardbox.utils.CardboardColor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class CardboardMetaLeatherArmor implements CardboardItemMeta, Serializable {

	private static final long serialVersionUID = 6469379988073713423L;
	private CardboardColor color;
	private int id;

	public CardboardMetaLeatherArmor(ItemStack leather) {

		this.id = leather.getTypeId();
		LeatherArmorMeta meta = (LeatherArmorMeta) leather.getItemMeta();
		this.color = new CardboardColor(meta.getColor());
	}

	public ItemMeta unbox() {

		LeatherArmorMeta meta = (LeatherArmorMeta) new ItemStack(this.id).getItemMeta();
		meta.setColor(color.unbox());
		return meta;
	}
}
