
package me.meiamsome.myriadcore;

import java.util.ArrayList;

import me.meiamsome.myriadbase.LivingTracker;
import me.meiamsome.myriadbase.MyriadPlugin;

import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

public class WorldLivingScanner extends WorldScanner<LivingEntity> implements LivingTracker{

	ArrayList<Class<? extends LivingEntity>> classes = new ArrayList<Class<? extends LivingEntity>>(); 
	WorldLivingScanner(Object creator, MyriadPlugin plugin, MyriadCore mCore) {
		super(LivingEntity.class, creator, plugin, mCore);
		mCore.wt.liveSearches.add(this);
	}

	public ArrayList<LivingEntity> getFound() {
		return foundThings;
	}

	public void addListener(Class<? extends LivingEntity> listenFor) {
		classes.add(listenFor);
	}

	@Override
	void remove(Chunk c) {
		for(LivingEntity e:foundThings) {
			if(e.getLocation().getChunk().equals(c)) foundThings.remove(e);
		}
	}

	@Override
	boolean isIncluded(Object o) {
		if(!(o instanceof LivingEntity)) return false;
		for(Class<? extends LivingEntity> cls : classes) {
			if(cls.isAssignableFrom(o.getClass())) return true;
		}
		return false;
	}

	Class<?>[] getArgs(int method) {
		switch(method) {
			case 2:return new Class<?>[]{EntityDeathEvent.class};
		}
		return super.getArgs(method);
	}

	@Override
	public void death(EntityDeathEvent event) {
		if(isIncluded(event.getEntity()))
			run(2,new Object[]{event});
		remove(event.getEntity());
	}

}
