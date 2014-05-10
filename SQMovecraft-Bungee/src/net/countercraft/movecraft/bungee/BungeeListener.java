package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.items.StorageChestItem;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.MovecraftLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeListener implements PluginMessageListener{
	
	public BungeeListener(){
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message){
		 if (!channel.equals("BungeeCord")){
			 return;
		 }
		 DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
		 try{
		 System.out.println("Recieved message");
		 String subchannel = in.readUTF();
	        if (subchannel.equals("movecraftWarp")) {
	            BungeeCraftReciever.recieveCraft(in);
	        } else if(subchannel.equals("movecraftPlayer")) {
	        	BungeePlayerHandler.recievePlayer(in);
	        } else if(subchannel.equals("UUIDOther")){
	        	System.out.println(in.readUTF() + ": " + in.readUTF());
	        }
		 } catch (Exception e){
			 e.printStackTrace();
			 return;
		 }
	}
}
