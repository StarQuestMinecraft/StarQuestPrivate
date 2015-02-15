package net.countercraft.movecraft.projectile;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.utils.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Torpedo extends Projectile{
	
	public static void testAndLaunch(Sign sign, Player p){
		BlockFace direction = getFacingBlockFace(sign);
		Block oneForward = sign.getBlock().getRelative(direction);
		Block twoForward = oneForward.getRelative(direction);
		Block dispenser = twoForward.getRelative(direction);
		if (oneForward.getType() == Material.SPONGE && twoForward.getType() == Material.SPONGE && dispenser.getType() == Material.DISPENSER){
			Dispenser d = (Dispenser) dispenser.getState();
			Inventory di = d.getInventory();
			di.setMaxStackSize(1);
			Block placeLoc = dispenser.getRelative(direction);
			if(placeLoc.getType() != Material.AIR) return;
			if(di.contains(Material.FIREWORK)){
				if(!LocationUtils.spaceCheck(p)){
					p.sendMessage("You can't fire TNT torpedoes on a planet.");
					return;
				}
				ItemStack m = new ItemStack(Material.FIREWORK, 1);
				di.removeItem(m);
				new Torpedo(placeLoc, direction);
				return;
			} else if(di.contains(Material.REDSTONE)){
				ItemStack m = new ItemStack(Material.REDSTONE, 1);
				di.removeItem(m);
				new InterdictionTorpedo(placeLoc, direction);
				return;
			}
			p.sendMessage("No ammo!");
			return;
		}
		p.sendMessage("Improperly built torpedo tube.");
	}
	public Torpedo(Block block, BlockFace direction){
		super(block, direction);
		myTask.runTaskTimer(Movecraft.getInstance(), 3, 3);
	}
	
	public void move(Block target){
		super.move(target);
		myBlock.getWorld().playEffect(myBlock.getLocation(), Effect.SMOKE, 20);
		myBlock.getWorld().playSound(myBlock.getLocation(), Sound.FIREWORK_LAUNCH, 2.0F, 1.0F);
	}
	
	public void detonate(){
		myBlock.setType(Material.AIR);
		TNTPrimed tnt = myBlock.getWorld().spawn(myBlock.getLocation(), TNTPrimed.class);
		tnt.setFuseTicks(1);
		tnt.setIsIncendiary(true);
	}
	@SuppressWarnings("deprecation")
	public static BlockFace getFacingBlockFace(Sign s){
		Block sBlock = s.getBlock();
		int data = sBlock.getData();
		switch(data){
			case 2:
				return BlockFace.SOUTH;
			case 3:
				return BlockFace.NORTH;
			case 4:
				return BlockFace.EAST;
			case 5:
				return BlockFace.WEST;
			default:
				return null;
		}
	}
}
