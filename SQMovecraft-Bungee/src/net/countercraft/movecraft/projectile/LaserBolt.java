package net.countercraft.movecraft.projectile;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LaserBolt extends Projectile{
	
	public LaserBolt(Block block, BlockFace direction) {
		super(block, direction);
		super.myBlockType = Material.STAINED_GLASS;
		BlockFace behind = getOppsoiteBlockFace(direction);
		Block behind4 = block.getRelative(behind).getRelative(behind).getRelative(behind).getRelative(behind);
		if(behind4.getType() == Material.WOOL){
			super.myData = behind4.getData();
		} else {
			super.myData = 0;
		}
		
		myTask.runTaskTimer(Movecraft.getInstance(), 1, 1);
	}
	
	@Override
	public void move(Block target){
		super.move(target);
		myBlock.getWorld().playEffect(myBlock.getLocation(), Effect.MOBSPAWNER_FLAMES, 20);
		myBlock.getWorld().playSound(myBlock.getLocation(), Sound.SPLASH, 2.0F, 1.0F);
	}
	
	@Override
	public void detonate(){
		myBlock.setType(Material.AIR);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				myBlock.getWorld().createExplosion(myBlock.getLocation(), 2.0F);
			}
		}, 1L);
	}
	
	private BlockFace getOppsoiteBlockFace(BlockFace face){
		switch(face){
		case NORTH:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.NORTH;
		case EAST:
			return BlockFace.WEST;
		case WEST:
			return BlockFace.EAST;
		default:
			return BlockFace.SELF;
		}
	}
}
