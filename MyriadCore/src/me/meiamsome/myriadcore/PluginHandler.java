package me.meiamsome.myriadcore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.meiamsome.myriadbase.MyriadPlugin;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginHandler implements Listener {
	MyriadCore par;
	HashMap<MyriadPlugin, PluginSettings> plugins= new HashMap<MyriadPlugin, PluginSettings>();
	File dataFile;
	
	PluginHandler(MyriadCore mc) {
		dataFile=new File(mc.getDataFolder(), "plugins");
		dataFile.mkdirs();
		par=mc;
	}
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if(!(event.getPlugin() instanceof MyriadPlugin)) return;
		remove((MyriadPlugin) event.getPlugin());
	}
	public void saveAll() {
		for(PluginSettings ps: plugins.values()) ps.save();
	}
	public void remove(MyriadPlugin plugin) {
		if(plugins.containsKey(plugin)) {
			plugin.onDisconnect();
			plugins.get(plugin).save();
			par.log.info("[MyriadCore] Plugin "+plugins.get(plugin).name+" disconnected from core.");
			plugins.remove(plugin);
		} else {
			par.log.warning("[MyriadCore] "+plugin.getDescription().getName()+" is not connected to the core.");
		}
	}
	public void add(MyriadPlugin plugin) {
		if(plugins.containsKey(plugin)) {
			par.log.warning("[MyriadCore] "+plugin.getDescription().getName()+" is already connected to the core.");
		} else {
			PluginSettings set=new PluginSettings(plugin);
			set.load();
			plugins.put(plugin, set);
			par.log.info("[MyriadCore] Plugin "+set.name+" connected to the core.");
			plugin.onConnect();
		}
	}
	public boolean isConnected(MyriadPlugin plugin) {
		return plugins.containsKey(plugin);
	}
	public MyriadPlugin get(String name) {
		for(PluginSettings s : plugins.values()) {
			if(s.name.equalsIgnoreCase(name)) return s.plugin;
		}
		for(PluginSettings s : plugins.values()) {
			for(String str:s.pseudonyms) {
				if(str.equalsIgnoreCase(name)) return s.plugin;
			}
		}
		return null;
	}
	public String getName(MyriadPlugin plugin) {
		PluginSettings s = plugins.get(plugin);
		if(s==null) return null;
		return s.name;
	}
	public void setName(MyriadPlugin plugin, String name) {
		PluginSettings s = plugins.get(plugin);
		if(s!=null) s.setName(name);
	}
	public String getTag(MyriadPlugin plugin) {
		PluginSettings s = plugins.get(plugin);
		if(s==null) return null;
		return s.getTag();
	}
	public void setTag(MyriadPlugin plugin, String tag) {
		PluginSettings s = plugins.get(plugin);
		if(s!=null) s.setTag(tag);
	}
	public void setTagCol(MyriadPlugin plugin, ChatColor tagCol) {
		PluginSettings s = plugins.get(plugin);
		if(s!=null) s.setTagColor(tagCol);
	}
	public void setTextCol(MyriadPlugin plugin, ChatColor textCol) {
		PluginSettings s = plugins.get(plugin);
		if(s!=null) s.setTextColor(textCol);
	}
	class PluginSettings {
		File folder;
		MyriadPlugin plugin;
		String name;
		private String tag;
		ChatColor tagCol=ChatColor.YELLOW, textCol=ChatColor.YELLOW;
		ArrayList<String> pseudonyms= new ArrayList<String>();
		PluginSettings(MyriadPlugin plug) {
			plugin=plug;
			name=plugin.getDescription().getName();
			pseudonyms.add(name);
			tag=name;
			folder=new File(dataFile, name);
			folder.mkdirs();
		}
		String getTag() {
			return ChatColor.WHITE+"["+tagCol+tag+ChatColor.WHITE+"] "+textCol;
		}
		void load() {
			try {
				par.ch.load(plugin, folder);
			} catch (Exception e) {
				par.log.warning("FAILED TO LOAD CHAT CONFIGURATION FILE FOR "+plugin.getPluginName());
			}
		}
		void save() {
			try {
				par.ch.save(plugin,folder);
			} catch (IOException e) {
				par.log.warning("FAILED TO SAVE CHAT CONFIGURATION FILE FOR "+plugin.getPluginName());
			}
		}
		void setName(String nam) {
			name=nam;
			pseudonyms.add(nam);
		}
		void setTag(String t) {
			tag=t;
		}
		void setTagColor(ChatColor col) {
			tagCol=col;
		}
		void setTextColor(ChatColor col) {
			textCol=col;
		}
	}
}
