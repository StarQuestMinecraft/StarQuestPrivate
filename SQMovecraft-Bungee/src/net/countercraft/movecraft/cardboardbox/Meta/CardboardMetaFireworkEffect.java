
package net.countercraft.movecraft.cardboardbox.Meta;

import java.io.Serializable;

import net.countercraft.movecraft.cardboardbox.Utils.CardboardFireworkEffect;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardMetaFireworkEffect implements CardboardItemMeta, Serializable {

	private static final long serialVersionUID = -738623056624942344L;
	private int id;
	private CardboardFireworkEffect effect;

	public CardboardMetaFireworkEffect(ItemStack firework) {

		this.id = firework.getTypeId();
		FireworkEffectMeta meta = (FireworkEffectMeta) firework.getItemMeta();
		this.effect = new CardboardFireworkEffect(meta.getEffect());
	}

	public ItemMeta unbox() {

		FireworkEffectMeta meta = (FireworkEffectMeta) new ItemStack(this.id).getItemMeta();
		meta.setEffect(this.effect.unbox());
		return meta;
	}
}
