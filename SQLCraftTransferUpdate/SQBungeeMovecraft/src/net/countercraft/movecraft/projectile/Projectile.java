package net.countercraft.movecraft.projectile;

import net.countercraft.movecraft.task.ProjectileFlyTask;
import net.countercraft.movecraft.utils.GunUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class Projectile {
	
	Block myBlock;
	ProjectileFlyTask myTask;
	BlockFace myDirection;
	long myStartTime;
	
	public Projectile(Block block, BlockFace direction){
		myStartTime = System.currentTimeMillis();
		myBlock = block;
		myBlock.setTypeIdAndData(getMyBlockType().getId(), getMyData(), false);
		myTask = new ProjectileFlyTask(this);
		myDirection = direction;
	}
	
	
	public void taskMove(){
		//if it's been flying longer than 10 seconds nuke it.
		if(System.currentTimeMillis() - myStartTime > 10000){
			myTask.cancel();
			detonate();
			return;
		}
		Block target = myBlock.getRelative(myDirection);
		
		//if I'm not the type I should be, something is wrong, cancel.
		if(myBlock.getType() != getMyBlockType()){
			myTask.cancel();
			removeData();
			return;
		}
		
		//if I'm in spawn, cancel.
		if(GunUtils.isInSpawn(myBlock.getLocation())){
			myTask.cancel();
			myBlock.setType(Material.AIR);
			myBlock.setData((byte) 0);
			removeData();
			return;
		}
		//if the target block isn't air, it must have hit something, so detonate.
		if (target.getType() != Material.AIR){
			myTask.cancel();
			removeData();
			detonate();
			return;
		}
		move(target);
	}
	public void move(Block target){
		target.setType(getMyBlockType());
		target.setData(getMyData());
		myBlock.setType(Material.AIR);
		myBlock.setData((byte) 0);
		myBlock = target;
	}
	protected Material getMyBlockType(){
		return Material.TNT;
	}
	protected byte getMyData(){
		return 0;
	}
	protected void removeData(){
		
	}
	public abstract void detonate();
}
