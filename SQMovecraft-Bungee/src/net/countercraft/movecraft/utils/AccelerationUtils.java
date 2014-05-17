package net.countercraft.movecraft.utils;

import net.countercraft.movecraft.craft.Craft;

public class AccelerationUtils {
	
	public static void checkAndIncreaseVelocity(Craft c){
		int v = c.getVelocity();
		int s = c.getSteps();
		int a = c.getType().getAcceleration();
		if(shouldGainVelocity(v, s, a)){
			if(v < c.getType().getMaxBlocksPerTranslation())
				c.setVelocity(v + 1);
			c.setSteps(0);
		} else {
			c.setSteps(s + 1);
		}
	}
	public static void checkAndDecrementVelocity(Craft c){
		int v = c.getVelocity();
		if(v == 0) return;
		int s = c.getSteps();
		int a = c.getType().getAcceleration();
		if(shouldGainVelocity(v, s, a)){
			if(v > 0) c.setVelocity(v - 1);
			c.setSteps(0);
		} else {
			c.setSteps(s + 1);
		}
	}
	//v = 0, steps to accel = 2
	public static boolean shouldGainVelocity(int v, int s, int a){
		return s <= steps2NextAcceleration(v, a);
	}
	private static int steps2NextAcceleration(int v, int a){
		return (int)((a/2) * Math.pow(2, a));
	}
	
	//steps to next acceleration is twice the last value of steps to next acceleration
	//EQUATION: stepsToNextAcceleration = (acceleration / 2) * (2 ^ currentMotion)
}
