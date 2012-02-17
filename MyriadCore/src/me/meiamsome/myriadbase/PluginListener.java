package me.meiamsome.myriadbase;

import me.meiamsome.myriadcore.MyriadCore;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class PluginListener extends ServerListener{
	MyriadPlugin par;
	public PluginListener(MyriadPlugin parent) {
		par=parent;
	}
	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if(par.core!=null) return;
		if(event.getPlugin().getDescription().getName().equals("MyriadCore")) {
			if(event.getPlugin() instanceof MyriadCore) {
				par.log.info("["+par.getDescription().getName()+"] Found MyriadCore.");
				par.core=(MyriadCore)event.getPlugin();
				par.ConnectCore();
			} else {
				par.log.warning("["+par.getDescription().getName()+"] Found MyriadCore, but it is incorrect.");
			}
		}
	}
	public void onPluginDisable(PluginDisableEvent event) {
		if(par.core==null) return;
		if(event.getPlugin()==par.core) {
			par.log.info("["+par.getDescription().getName()+"] MyriadCore disabled.");
			par.core=(MyriadCore)event.getPlugin();
		}
	}
}
