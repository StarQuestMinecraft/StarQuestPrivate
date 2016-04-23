package net.homeip.hall.sqnetevents;

import org.bukkit.plugin.java.JavaPlugin;

import net.homeip.hall.sqnetevents.networking.Receiver;
import net.homeip.hall.sqnetevents.networking.Sender;
import net.homeip.hall.sqnetevents.packet.Packet;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SQNetEvents extends JavaPlugin {
	
	private static SQNetEvents instance;
	
	private Receiver receiver;
	
	private HashMap<String, Sender> senders;
	
	@Override
	public void onEnable() {
		setInstance(this);
		senders = new HashMap<String, Sender>();
		//address to bind and listen on
		String listenAddress = getConfig().getString("ListenAt");
		setReceiver(new Receiver(listenAddress));
		//client name and address, separated by an underscore
		List<String> namesAndAddresses = getConfig().getStringList("SendTo");
		namesAndAddresses.add(getConfig().getString("SendTo"));
		//splits the name and address
		for(String nameAndAddress : namesAndAddresses) {
			String[] naa = nameAndAddress.split("@");
			String name = naa[0];
			String address = naa[1];
			System.out.println("[NetEvents] Connect address: " + address);
			Sender sender = new Sender(address);
			//puts sender in hashmap with name as key
			addSender(sender, name);
		}
	}
	//closes all connections
	@Override
	public void onDisable() {
		try {
			getReceiver().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(Sender sender: getSenders().values()) {
			try {
				sender.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//sends packet to all client servers, not recommended
	public void send(Packet packet) {
		for(Sender sender : getSenders().values()) {
			sender.send(packet);
		}
	}
	//sends packet to a specific client server
	public void send(Packet packet, String serverName) {
		getSenders().get(serverName).send(packet);
	}
	//address to bind and listen on
	public Receiver getReceiver() {
		return receiver;
	}
	public void setReceiver(Receiver aReceiver) {
		receiver = aReceiver;
	}
	//addresses to send data to
	public HashMap<String, Sender> getSenders() {
		return senders;
	}
	public void addSender(Sender sender, String name) {
		senders.put(name, sender);
	}
	
	public static SQNetEvents getInstance() {
		return instance;
	}
	public static void setInstance(SQNetEvents anInstance) {
		instance = anInstance;
	}
}
