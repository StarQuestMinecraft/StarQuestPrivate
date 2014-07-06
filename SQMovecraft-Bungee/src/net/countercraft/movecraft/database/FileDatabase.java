package net.countercraft.movecraft.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.bungee.LocAndBlock;

import org.bukkit.Location;

public class FileDatabase implements StarshipDatabase {
	
	private static File folder = new File(Movecraft.getInstance().getDataFolder().getAbsoluteFile().
			  getParentFile().getParentFile().getParentFile().getParentFile() +
			  "/savedstarships");

	@Override
	public StarshipData getStarshipByLocation(Location l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeStarshipAtLocation(Location l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveStarshipDataAtLocation(Location l, StarshipData d) {
		// TODO Auto-generated method stub
		
	}
	
	private static byte[] serialize(StarshipData d) throws IOException{
		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		msgout.writeUTF(d.getName());
		msgout.writeUTF(d.getType());
		msgout.writeUTF(d.getCaptain().toString());
		msgout.writeInt(d.getMembers().size());
		for(UUID u : d.getMembers()){
			msgout.writeUTF(u.toString());
		}
		msgout.writeInt(d.getPilots().size());
		for(UUID u : d.getPilots()){
			msgout.writeUTF(u.toString());
		}
		msgout.writeInt(d.getLBBA().length);
		for(LocAndBlock b : d.getLBBA()){
			
		}
		return msgbytes.toByteArray();
	}
	
	private static StarshipData unserialize(byte[] b){
		DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(b));
		return null;
		//TODO
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
		return l.getWorld() + "@" + l.getX() + "," + l.getY() + "," + l.getZ();
	}
}
