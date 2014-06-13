package net.countercraft.movecraft.bungee;

import java.util.HashMap;

import de.howaner.BungeeCordLib.server.BungeeServer;

public class PingUtils{
	private static HashMap<String, BungeeServer> servers = new HashMap<String, BungeeServer>();
	private static HashMap<String, Boolean> onlineData = new HashMap<String, Boolean>();
	public static boolean done = false;
	private static String[] serverNames = {"Regalis", "Defalos", "Digitalia", "Quavara", "Acualis", "Drakos", "Krystallos", "Emera", "Valadro", "Iffrizar", "Valadro", "Boskevine", "Boletarian", "Inaris", "AsteroidBelt", "Kelakaria"};
	
/*	public static void setUp(){
		return;
		/*servers = new HashMap<String, BungeeServer>();
		new Thread(new Runnable(){
			public void run(){
				pingCheck();
			}
		}).start();
	}
	
	public static void pingCheck(){
		while(!done){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(String s : serverNames){
				boolean isOnline = ping(s);
				boolean existingValue;
				if(onlineData.containsKey(s)){
					existingValue = onlineData.get(s); 
					onlineData.remove(s);
				} else {
					existingValue = false;
				}
				if(existingValue != isOnline){
					onlineData.put(s, isOnline);
				}
			}
		}
	}
	
	public static boolean isOnline(String server){
		return true;
		//return onlineData.get(server);
	}
	
	private static boolean ping(String server){
		BungeeServer s = load(server);
		if(s == null) return false;
		
		ServerData data = s.getData();
	
		if(data == null){
			return false;
		}
		return true;
	}
	
	public ServerData getServerData(String server){
		BungeeServer s = load(server);
		if(s == null) return null;
		ServerData data = s.getData();
		return data;
	}
	
	private static BungeeServer load(String server){
		BungeeServer s;
		if(!servers.containsKey(server)){
			int i = getPort(server);
			if(i == -1) return null;
			s = BungeeCord.getManager().addServer(server, "localhost:" + i);
			if(s == null){
				System.out.println("WILL RETURN NULL ERROR");
			}
			servers.put(server, s);
		} else {
			s = servers.get(server);
		}
		return s;
	}
	
	public static int getPort(String server){
		switch(server){
		case "Regalis": return 25401;
		case "Boletarian": return 25402;
		case "Boskevine": return 25503;
		case "Kelakaria": return 25504;
		case "Quavara": return 25505;
		case "Defalos": return 25501;
		case "Acualis": return 25502;
		case "Drakos": return 25503;
		case "Emera": return 25504;
		case "Krystallos": return 25505;
		case "Digitalia": return 25601;
		case "AsteroidBelt": return 25602;
		case "Ceharram": return 25603;
		case "Iffrizar": return 25604;
		case "Inaris": return 25605;
		case "Valadro": return 25606;
		default: return -1;
		}
	}*/
}
