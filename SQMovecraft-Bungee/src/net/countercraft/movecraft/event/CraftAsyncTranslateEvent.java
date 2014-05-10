package net.countercraft.movecraft.event;

import net.countercraft.movecraft.async.translation.TranslationTaskData;
import net.countercraft.movecraft.craft.Craft;

public class CraftAsyncTranslateEvent extends CraftEvent{
	
	TranslationTaskData data;
	String message;
	
	public CraftAsyncTranslateEvent(Craft c, TranslationTaskData taskData) {
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
