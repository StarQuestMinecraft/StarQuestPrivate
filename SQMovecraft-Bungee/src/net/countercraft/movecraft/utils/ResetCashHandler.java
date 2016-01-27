package net.countercraft.movecraft.utils;

import java.io.File;

import net.countercraft.movecraft.Movecraft;

public class ResetCashHandler {
	
	public static void run(){
		File f = new File(Movecraft.getInstance().getDataFolder().getPath() + "/money.txt");
		if(!f.exists()){
			System.out.println("File does not exist, quitting!");
			return;
		}
	}
	
}
