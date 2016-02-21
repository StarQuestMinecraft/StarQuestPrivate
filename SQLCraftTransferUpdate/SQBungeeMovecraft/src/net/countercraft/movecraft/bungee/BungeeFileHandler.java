package net.countercraft.movecraft.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.MovecraftLocation;

@Deprecated
public class BungeeFileHandler {
	
	private static int[] TRANSFER_ILLEGALS = new int[]{
		41,
		42,
		57,
		92,
		116,
		117,
		130,
		133,
		145,
		152,
		154
	};
	
	private static File folder = new File(Movecraft.getInstance().getDataFolder().getAbsoluteFile().
										  getParentFile().getParentFile().getParentFile().getParentFile() +
										  "/starships");
	public static File transferFolder = new File(Movecraft.getInstance().getDataFolder().getAbsoluteFile().
			  getParentFile().getParentFile().getParentFile().getParentFile() +
			  "/transferStarships");
	//public static File transferFolder = Movecraft.getInstance().getDataFolder().getAbsoluteFile();
	
	static{
		if(!transferFolder.exists()){
			transferFolder.mkdirs();
		}
	}
	
	public static void saveCraftBytes(byte[] craftdata, String pilot, File folder){
		System.out.println("Folder: " + folder.getAbsolutePath());
		File target = new File(folder + "/" + pilot + ".starship");
		System.out.println("Target: " + target.getAbsolutePath());
		if(target.exists()){
			target.delete();
		}
		try{
			target.createNewFile();
			FileOutputStream out = new FileOutputStream(target);
			out.write(craftdata);
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void saveCraftBytes(byte[] craftData, String pilot){
		saveCraftBytes(craftData, pilot, folder);
	}
	
	public static byte[] readCraftBytes(String pilot){
		return readCraftBytes(pilot, folder);
	}
	public static byte[] readCraftBytes(String pilot, File folder){
		File target = new File(folder + "/" + pilot + ".starship");
		if(!target.exists()){
			System.out.println("ERROR: File does not exist!: " + target.toString());
			return null;
		}
		byte[] inbytes = null;
		try{
			FileInputStream in = new FileInputStream(target);
			inbytes = new byte[in.available()];
			in.read(inbytes);
			in.close();
		} catch (Exception e){
			System.out.println("crash possibility 2");
			e.printStackTrace();
		}
		return inbytes;
	}
	
	public static void deleteTransferFile(String pilot){
		File target = new File(transferFolder + "/" + pilot + ".starship");
		if(target.exists()){
			target.delete();
		}
	}

	public static boolean transferScanForIllegal(Craft craft){
		for(MovecraftLocation loc : craft.getBlockList()){
			int id = craft.getW().getBlockTypeIdAt(loc.getX(), loc.getY(), loc.getZ());
			for(int i : TRANSFER_ILLEGALS){
				if(id == i){
					Player p = craft.pilot;
					if(p != null){
						p.sendMessage("Illegal block of type " + id + " at coordinates " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ".");
					}
					return false;
				}
			}
		}
		return true;
	}
}
