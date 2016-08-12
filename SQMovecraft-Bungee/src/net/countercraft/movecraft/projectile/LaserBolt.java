package net.countercraft.movecraft.projectile;

import java.util.HashSet;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.event.CraftProjectileDetonateEvent;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class LaserBolt extends Projectile{
	
	private static HashSet<Block> blocks = new HashSet<Block>();
	
	static HashSet<LocationHit> recentExplosions = new HashSet<LocationHit>();
	Player shooter;
	private byte myData = 0;
	public LaserBolt(Block block, BlockFace direction, Player shooter) {
		super(block, direction);
		blocks.add(block);
		this.shooter = shooter;
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
		myBlock.getWorld().playSound(myBlock.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2.0F, 1.0F);
	}
	
	@Override
	public void detonate(){
		myBlock.setType(Material.AIR);
		CraftProjectileDetonateEvent event = new CraftProjectileDetonateEvent(shooter, myBlock);
		event.call();
		if(!event.isCancelled()){
			Bukkit.getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
				@Override
				public void run(){
					Craft c = CraftManager.getInstance().getCraftByPlayer(shooter);
					if(c != null){
						createExplosion(myBlock, shooter, c.getType().getLaserPower());
					} else {
						createExplosion(myBlock, shooter, 1.80f);
					}
				}
			}, 1L);
		}
	}
	
	@Override
	public void removeData(){
		blocks.remove(myBlock);
	}
	
	public static void createExplosion(Block b, Player shooter, float power){
		Location l = b.getLocation();
		l.add(l.getX() > 0 ? 0.5 : -0.5, 0.5, l.getZ() > 0 ? 0.5 : -0.5);
		recentExplosions.add(new LocationHit(l, shooter));
		l.getWorld().createExplosion(l, power);
		recentExplosions.remove(l);
		/*Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Movecraft.getInstance(), new Runnable(){
			public void run(){
				recentExplosions.remove(l);
			}
		}, 20L);*/
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

	public static LocationHit getClosestExplosion(Location me) {
		double minDist = Double.MAX_VALUE;
		LocationHit closest = null;
		for(LocationHit l : recentExplosions){
			double dist = l.distanceSquared(me);
			if(dist < minDist){
				closest = l;
			}
		}
		return closest;
	}
	
	public static class LocationHit{
		
		Location loc;
		Player plr;
		public LocationHit(Location l, Player p){
			loc = l;
			plr = p;
		}
		
		public Location getLocation(){
			return loc;
		}
		
		public Player getPlayer(){
			return plr;
		}
		
		
		public double distanceSquared(Location l){
			double dx = loc.getX() - l.getX();
			double dy = loc.getY() - l.getY();
			double dz = loc.getZ() - l.getZ();
			
			return dx * dx + dy * dy + dz * dz;
		}
	}
}
