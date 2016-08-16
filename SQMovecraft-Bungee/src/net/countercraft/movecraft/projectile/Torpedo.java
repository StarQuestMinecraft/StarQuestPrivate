package net.countercraft.movecraft.projectile;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.event.CraftProjectileDetonateEvent;
import net.countercraft.movecraft.utils.DirectionUtils;
import net.countercraft.movecraft.utils.HPUtils;
import net.countercraft.movecraft.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Torpedo extends Projectile{
	
	Player plr;
	
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
				if(!LocationUtils.spaceCheck(p) && !LocationUtils.isInCorePlanet(p)){
					p.sendMessage("You can't fire TNT torpedoes on a rim planet.");
					return;
				}
				if(!p.hasPermission("movecraft.torpedo")){
					p.sendMessage("You don't have permission for this.");
					return;
				}
				ItemStack m = new ItemStack(Material.FIREWORK, 1);
				di.removeItem(m);
				new Torpedo(placeLoc, direction, p);
				return;
			} else if(di.contains(Material.REDSTONE)){
				if(!p.hasPermission("movecraft.emptorpedo")){
					p.sendMessage("You don't have permission for this.");
					return;
				}
				ItemStack m = new ItemStack(Material.REDSTONE, 1);
				di.removeItem(m);
				new InterdictionTorpedo(placeLoc, direction, p);
				return;
			}
			p.sendMessage("No ammo!");
			return;
		}
		p.sendMessage("Improperly built torpedo tube.");
	}
	public Torpedo(Block block, BlockFace direction, Player plr){
		super(block, direction);
		myTask.runTaskTimer(Movecraft.getInstance(), 3, 3);
		this.plr = plr;
	}
	
	@Override
	public void move(Block target){
		super.move(target);
		myBlock.getWorld().playEffect(myBlock.getLocation(), Effect.SMOKE, 20);
		myBlock.getWorld().playSound(myBlock.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 2.0F, 1.0F);
	}
	
	@Override
	public void detonate(){
		CraftProjectileDetonateEvent event = new CraftProjectileDetonateEvent(plr, myBlock);
		event.call();
		if(!event.isCancelled()){
			/*myBlock.setType(Material.AIR);
			final Player shooter = plr;
			Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				@Override
				public void run(){
					Craft c = CraftManager.getInstance().getCraftByPlayer(shooter);
					if(c != null){
						LaserBolt.createExplosion(myBlock, shooter, c.getType().getTorpedoPower());
					} else {
						LaserBolt.createExplosion(myBlock, shooter, 4.0f);
					}
				}
			}, 1L);*/
			
			myBlock.setType(Material.AIR);
			
			myBlock.getWorld().playEffect(myBlock.getLocation(), Effect.EXPLOSION_LARGE, 0);
			myBlock.getWorld().playSound(myBlock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
			
			double currentPower = HPUtils.getTorpedoPower();
			int stage = 1;
			
			Block centerBlock = myBlock.getRelative(myDirection);
			
			int i = 0;
			
			while (currentPower > 0 && stage <= 6 && i < 100) {

				i ++;
				
				List<Block> blocks = new ArrayList<Block>();
				
				if (stage == 1) {
					
					blocks.add(centerBlock);
					
				} else if (stage == 2) {
					
					blocks.add(myBlock);
					blocks.add(centerBlock.getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(DirectionUtils.getBlockFaceRight(myDirection)));
					blocks.add(centerBlock.getRelative(DirectionUtils.getBlockFaceLeft(myDirection)));
					blocks.add(centerBlock.getRelative(myDirection));
					
				} else if (stage == 3) {
					
					blocks.add(centerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP));
					
					blocks.add(centerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN));
					
					blocks.add(centerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH));
					
				} else if (stage == 4) {
					
					blocks.add(centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH));
					
					blocks.add(centerBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH));
					
				} else if (stage == 5) {
					
					blocks.add(centerBlock.getRelative(BlockFace.EAST, 2));
					blocks.add(centerBlock.getRelative(BlockFace.WEST, 2));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH, 2));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH, 2));
					blocks.add(centerBlock.getRelative(BlockFace.UP, 2));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN, 2));
					
				} else if (stage == 6) {
					
					blocks.add(centerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.NORTH));
					
					blocks.add(centerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.NORTH));
					
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.WEST));
					blocks.add(centerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.EAST));
					
					blocks.add(centerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.UP));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.DOWN));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.WEST));
					blocks.add(centerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.EAST));
					
					blocks.add(centerBlock.getRelative(BlockFace.UP, 2).getRelative(BlockFace.EAST));
					blocks.add(centerBlock.getRelative(BlockFace.UP, 2).getRelative(BlockFace.WEST));
					blocks.add(centerBlock.getRelative(BlockFace.UP, 2).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.UP, 2).getRelative(BlockFace.NORTH));
					
					blocks.add(centerBlock.getRelative(BlockFace.DOWN, 2).getRelative(BlockFace.EAST));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN, 2).getRelative(BlockFace.WEST));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN, 2).getRelative(BlockFace.SOUTH));
					blocks.add(centerBlock.getRelative(BlockFace.DOWN, 2).getRelative(BlockFace.NORTH));
					
				}

				double totalHP = 0;
				double leastHP = Integer.MAX_VALUE;
				
				List<Block> removeBlocks = new ArrayList<Block>();
				
				for (Block block : blocks) {
					
					if (block.getType().equals(Material.AIR)) {
						
						removeBlocks.add(block);
						
					} else {
						
						if (block.hasMetadata("hp")) {
							
							try {
								
								double currentHP = block.getMetadata("hp").get(0).asDouble();
								
								totalHP = totalHP + currentHP;
								
								if (currentHP < leastHP) {
									
									leastHP = currentHP;
									
								}
								
							} catch (Exception e) {
								
								e.printStackTrace();
								
							}
							
						} else {
							
							double currentHP = HPUtils.getHP(block.getType());
							
							if (currentHP == -1) {
								
								return;
								
							}
							
							totalHP = totalHP + currentHP;
							
							if (currentHP < leastHP) {
								
								leastHP = currentHP;
								
							}
							
						}
						
					}

				}
				
				blocks.removeAll(removeBlocks);
				
				if (blocks.size() == 0) {
					
					return;
					
				}

				if (totalHP - currentPower <= 0) {
					
					Fireball fireball = (Fireball) plr.getWorld().spawnEntity(centerBlock.getLocation(), EntityType.FIREBALL);
					fireball.setYield(0.0f);
					fireball.setShooter(plr);
					
					List<Block> eventBlocks = new ArrayList<Block>();
					eventBlocks.addAll(blocks);
					
					EntityExplodeEvent explosionEvent = new EntityExplodeEvent(fireball,centerBlock.getLocation(), eventBlocks, 0.0f);
					Bukkit.getServer().getPluginManager().callEvent(explosionEvent);
					
					fireball.remove();
					
					if (!explosionEvent.isCancelled()) {
						
						for (Block block : blocks) {
							
							block.setType(Material.AIR);
							block.removeMetadata("hp", Movecraft.getInstance());
							
						}
						
						currentPower = currentPower - totalHP;
						
						stage = stage + 1;
						
					} else {
						
						currentPower = 0;
						
					}

				} else {
					
					double power;
					
					if (currentPower / blocks.size() > leastHP) {
						
						power = leastHP;

					} else {
						
						power = currentPower / blocks.size();
						
					}
					
					Fireball fireball = (Fireball) plr.getWorld().spawnEntity(centerBlock.getLocation(), EntityType.FIREBALL);
					fireball.setYield(0.0f);
					fireball.setShooter(plr);
					
					List<Block> eventBlocks = new ArrayList<Block>();
					eventBlocks.addAll(blocks);
					
					EntityExplodeEvent explosionEvent = new EntityExplodeEvent(fireball,centerBlock.getLocation(), eventBlocks, 0.0f);
					Bukkit.getServer().getPluginManager().callEvent(explosionEvent);
					
					fireball.remove();
					
					if (!explosionEvent.isCancelled()) {
						
						currentPower = currentPower - (power * blocks.size());
						
						for (Block block : blocks) {
							
							double currentHP;
							
							if (block.hasMetadata("hp")) {
								
								currentHP = block.getMetadata("hp").get(0).asDouble();
								
							} else {
								
								currentHP = HPUtils.getHP(block.getType());
								
							}
							
							if (power >= currentHP) {
								
								block.setType(Material.AIR);
								block.removeMetadata("hp", Movecraft.getInstance());
								
							} else {
								
								block.setMetadata("hp", new FixedMetadataValue(Movecraft.getInstance(), currentHP - power));
								
							}

						}
						
					} else {
						
						currentPower = 0;
						
					}
					
					//currentPower = 0;
					
					//block.setMetadata("hp", new FixedMetadataValue(Movecraft.getInstance(), currentHp - currentPower));
					
				}
				
			}
			
		}
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
