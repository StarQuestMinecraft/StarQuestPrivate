package com.dibujaron.cardboardbox.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardMetaBanner implements CardboardItemMeta, Serializable {

	private static final long serialVersionUID = 7679865680958983363L;
	private int id;
	private List<Map<String, Object>> patterns;
	private byte baseColorData;

	public CardboardMetaBanner(ItemStack banner) {

		BannerMeta meta = (BannerMeta) banner.getItemMeta();
		List<Map<String, Object>> patterns = new ArrayList<Map<String, Object>>();
		
		for(Pattern pat : meta.getPatterns()) {
			
			Map<String, Object> pattern = pat.serialize();
			patterns.add(pattern);
			
		}
		
		this.patterns = patterns;
		this.id = banner.getTypeId();
		this.baseColorData = meta.getBaseColor().getData();
		
	}

	public ItemMeta unbox() {

		BannerMeta meta = (BannerMeta) new ItemStack(this.id).getItemMeta();
		
		List<Pattern> patterns = new ArrayList<Pattern>();
		
		for(Map<String, Object> list : this.patterns) {
			
			Pattern pattern = new Pattern(list);
			patterns.add(pattern);
			
		}
		meta.setPatterns(patterns);
		meta.setBaseColor(DyeColor.getByData(baseColorData));
		
		return meta;
	}
	
	
	
}
