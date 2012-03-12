package me.meiamsome.myriadcore;

import java.lang.reflect.Method;
import java.util.ArrayList;

import me.meiamsome.myriadbase.MyriadPlugin;

import org.bukkit.Chunk;

public abstract class WorldScanner<T> {
		int id;
		Method[] methods={null,null,null,null,null};
		/*   method[x]		event
		 *      0			add event
		 *      1			remove event
		 *      2			canPlace       |   Death 
		 *      3			canDestroy     |
		 *      4			canMove        |
		 */
		Object owner;
		MyriadPlugin plug;
		MyriadCore mc;
		Class<T> theClass;
		ArrayList<T> foundThings=new ArrayList<T>();
		WorldScanner(Class<T> thClass, Object creator, MyriadPlugin plugin, MyriadCore mCore) {
			theClass=thClass;
			id=mCore.wt.scans++;
			mCore.wt.scanners.put(id, this);
			owner=creator;
			mc=mCore;
			plug=plugin;
		}

		protected Boolean run(int method, Object[] args) {
			Boolean ret=true;
			if(methods[method]!=null) try {
				Object a=methods[method].invoke(owner, args);
				if(a instanceof Boolean && a!=null) ret=(Boolean) a;
			} catch (Exception e) {
				mc.log.warning("[MyriadCore][Debug]Failed to pass WorldTracker event to "+plug.getPluginName());
				if(mc.debug) e.printStackTrace();
			}
			return ret;
		}
		void found(T thing){
			if(foundThings.contains(thing)) return;//Caused when a rescan occurs
			if(run(0,new Object[]{thing})) foundThings.add(thing);
		}
		void remove(Object item) {
			if(foundThings.contains(item) && run(1,new Object[]{item})) foundThings.remove(item);
		}
		public void setCallbacks(int[] ids, String[] functions) throws SecurityException, NoSuchMethodException {
			Exception e=null;
			for(int i=0;i<ids.length;i++) {
				try {
				Method m=plug.getClass().getMethod(functions[i], getArgs(ids[i]));
				methods[ids[i]]=m;
				} catch (Exception ex) {e=ex;}
			}
			if(e instanceof SecurityException) throw (SecurityException)e;
			if(e instanceof NoSuchMethodException) throw (NoSuchMethodException)e;
		}
		//Meant to overload
		Class<?>[] getArgs(int method) {return new Class<?>[]{theClass};};
		//All abstract functions
		abstract void remove(Chunk c);
		abstract boolean isIncluded(Object o);
}
