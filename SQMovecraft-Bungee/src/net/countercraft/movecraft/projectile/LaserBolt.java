package net.countercraft.movecraft.projectile;

import java.util.HashSet;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LaserBolt extends Projectile{
	
	private static HashSet<Block> blocks = new HashSet<Block>();
	
	private byte myData = 0;
	public LaserBolt(Block block, BlockFace direction) {
		super(block, direction);
		blocks.add(block);
		System.out.println("Blocks active in world: " + blocks.size());
		BlockFace behind = getOppsoiteBlockFace(direction);
		Block behind4 = block.getRelative(behind).getRelative(behind).getRelative(behind).getRelative(behind);
		if(behind4.getType() == Material.WOOL){
			myData = behind4.getData();
		} else {
			myData = 0;
		}
		
		myTask.runTaskTimer(Movecraft.getInstance(), 1, 1);
	}
	
	@Override
	public void move(Block target){
		blocks.remove(myBlock);
		blocks.add(target);
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
	
	@Override
	public void removeData(){
		blocks.remove(myBlock);
	}
	
	//region info __global__ -w 
	
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
	
	@Override
	protected Material getMyBlockType(){
		return Material.STAINED_GLASS;
	}
	
	@Override
	protected byte getMyData(){
		return myData;
	}
	
	public static HashSet<Block> getBoltBlocks(){
		return blocks;
	}
}
