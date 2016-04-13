package net.homeip.hall.sqnetevents;

import org.bukkit.plugin.java.JavaPlugin;

import net.homeip.hall.sqnetevents.networking.Receiver;
import net.homeip.hall.sqnetevents.networking.Sender;

import java.util.List;
import java.util.ArrayList;

public class SQNetEvents extends JavaPlugin {
	
	private static SQNetEvents instance;
	
	private Receiver receiver;
	
	private ArrayList<Sender> senders;
	
	@Override
	public void onEnable() {
		instance = this;
		senders = new ArrayList<Sender>();
		//address to bind and listen on
		String listenAddress = getConfig().getString("ListenAt");
		receiver = new Receiver(listenAddress);
		//addresses to send data to
		List<String> connectAddresses = getConfig().getStringList("SendTo");
		connectAddresses.add(getConfig().getString("SendTo"));
		for(String address : connectAddresses) {
			System.out.println("Connect address: " + address);
			Sender sender = new Sender(address);
			senders.add(sender);
		}
	}
	//address to bind and listen on
	public Receiver getReceiver() {
		return receiver;
	}
	//addresses to send data to
	public ArrayList<Sender> getSenders() {
		return senders;
	}
	
	public static SQNetEvents getInstance() {
		return instance;
	}
}
