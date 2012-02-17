package me.meiamsome.myriadbase;

import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

public interface LivingTracker {
	public void addListener(Class<? extends LivingEntity> listenFor);
	public ArrayList<LivingEntity> getFound();
	public void setCallbacks(int[] ids, String[] functions) throws SecurityException, NoSuchMethodException;
	public void death(EntityDeathEvent event);
}
