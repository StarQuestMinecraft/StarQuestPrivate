package net.countercraft.movecraft.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.bukkit.entity.Player;
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
	        } else if(subchannel.equals("movecraftDeath")){
	        	BungeePlayerHandler.recievePlayerDeath(in);
	        } else if(subchannel.equals("UUIDOther")){
	        	System.out.println(in.readUTF() + ": " + in.readUTF());
	        }
		 } catch (Exception e){
			 e.printStackTrace();
			 return;
		 }
	}
}
