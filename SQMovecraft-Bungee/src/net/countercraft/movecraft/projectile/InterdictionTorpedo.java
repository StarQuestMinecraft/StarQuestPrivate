package net.countercraft.movecraft.projectile;
import java.util.Arrays;
import java.util.List;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class InterdictionTorpedo extends Torpedo{
	
	private final static String[] explodesigns = {ChatColor.BLUE + "AUTOPILOT", "\\  ||  /"};
	private static final List<String> EXPLODE_SIGNS = Arrays.asList(explodesigns);
	public InterdictionTorpedo(Block block, BlockFace direction) {
		super(block, direction);
		super.myBlockType = Material.WEB;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void detonate(){
		myTask.cancel();
		myBlock.setType(Material.AIR);
		smoke(myBlock);
		Block hit = super.myBlock.getRelative(super.myDirection);
		MovecraftLocation bloc = new MovecraftLocation(hit.getX(), hit.getY(), hit.getZ());
		Craft[] craftsInWorld = CraftManager.getInstance().getCraftsInWorld(hit.getWorld());
		if(craftsInWorld != null){
			for(Craft c : craftsInWorld){
				MovecraftLocation[] blocks = c.getBlockList();
				for(MovecraftLocation l : blocks){
					if(bloc.equals(l)){
						emp(blocks, c);
						return;
					}
				}
			}
		}
	}
	
	//sends an EMP charge through a ship
	private void emp(MovecraftLocation[] blocks, Craft c){
		// first remove the craft
		c.pilot.sendMessage(ChatColor.RED + "Your ship has been hit by an EMP!");
		CraftManager.getInstance().removeCraft(c);
		World w = c.getW();
		for(MovecraftLocation l : blocks){
			Block b = w.getBlockAt(l.getX(), l.getY(), l.getZ());
			if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
				Sign s = (Sign) b.getState();
				String l1 = s.getLine(0);
				if(InteractListener.getCraftTypeFromString(l1) != null || EXPLODE_SIGNS.contains(l1)){
					nukeBlock(b);
				}
			} else if(Math.random() < 0.25){
				smoke(b);
			}
		}
		
	}
	
	private void smoke(Block b){
		b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 0);
		b.getWorld().playSound(b.getLocation(), Sound.EXPLODE, 10, 5);
	}
	
	private void nukeBlock(Block b){
		b.breakNaturally();
		smoke(b);
	}
}
