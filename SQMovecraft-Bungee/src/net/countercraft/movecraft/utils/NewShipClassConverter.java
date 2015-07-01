package net.countercraft.movecraft.utils;

public class NewShipClassConverter {
	public static String convert(String existingClass, int n){
		switch(existingClass.toLowerCase()){
		case "blockade runner":
			if(n <= 900) return "Courier";
			return "corvette";
		case "bomber":
			if(n <= 600) return "Gunship";
			return "Cruiser";
		case "carrier":
			return "Legacy Carrier";
		case "cruiser":
			if(n <= 1200) return "Cruiser";
			return "Dreadnought";
		case "gunship":
			if(n <= 600) return "Gunship";
			return "Cruiser";
		case "heavy freighter":
			if(n <= 1200) return "Freighter";
			return "Heavy Freighter";
		case "interceptor":
			if(n <= 300) return "Gunship";
			if(n <= 900) return "Pursuer";
			return "Interceptor";
		case "ironclad":
			if(n <= 600) return "Light Yacht";
			if(n <= 1200) return "Yacht";
			if(n <= 1250) return "Heavy Yacht";
		case "light freighter":
			if(n <= 600) return "Light Freighter";
			return "Freighter";
		case "starfighter":
			return "Fighter";
		default: return existingClass;
		}
	}
}
