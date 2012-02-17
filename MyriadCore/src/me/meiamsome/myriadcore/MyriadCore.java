package me.meiamsome.myriadcore;

import me.meiamsome.myriadbase.BlockTracker;
import me.meiamsome.myriadbase.LivingTracker;
import me.meiamsome.myriadbase.MyriadPlugin;
import me.meiamsome.myriadbase.SignTracker;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

public class MyriadCore extends MyriadPlugin{
	ChatHandler ch;
	PluginHandler ph;
	WorldTracker wt;
	public boolean debug = true;
	
	PluginManager pm;
	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public void onEnable() {
		pm=getServer().getPluginManager();
		
		ch=new ChatHandler(this);
		ph=new PluginHandler(this);
		wt=new WorldTracker(this, pm);

		
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, ph, Event.Priority.Monitor, this);
		getCommand("ignoreplugin").setExecutor(ch);
		getCommand("talk").setExecutor(ch);
		super.onEnable();
	}
	@Override
	public void onConnect() {
		if(debug)log.info("[MyriadCore] [Debug] Connected. Vers 0.05");
	}
	
	//Plugin handler functions
	public void add(MyriadPlugin plugin) {ph.add(plugin);}
	public void remove(MyriadPlugin plugin) {ph.remove(plugin);}
	public MyriadPlugin get(String name) {return ph.get(name);}
	public String getName(MyriadPlugin plugin) {return ph.getName(plugin);}
	public void setName(MyriadPlugin plugin, String name) {ph.setName(plugin,name);}
	public String getTag(MyriadPlugin plugin) {return ph.getTag(plugin);}
	public void setTag(MyriadPlugin plugin, String tag) {ph.setTag(plugin,tag);}
	public void setTagColor(MyriadPlugin plugin, ChatColor col) {ph.setTagCol(plugin,col);}
	public void setTextColor(MyriadPlugin plugin, ChatColor col) {ph.setTextCol(plugin,col);}
	public boolean isConnected(MyriadPlugin plugin) {return ph.isConnected(plugin);}
	
	//Chat handler functions
	public void sendMessage(CommandSender player, String[] message, boolean tag, int type, MyriadPlugin plugin) {ch.sendMessage(player, message, tag, type, plugin);}
	public String[] getToAll(String str) {return ch.getToAll(str); }
	
	//World tracker functions
	public LivingTracker newScan(Class<? extends LivingEntity> theClass, Object creator, MyriadPlugin plugin) {return wt.newScan(theClass,creator,plugin);}
	public BlockTracker newScan(Material mat, Object creator, MyriadPlugin plugin) {return wt.newScan(mat,creator,plugin);}	
	
	//Other
	public SignTracker newSign() {
		try {
			return new RealSignTracker(this);
		} catch (Exception e) {/* Silly Java,this wont ever happen */}
		return null;
	}

}
