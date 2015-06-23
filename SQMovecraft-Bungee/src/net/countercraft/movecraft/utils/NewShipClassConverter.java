package net.countercraft.movecraft.utils;

public class NewShipClassConverter {
	public static String convert(String existingClass, int n){
		switch(existingClass){
		case "Blockade Runner":
			if(n <= 900) return "Courier";
			return "Corvette";
		case "Bomber":
			if(n <= 600) return "Gunship";
			return "Cruiser";
		case "Carrier":
			return "Legacy Carrier";
		case "Cruiser":
			if(n <= 1200) return "Cruiser";
			return "Dreadnought";
		case "Gunship":
			if(n <= 600) return "Gunship";
			return "Cruiser";
		case "Heavy Freighter":
			if(n <= 1200) return "Freighter";
			return "Heavy Freighter";
		case "Interceptor":
			if(n <= 300) return "Gunship";
			if(n <= 900) return "Pursuer";
			return "Interceptor";
		case "Ironclad":
			if(n <= 600) return "Light Yacht";
			if(n <= 1200) return "Yacht";
			if(n <= 1250) return "Heavy Yacht";
		case "Light Freighter":
			if(n <= 600) return "Light Freighter";
			return "Freighter";
		case "Starfighter":
			return "Fighter";
		default: return existingClass;
		}
	}
}
