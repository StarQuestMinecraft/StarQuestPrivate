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

package net.countercraft.movecraft.craft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.localisation.I18nSupport;

import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import com.ginger_walnut.sqboosters.SQBoosters;

public class CraftType {
	private String craftName;
	private int maxSize, minSize, minHeightLimit, maxHeightLimit, drillHeadID;
	private Integer[] allowedBlocks, forbiddenBlocks;
	private ArrayList<Integer> drilledBlocks;
	private boolean canFly, tryNudge, canCruise, canTeleport, canStaticMove, isGroundVehicle, isPod, isFlagship, isCarrier, canServerJump, canPilotSpace, canPilotPlanet, canHelm, canSpeedScale, canAutopilot;
	private int cruiseSkipBlocks;
	private int hoverHeight;
	private float laserPower, torpedoPower;
	private double fuelBurnRate;
	private double sinkPercent;
	private float collisionExplosion;
	private int tickCooldown;
	private double speed;
	private HashMap<Integer, ArrayList<Double>> flyBlocks = new HashMap<Integer, ArrayList<Double>>();
	private int allowedCannons, allowedPassengers, armorMax, armorResistance;
	private String altName;
	private double cannonDamage;
	private double cannonMaxDegrees;
	private int cannonCooldown;

	public CraftType(File f) {
		try {
			parseCraftDataFromFile(f);
		} catch (Exception e) {
			Movecraft.getInstance().getLogger().log(Level.SEVERE, String.format(I18nSupport.getInternationalisedString("Startup - Error parsing CraftType file"), f.getAbsolutePath()));
			e.printStackTrace();
		}
	}

	private void parseCraftDataFromFile(File file) throws FileNotFoundException {
		InputStream input = new FileInputStream(file);
		Yaml yaml = new Yaml();
		Map data = (Map) yaml.load(input);
		craftName = (String) data.get("name");
		maxSize = (Integer) data.get("maxSize");
		minSize = (Integer) data.get("minSize");
		allowedBlocks = ((ArrayList<Integer>) data.get("allowedBlocks")).toArray(new Integer[1]);
		forbiddenBlocks = ((ArrayList<Integer>) data.get("forbiddenBlocks")).toArray(new Integer[1]);
		canFly = (Boolean) data.get("canFly");
		if (data.containsKey("canHelm")) {
			canHelm = (Boolean) data.get("canHelm");
		} else {
			canHelm = false;
		}	
		if (data.containsKey("canPilotSpace")) {
			canPilotSpace = (Boolean) data.get("canPilotSpace");
		} else {
			canPilotSpace = false;
		}
		if (data.containsKey("canPilotPlanet")) {
			canPilotPlanet = (Boolean) data.get("canPilotPlanet");
		} else {
			canPilotPlanet = false;
		}
		if (data.containsKey("canServerJump")) {
			canServerJump = (Boolean) data.get("canServerJump");
		} else {
			canServerJump = false;
		}
		if (data.containsKey("canSpeedScale")) {
			canSpeedScale = (Boolean) data.get("canSpeedScale");
		} else {
			canSpeedScale = false;
		}
		if (data.containsKey("canAutopilot")) {
			canAutopilot = (Boolean) data.get("canAutopilot");
		} else {
			canAutopilot = true;
		}
		tryNudge = (Boolean) data.get("tryNudge");
		speed = (double) data.get("speed");
		tickCooldown = (int) Math.ceil(20 / ((Double) data.get("speed")));
		flyBlocks = (HashMap<Integer, ArrayList<Double>>) data.get("flyblocks");
		if (data.containsKey("canCruise")) {
			canCruise = (Boolean) data.get("canCruise");
		} else {
			canCruise = true;
		}
		if (data.containsKey("canTeleport")) {
			canTeleport = (Boolean) data.get("canTeleport");
		} else {
			canTeleport = false;
		}
		if (data.containsKey("drilledBlocks")) {
			drilledBlocks = (ArrayList<Integer>) data.get("drilledBlocks");
		} else {
			drilledBlocks = new ArrayList<Integer>();
		}
		if (data.containsKey("drillHeadID")) {
			drillHeadID = (Integer) data.get("drillHeadID");
		} else {
			drillHeadID = -1;
		}
		
		if (data.containsKey("isGroundVehicle")) {
			isGroundVehicle = (Boolean) data.get("isGroundVehicle");
		} else {
			isGroundVehicle = false;
		}
		
		if (data.containsKey("hoverHeight")) {
			hoverHeight = (Integer) data.get("hoverHeight");
		} else {
			if(isGroundVehicle){
				hoverHeight = 1;
			} else {
				hoverHeight = -1;
			}
		}

		if (data.containsKey("canStaticMove")) {
			canStaticMove = (Boolean) data.get("canStaticMove");
		} else {
			canStaticMove = false;
		}
		if (data.containsKey("cruiseSkipBlocks")) {
			cruiseSkipBlocks = (Integer) data.get("cruiseSkipBlocks");
		} else {
			cruiseSkipBlocks = 2 * (Math.round(tickCooldown));
		}
		if (data.containsKey("fuelBurnRate")) {
			fuelBurnRate = (Double) data.get("fuelBurnRate");
		} else {
			fuelBurnRate = 0.0;
		}
		if (data.containsKey("sinkPercent")) {
			sinkPercent = (Double) data.get("sinkPercent");
		} else {
			sinkPercent = 0.0;
		}
		if (data.containsKey("collisionExplosion")) {
			double temp = (Double) data.get("collisionExplosion");
			collisionExplosion = (float) temp;
		} else {
			collisionExplosion = 0.0F;
		}
		if (data.containsKey("minHeightLimit")) {
			minHeightLimit = (Integer) data.get("minHeightLimit");
			if (minHeightLimit < 0) {
				minHeightLimit = 0;
			}
		} else {
			minHeightLimit = 0;
		}
		// maxHeightLimit is corrected by world in Craft.translate
		if (data.containsKey("maxHeightLimit")) {
			maxHeightLimit = (Integer) data.get("maxHeightLimit");
			if (maxHeightLimit <= minHeightLimit) {
				maxHeightLimit = 254;
			}
		} else {
			maxHeightLimit = 254;
		}
		if (data.containsKey("allowedCannons")) {
			allowedCannons = (Integer) data.get("allowedCannons");
		} else {
			allowedCannons = 0;
		}
		if (data.containsKey("allowedCannons")) {
			allowedCannons = (Integer) data.get("allowedCannons");
		} else {
			allowedCannons = 0;
		}
		if (data.containsKey("flagship")) {
			isFlagship = (Boolean) data.get("flagship");
		} else {
			isFlagship = false;
		}
		if (data.containsKey("pod")) {
			isPod = (Boolean) data.get("pod");
		} else {
			isPod = false;
		}
		if (data.containsKey("carrier")) {
			isCarrier = (Boolean) data.get("carrier");
		} else {
			isCarrier = false;
		}
		if (data.containsKey("allowedPassengers")) {
			allowedPassengers = (Integer) data.get("allowedPassengers");
		} else {
			allowedPassengers = -1;
		}
		if (data.containsKey("armorMaxPercent")) {
			armorMax = (Integer) data.get("armorMaxPercent");
		} else {
			armorMax = -1;
		}
		if (data.containsKey("armorResistance")) {
			armorResistance = (Integer) data.get("armorResistance");
		} else {
			armorResistance = 20;
		}
		if (data.containsKey("altName")) {
			altName = (String) data.get("altName");
		} else {
			altName = null;
		}
		if (data.containsKey("laserPower")) {
			double d = (Double) data.get("laserPower");
			laserPower = (float) d;
		} else {
			laserPower = 1.80f;
		}
		if (data.containsKey("cannonDamage")) {
			cannonDamage = (Double) data.get("cannonDamage");
		} else {
			cannonDamage = 20;
		}
		if (data.containsKey("cannonMaxDegrees")) {
			cannonMaxDegrees = (Double) data.get("cannonMaxDegrees");
		} else {
			cannonMaxDegrees = 15;
		}
		if (data.containsKey("cannonCooldown")) {
			cannonCooldown = (int) data.get("cannonCooldown");
		} else {
			cannonCooldown = 20;
		}
		if (data.containsKey("torpedoPower")) {
			double d = (Double) data.get("torpedoPower");
			torpedoPower = (float) d;
		} else {
			torpedoPower = 4.0f;
		}
	}

	public String getCraftName() {
		return craftName;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getMinSize() {
		return minSize;
	}

	public int getAllowedCannons(Player p) {
		return allowedCannons;
	}

	public Integer[] getAllowedBlocks() {
		return allowedBlocks;
	}

	public Integer[] getForbiddenBlocks() {
		return forbiddenBlocks;
	}

	public ArrayList<Integer> getDrilledBlocks() {
		return drilledBlocks;
	}

	public boolean canFly() {
		return canFly;
	}

	public boolean isGroundVehicle() {
		return isGroundVehicle;
	}

	public boolean getCanCruise() {
		return canCruise;
	}

	public int getCruiseSkipBlocks() {
		return cruiseSkipBlocks;
	}

	public int getDrillHeadID() {
		return drillHeadID;
	}
	
	public int getHoverHeight() {
		return hoverHeight;
	}

	public boolean getCanTeleport() {
		return canTeleport;
	}

	public boolean getCanStaticMove() {
		return canStaticMove;
	}

	public double getFuelBurnRate() {
		return fuelBurnRate;
	}

	public double getSinkPercent() {
		return sinkPercent;
	}

	public float getCollisionExplosion() {
		return collisionExplosion;
	}

	public int getTickCooldown() {
		return tickCooldown;
	}

	public boolean isTryNudge() {
		return tryNudge;
	}
	
	public float getTorpedoPower(){
		return torpedoPower;
	}
	
	public float getLaserPower(){
		return laserPower;
	}

	public double getSpeed(Player p) {
		double spd = speed;
		spd = spd + SQBoosters.getSpeedBooster() - 1;
		return spd;
	}

	public HashMap<Integer, ArrayList<Double>> getFlyBlocks(Player p) {
		HashMap<Integer, ArrayList<Double>> retval = new HashMap<Integer, ArrayList<Double>>();
			for(Integer i : flyBlocks.keySet()){ 
				ArrayList<Double> values = flyBlocks.get(i);
				ArrayList<Double> newValues = new ArrayList<Double>();
				for(Double d : values){
					newValues.add(d);
				}
				retval.put(i, newValues);
			}
			return retval;
		}

	public int getMaxHeightLimit() {
		return maxHeightLimit;
	}

	public int getMinHeightLimit() {
		return minHeightLimit;
	}

	public boolean isFlagship() {
		return isFlagship;
	}
	
	public String getAltName(){
		return altName;
	}
	
	public boolean isCarrier(){
		return isCarrier;
	}
	
	public boolean isPod(){
		return isPod;
	}
	
	public int getArmorMax(Player p){
		return armorMax;
	}
	
	public int getArmorResistance(){
		return armorResistance;
	}
	
	public int getMaxPassengeres(){
		return allowedPassengers;
	}
	
	public boolean getCanHelm(){
		return canHelm;
	}
	
	public boolean getCanPilotSpace(){
		return canPilotSpace;
	}
	
	public boolean getCanPilotPlanet(){
		return canPilotPlanet;
	}
	
	public boolean getCanServerJump(){
		return canServerJump;
	}
	
	public boolean getCanSpeedScale(){
		return canSpeedScale;
	}

	public boolean getCanAutopilot(){
		return canAutopilot;
	}
	
	public double getCannonMaxDegress(){
		return cannonMaxDegrees;
	}
	
	public double getCannonDamage(){
		return cannonDamage;
	}
	
	public int getCannonCooldown(){
		return cannonCooldown;
	}
	
}
