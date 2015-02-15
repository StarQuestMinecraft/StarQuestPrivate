package net.countercraft.movecraft.shield;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PendingActivation {
	String name;
	World w;
	BlockVector min;
	BlockVector max;
	public String pilot;
	List<String> members;
	Block sign;

	public PendingActivation(World w, BlockVector min, BlockVector max, String name, String pilot, ArrayList<String> members, Block sign) {
		this.name = name;
		this.w = w;
		this.min = min;
		this.max = max;
		this.sign = sign;
		this.pilot = pilot;
		this.members = members;
	}

	public void activate(boolean succeed) {
		Player p = Bukkit.getPlayer(pilot);
		if (!succeed) {
			if (p != null) {
				p.sendMessage(ChatColor.RED + "Your shield failed to deploy, its warmup cycle was interrupted.");
				if (sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST) {
					Sign s = (Sign) sign.getState();
					if (ShieldUtils.isShieldSign(s)) {
						/*
						 * s.setLine(1, DISABLED); s.setLine(2, ""); s.update();
						 */
					}
				}
			}
		} else {
			ProtectedRegion reg = new ProtectedCuboidRegion(name, min, max);
			RegionManager rm = ShieldUtils.wg.getRegionManager(w);
			ProtectedRegion remove = rm.getRegion(name);
			if (remove != null) {
				rm.removeRegion(name);
				if (p != null && p.isOnline()) {
					p.sendMessage(ChatColor.RED + "An existing starship shield was removed; you cannot have two shields in the same world.");
				}
			}
			/*
			 * if(sign.getType() == Material.WALL_SIGN || sign.getType() ==
			 * Material.SIGN_POST){ Sign s = (Sign) sign.getState();
			 * if(isShieldSign(s)){ s.setLine(1, ENABLED); s.setLine(2,
			 * CryoSpawn.signTrim(pilot)); s.update(); } }
			 */
			ApplicableRegionSet set = rm.getApplicableRegions(reg);
			for (ProtectedRegion r : set) {
				// TODO check for all dock regions
			}
			rm.addRegion(reg);
			DefaultDomain members = new DefaultDomain();
			ShieldUtils.fill(members, this.members);
			reg.setMembers(members);
			ShieldUtils.setRegionFlag(reg, DefaultFlag.OTHER_EXPLOSION, "deny");
			ShieldUtils.setRegionFlag(reg, DefaultFlag.TNT, "deny");
			ShieldUtils.setRegionFlag(reg, DefaultFlag.CREEPER_EXPLOSION, "deny");
			if (p != null && p.isOnline()) {
				p.sendMessage(ChatColor.GREEN + "Your starship's shield is fully warmed up and is providing full protection.");
			}
			ShieldUtils.saveRM(rm);
		}
	}
}