package net.countercraft.movecraft.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.async.detection.SaveableBlock;
import net.countercraft.movecraft.bungee.LocAndBlock;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class FileDatabase implements StarshipDatabase {
	
	private static File folder = new File(Movecraft.getInstance().getDataFolder().getAbsolutePath() +
			  "/savedstarships");
	
	static{
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	@Override
	public StarshipData getStarshipByLocation(Location l) {
		byte[] cbytes = readCraftBytes(l);
		if(cbytes == null) return null;
		StarshipData d;
		try {
			d = unserialize( cbytes);
		} catch (IOException e) {
			e.printStackTrace();
			d = null;
		}
		return d;
	}

	@Override
	public void removeStarshipAtLocation(Location l) {
		File target = new File(folder + "/" + locToString(l) + ".sdata");
		if(target.exists()){
			target.delete();
		}
	}

	@Override
	public void saveStarshipAtLocation(final Craft c) {
		final Location l = getCraftPilotSign(c);
		if(l == null){
			c.pilot.sendMessage("ERROR: could not save starship at location- could not find craft's main sign. Starship must be re-detected to fly again.");
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(Movecraft.getInstance(), new Runnable(){
			public void run(){
				StarshipData d = StarshipData.fromCraft(c);
				byte[] ser;
				try {
					ser = serialize(d);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ser = null;
				}
				saveBytes(ser, l);
			}
		});
	}
	
	private Location getCraftPilotSign(Craft c){
		for(MovecraftLocation l : c.getBlockList()){
			Block b = c.getW().getBlockAt(l.getX(), l.getY(), l.getZ());
			if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
				Sign s = (Sign) b.getState();
				String l1 = s.getLine(0);
				CraftType t = InteractListener.getCraftTypeFromString(l1);
				if(t != null && c.getType().equals(t)){
					return b.getLocation();
				}
			}
		}
		return null;
	}

	
	private static byte[] serialize(StarshipData d) throws IOException{
		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		msgout.writeUTF(d.getType());
		msgout.writeUTF(d.getCaptain().toString());
		msgout.writeInt(d.getBlockList().length);
		for(SaveableBlock b : d.getBlockList()){
			msgout.writeUTF(b.getWorld());
			msgout.writeInt(b.getX());
			msgout.writeInt(b.getY());
			msgout.writeInt(b.getZ());
			msgout.writeInt(b.getType());
			msgout.writeByte(b.getData());
			if(b.getType() == 63 || b.getType() == 68){
				msgout.writeUTF(b.line0);
				msgout.writeUTF(b.line1);
				msgout.writeUTF(b.line2);
				msgout.writeUTF(b.line3);
			}
		}
		return msgbytes.toByteArray();
	}
	
	private static StarshipData unserialize(byte[] b) throws IOException{
		try{
		DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(b));
		
		String ctype = msgin.readUTF();
		UUID captain = UUID.fromString(msgin.readUTF());
		int blocksLength = msgin.readInt();
		SaveableBlock[] blocksList = new SaveableBlock[blocksLength];
		for(int i = 0; i < blocksLength; i++){
			String world = msgin.readUTF();
			int x = msgin.readInt();
			int y = msgin.readInt();
			int z = msgin.readInt();
			int type = msgin.readInt();
			byte data = msgin.readByte();
			if(type == 63 || type == 68){
				try{
				String line0 = msgin.readUTF();
				String line1 = msgin.readUTF();
				String line2 = msgin.readUTF();
				String line3 = msgin.readUTF();
				blocksList[i] = new SaveableBlock(world,x,y,z,type,data,line0,line1,line2,line3);
				} catch (Exception e){
					blocksList[i] = new SaveableBlock(world,x,y,z,type,data);
				}
			} else {
				blocksList[i] = new SaveableBlock(world,x,y,z,type,data);
			}
		}
		return new StarshipData(blocksList, ctype, captain);
		} catch (Exception e){
			return null;
		}
	}
	private static void saveBytes(byte[] craftdata, Location l){
		File target = new File(folder + "/" + locToString(l) + ".sdata");
		System.out.println("Saved starship to " + target.toString());
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
	private static byte[] readCraftBytes(Location l){
		File target = new File(folder + "/" + locToString(l) + ".sdata");
		System.out.println("possibility 1");
		if(!target.exists()){
			System.out.println("ERROR: File does not exist!: " + target.toString());
			return null;
		}
		byte[] inbytes = null;
		try{
			FileInputStream in = new FileInputStream(target);
			System.out.println(in.available());
			inbytes = new byte[in.available()];
			in.read(inbytes);
			in.close();
		} catch (Exception e){
			System.out.println("crash possibility 2");
			e.printStackTrace();
		}
		System.out.println("Inbytes: " + inbytes);
		return inbytes;
	}
	private static String locToString(Location l){
		return l.getWorld().getName() + "@" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}
}
