package net.countercraft.movecraft.event;

import net.countercraft.movecraft.async.translation.TranslationTaskData;
import net.countercraft.movecraft.craft.Craft;

public class CraftSyncTranslateEvent extends CraftEvent{
	TranslationTaskData data;
	String message = "Your craft was blocked from moving by a plugin. Ask the dev to set a custom message here.";
	public CraftSyncTranslateEvent(Craft c, TranslationTaskData taskData) {
		super(c);
		data = taskData;
	}
	
	public TranslationTaskData getData(){
		return data;
	}
	
	public void setFailMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
