package net.countercraft.movecraft.projectile;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class Projectile {
	
	Block myBlock;
	ProjectileFlyTask myTask;
	BlockFace myDirection;
	long myStartTime;
	Material myBlockType = Material.TNT;
	byte myData = 0;
	
	public Projectile(Block block, BlockFace direction){
		myStartTime = System.currentTimeMillis();
		myBlock = block;
		myTask = new ProjectileFlyTask(this);
		myDirection = direction;
	}
	
	
	void taskMove(){
		//if it's been flying longer than 10 seconds nuke it.
		if(System.currentTimeMillis() - myStartTime > 10000){
			myTask.cancel();
			detonate();
			return;
		}
		Block target = myBlock.getRelative(myDirection);
		
		//if the target block isn't air, it must have hit something, so detonate.
		if (target.getType() != Material.AIR){
			myTask.cancel();
			detonate();
			return;
		}
		move(target);
	}
	public void move(Block target){
		target.setType(myBlockType);
		target.setData(myData);
		myBlock.setType(Material.AIR);
		myBlock.setData((byte) 0);
		myBlock = target;
	}
	public abstract void detonate();
}
