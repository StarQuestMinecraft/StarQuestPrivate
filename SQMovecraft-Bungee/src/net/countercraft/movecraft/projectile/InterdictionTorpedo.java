package net.countercraft.movecraft.projectile;
import net.countercraft.movecraft.utils.EMPUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class InterdictionTorpedo extends Torpedo{
	public InterdictionTorpedo(Block block, BlockFace direction) {
		super(block, direction);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void detonate(){
		myTask.cancel();
		myBlock.setType(Material.AIR);
		Block hit = super.myBlock.getRelative(super.myDirection);
		EMPUtils.detonateEMP(hit);
		
	}

	@Override
	protected Material getMyBlockType(){
		return Material.WEB;
	}
}
