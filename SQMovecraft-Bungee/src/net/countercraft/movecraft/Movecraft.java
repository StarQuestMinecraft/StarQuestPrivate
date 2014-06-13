/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.countercraft.movecraft.async.AsyncManager;
import net.countercraft.movecraft.async.translation.AutopilotRunTask;
import net.countercraft.movecraft.bedspawns.Bedspawn;
import net.countercraft.movecraft.bungee.BungeeCraftReciever;
import net.countercraft.movecraft.bungee.BungeeCraftSender;
import net.countercraft.movecraft.bungee.BungeeFileHandler;
import net.countercraft.movecraft.bungee.BungeeListener;
import net.countercraft.movecraft.bungee.PingUtils;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.listener.BlockListener;
import net.countercraft.movecraft.listener.CommandListener;
import net.countercraft.movecraft.listener.InteractListener;
import net.countercraft.movecraft.listener.PartListener;
import net.countercraft.movecraft.listener.PlayerListener;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.metrics.MovecraftMetrics;
import net.countercraft.movecraft.utils.LocationUtils;
import net.countercraft.movecraft.utils.MapUpdateManager;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.countercraft.movecraft.utils.ShipNuker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Movecraft extends JavaPlugin {
	private static Movecraft instance;
	private Logger logger;
	private boolean shuttingDown;
	
	public void onDisable() {
		// Process the storage crates to disk
		//StorageChestItem.saveToDisk();
		shuttingDown = true;
		PingUtils.done = true;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("removehome") && sender instanceof Player){
			Bedspawn.deleteBedspawn(sender.getName());
		} else if(cmd.getName().equalsIgnoreCase("UUID")){
			String name = args[0];
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("UUIDOther");
				out.writeUTF(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Player p = Bukkit.getOnlinePlayers()[0];
			p.sendPluginMessage(Movecraft.getInstance(), "BungeeCord", b.toByteArray());
			return true;
		} else if(cmd.getName().equalsIgnoreCase("serverjump") && sender.isOp()){
			String serverName = args[0];
			Player p = (Player) sender;
			try {
				BungeeCraftSender.sendCraft(p, serverName, "world", 0, 200, 0, CraftManager.getInstance().getCraftByPlayer(p));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("loadship")){
			if(sender.hasPermission("movecraft.loadship")){
				if(args.length == 1){
					byte[] craftData = BungeeFileHandler.readCraftBytes(args[0]);
					BungeeCraftReciever.readCraftAndBuild(craftData, false);
					sender.sendMessage("loaded ship.");
					return true;
				}
				sender.sendMessage("incorrect arguments.");
				return true;
			}
			sender.sendMessage("you don't have permission.");
			return true;
		} else if(cmd.getName().equalsIgnoreCase("nukeship")){
			if(sender.hasPermission("movecraft.loadship")){
				ShipNuker.nuke((Player) sender);
				sender.sendMessage("Ship nuked!");
			}
		}
		return false;
	}

	public void onEnable() {
		// Read in config
		this.saveDefaultConfig();
		Settings.LOCALE = getConfig().getString( "Locale" );
		// if the PilotTool is specified in the config.yml file, use it
		if(getConfig().getInt("PilotTool")!=0) {
			logger.log( Level.INFO, "Recognized PilotTool setting of: "+getConfig().getInt("PilotTool"));
			Settings.PilotTool=getConfig().getInt("PilotTool");
		} else {
			logger.log( Level.INFO, "No PilotTool setting, using default of 280");
		}
		// if the CompatibilityMode is specified in the config.yml file, use it. Otherwise set to false. - NOT IMPLEMENTED YET - Mark
		Settings.CompatibilityMode=getConfig().getBoolean("CompatibilityMode", false);
		LocationUtils.setUp(getConfig());
		if ( !new File( getDataFolder() + "/localisation/movecraftlang_en.properties" ).exists() ) {
			this.saveResource( "localisation/movecraftlang_en.properties", false );
		}
		I18nSupport.init();
		if ( shuttingDown && Settings.IGNORE_RESET ) {
			logger.log( Level.SEVERE, String.format( I18nSupport.getInternationalisedString( "Startup - Error - Reload error" ) ) );
			logger.log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Startup - Error - Disable warning for reload" ) ) );
			getPluginLoader().disablePlugin( this );
		} else {


			// Startup procedure
			AsyncManager.getInstance().runTaskTimer( this, 0, 1 );
			MapUpdateManager.getInstance().runTaskTimer( this, 0, 1 );

			CraftManager.getInstance();

			getServer().getPluginManager().registerEvents( new InteractListener(), this );
			getServer().getPluginManager().registerEvents( new CommandListener(), this );
			getServer().getPluginManager().registerEvents( new BlockListener(), this );
			getServer().getPluginManager().registerEvents( new PlayerListener(), this );
			getServer().getPluginManager().registerEvents( new PartListener(), this );
			
			this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener());
		    
			//StorageChestItem.readFromDisk();
			//StorageChestItem.addRecipie();

			new MovecraftMetrics( CraftManager.getInstance().getCraftTypes().length );   
			//bungee server/minecraft server/plugins/movecraft
			
			new AutopilotRunTask();
			
			Bedspawn.setUp();
			//PingUtils.setUp();
			
			logger.log( Level.INFO, String.format( I18nSupport.getInternationalisedString( "Startup - Enabled message" ), getDescription().getVersion() ) );
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instance = this;
		logger = getLogger();
	}

	public static Movecraft getInstance() {
		return instance;
	}
	
	/*public WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}*/
	
	public static String locToString(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}

	public static Location stringToLoc(String string) {
		if (string == null) return null;
		String[] split = string.split(":");
		World world = Bukkit.getServer().getWorld(split[0]);
		double X = Double.parseDouble(split[1]);
		double Y = Double.parseDouble(split[2]);
		double Z = Double.parseDouble(split[3]);
		return new Location(world, X, Y, Z);
	}
	
	public static MovecraftLocation stringToMovecraftLoc(String string) {
		if (string == null) return null;
		String[] split = string.split(":");
		//World world = Bukkit.getServer().getWorld(split[0]);
		int X = (int) Double.parseDouble(split[1]);
		int Y = (int) Double.parseDouble(split[2]);
		int Z = (int) Double.parseDouble(split[3]);
		return new MovecraftLocation(X, Y, Z);
	}
	
	public static boolean signContainsPlayername(Sign sign, String name){
		String[] lines = sign.getLines();
		if(name.length() > 15){
			name = name.substring(0, 15);
		}
		for(int i = 1; i < 4; i++){
			String s = lines[i];
			if(name.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public Bedspawn getDefaultBedspawn(){
		String server = getConfig().getString("defaultBedspawnServer");
		String world = getConfig().getString("defaultBedspawnWorld");
		int X = getConfig().getInt("defaultBedspawnX");
		int Y = getConfig().getInt("defaultBedspawnY");
		int Z = getConfig().getInt("defaultBedspawnZ");
		return new Bedspawn(null, server, world, X, Y, Z);
	}
	
}
