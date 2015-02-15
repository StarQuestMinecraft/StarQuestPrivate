package com.dibujaron.globalsql;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import net.countercraft.movecraft.Movecraft;

import org.bukkit.configuration.file.FileConfiguration;


public class CInfo {
	
	static CInfo instance;
	
	static{
		File dir = Movecraft.getInstance().getDataFolder().getParentFile().getParentFile().getParentFile().getParentFile();
		ConfigAccessor accessor = new ConfigAccessor(Movecraft.getInstance(), "connectiondata.yml", dir.getAbsolutePath());
		instance = new CInfo(accessor.getConfig());
	}
	
	String username;
	String password;
	String hostname;
	String mainDBName;
	String port;
	
	HashMap<String, String> altDbMap = new HashMap<String, String>();
	
	public CInfo(FileConfiguration config){
		instance = this;
		username = config.getString("username");
		password = config.getString("password");
		hostname = config.getString("hostname");
		mainDBName = config.getString("mainDBName");
		port = config.getString("port");
		Set<String> itemKeys = config.getConfigurationSection("pluginDBs").getKeys(true);
		for(String iKey : itemKeys){
			String localKey = "pluginDBs." + iKey;
			String dbname = config.getString(localKey);
			altDbMap.put(iKey, dbname);
		}
	}
	
	public static CInfo get(){
		return instance;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getHostname(){
		return hostname;
	}
	
	public String getDBName(){
		return mainDBName;
	}
	
	public String getPort(){
		return port;
	}
	
	public String getDBName(String plugin){
		return altDbMap.get(plugin);
	}
}
