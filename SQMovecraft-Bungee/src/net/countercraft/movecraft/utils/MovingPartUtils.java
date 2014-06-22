package net.countercraft.movecraft.utils;

import java.io.File;
import java.io.IOException;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class MovingPartUtils {
	public static File dir = new File(Movecraft.getInstance().getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParentFile().getParentFile() + "/movingpart");
	public static boolean saveSchematic(Sign s, CuboidRegion r, WorldEditPlugin p, Player plr, boolean onState){
		Vector min = r.getMinimumPoint();
		Vector max = r.getMaximumPoint();
		Block b = s.getBlock();
		Vector pos = new Vector(b.getX(), b.getY(), b.getZ());
		
		CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1,1,1)), min, min.subtract(pos));
		EditSession session = new EditSession(new BukkitWorld(b.getWorld()), -1);
		clipboard.copy(session);
		WorldEdit we = p.getWorldEdit();
		LocalConfiguration config = we.getConfiguration();
		
		if(!dir.exists()){
			dir.mkdirs();
		}
		LocalPlayer lplr = p.wrapPlayer(plr);
		
		//dibujaron|wings|on.schematic
		String filename = (onState) ? (s.getLine(1) + "_" + s.getLine(2) + "_" + "on") : (s.getLine(1) + "_" + s.getLine(2) + "_" + "off");
		
		try {
		File f = we.getSafeSaveFile(lplr, dir, filename, "schematic", "schematic");
			SchematicFormat.MCEDIT.save(clipboard, f);
		} catch (IOException | FilenameException | DataException e) {
			plr.sendMessage("Something went wrong with the schematic saver: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean switchSchematic(Sign s, WorldEditPlugin wep, Player p, boolean initialState){
        Block b = s.getBlock();
        Vector signLoc = new Vector(b.getX(), b.getY(), b.getZ());
		WorldEdit we = wep.getWorldEdit();
		LocalConfiguration config = we.getConfiguration();
		LocalPlayer plr = wep.wrapPlayer(p);
		String fileName = (initialState) ? (s.getLine(1) + "_" + s.getLine(2) + "_" + "on") : (s.getLine(1) + "_" + s.getLine(2) + "_" + "off");
		if(!dir.exists()){
			dir.mkdirs();
		}
        File f;
        CuboidClipboard cc;
        
        //load the old schematic
        try {
			f = we.getSafeOpenFile(plr, dir, fileName, "schematic", "schematic");
			cc = SchematicFormat.MCEDIT.load(f);
		} catch (IOException | DataException | FilenameException e) {
			p.sendMessage("Something went wrong with the schematic loader: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
         
        
        //save a new copy of it
        EditSession session = new EditSession(new BukkitWorld(p.getWorld()), -1);
        Vector pos2 = new Vector(b.getX(), b.getY(), b.getZ());
        Vector newVector = pos2.add(cc.getOffset());
        cc.setOrigin(newVector);
        cc.copy(session);
        
        try {
    		f = we.getSafeSaveFile(plr, dir, fileName, "schematic", "schematic");
			SchematicFormat.MCEDIT.save(cc, f);
		} catch (IOException | FilenameException | DataException e) {
			p.sendMessage("Something went wrong with the schematic saver: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
        
        fileName = (initialState) ? (s.getLine(1) + "_" + s.getLine(2) + "_" + "off") : (s.getLine(1) + "_" + s.getLine(2) + "_" + "on");
        
        //load the new schematic
        try {
			f = we.getSafeOpenFile(plr, dir, fileName, "schematic", "schematic");
			cc = SchematicFormat.MCEDIT.load(f);
		} catch (IOException | DataException | FilenameException e) {
			p.sendMessage("Something went wrong with the schematic loader: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
        
        //paste it
        cc.setOrigin(newVector);
        //p.sendMessage("Pasting new: setting origin to:" + newVector.getX() + "," + newVector.getY() + "," + newVector.getZ());
    	try {
			cc.paste(session, signLoc, false);
		} catch (MaxChangedBlocksException e) {
			p.sendMessage("Something went wrong with the schematic paster: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
    	return true;
    }
	
	//precondition: sign known to be a moving part sign
	public static void rotateSchematic(Sign s, Player p, Rotation r){
		WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		WorldEdit we = wep.getWorldEdit();
		LocalConfiguration config = we.getConfiguration();
		LocalPlayer plr = wep.wrapPlayer(p);
		String fileNameOn = s.getLine(1) + "_" + s.getLine(2) + "_" + "on";
		String fileNameOff = s.getLine(1) + "_" + s.getLine(2) + "_" + "off";
		if(!dir.exists()){
			dir.mkdirs();
		}
        File f;
        CuboidClipboard cc;
        
        int rotation;
        if (r == Rotation.CLOCKWISE){
        	rotation = 90;
        } else {
        	rotation = -90;
        }

        //load the old schematic
        try {
			f = we.getSafeOpenFile(plr, dir, fileNameOn, "schematic", "schematic");
			cc = SchematicFormat.MCEDIT.load(f);
			
			cc.rotate2D(rotation);
			f = we.getSafeSaveFile(plr, dir, fileNameOn, "schematic", "schematic");
			SchematicFormat.MCEDIT.save(cc, f);
			
			f = we.getSafeOpenFile(plr, dir, fileNameOff, "schematic", "schematic");
			cc = SchematicFormat.MCEDIT.load(f);
			
			cc.rotate2D(rotation);			
    		f = we.getSafeSaveFile(plr, dir, fileNameOff, "schematic", "schematic");
			SchematicFormat.MCEDIT.save(cc, f);
			
		} catch (IOException | DataException | FilenameException e) {
			p.sendMessage("Something went wrong with the schematic rotator: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
