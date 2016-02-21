package net.countercraft.movecraft.projectile;
import net.countercraft.movecraft.event.CraftProjectileDetonateEvent;
import net.countercraft.movecraft.utils.EMPUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class InterdictionTorpedo extends Torpedo{
	public InterdictionTorpedo(Block block, BlockFace direction, Player plr) {
		super(block, direction, plr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void detonate(){
		CraftProjectileDetonateEvent event = new CraftProjectileDetonateEvent(super.plr, myBlock);
		event.call();
		if(!event.isCancelled()){
			myTask.cancel();
			myBlock.setType(Material.AIR);
			Block hit = super.myBlock.getRelative(super.myDirection);
			EMPUtils.detonateEMP(hit);
		}
		
	}

	@Override
	protected Material getMyBlockType(){
		return Material.WEB;
	}
}
