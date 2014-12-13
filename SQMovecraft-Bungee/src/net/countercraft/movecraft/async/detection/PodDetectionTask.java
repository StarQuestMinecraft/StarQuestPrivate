package net.countercraft.movecraft.async.detection;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.BoundingBoxUtils;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.World;

public class PodDetectionTask extends DetectionTask {

	public PodDetectionTask(Craft c, MovecraftLocation startLocation, int minSize, int maxSize, Integer[] allowedBlocks, Integer[] forbiddenBlocks, String player, World w) {
		super(c, startLocation, minSize, maxSize, allowedBlocks, forbiddenBlocks, player, w);
	}

	@Override
	public void execute() {
		for (int y = startLocation.getY() - 1; y < startLocation.getY() + 3; y++) {
			for (int x = startLocation.getX() - 1; x < startLocation.getX() + 2; x++) {
				for (int z = startLocation.getZ() - 1; z < startLocation.getZ() + 2; z++) {
					MovecraftLocation workingLocation = new MovecraftLocation(x,y,z);
					int id = data.getWorld().getBlockTypeIdAt(x,y,z);
					if(isAcceptableId(id)){
						addToBlockList(workingLocation);
						addToBlockCount(id);
						calculateBounds(workingLocation);
						if (id == 63 || id == 68) {
							signLocations.add(workingLocation);
						}
					} else {
						fail("Disallowed blocks found on pod!");
					}
				}
			}
		}
		data.setBlockList(finaliseBlockList(blockList, minY, maxY));
		data.setSignLocations(signLocations);
		data.setHitBox(BoundingBoxUtils.formBoundingBox(data.getBlockList(), data.getMinX(), maxX, data.getMinZ(), maxZ));
	}

	private boolean isAcceptableId(int id) {
		return id == 0 || id == 35 || id == 68 || id == 95 || id == 64;
	}
}
