package me.meiamsome.myriadbase;

import java.util.logging.Logger;

import me.meiamsome.myriadcore.MyriadCore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class MyriadPlugin extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	public MyriadCore core=null;
	PluginListener pluginListener;
	PluginManager pm;
	public final int CHAT_ALL=0, CHAT_DEV=1, CHAT_ADMIN=2;
	public final int CHATLEVEL_FULL=0, CHATLEVEL_LIMITED=1, CHATLEVEL_BLOCKED=2;
	@Override
	public void onEnable() {
		pm=getServer().getPluginManager();
		pluginListener = new PluginListener(this);
		pm.registerEvents(pluginListener, this);
		if(!pm.isPluginEnabled("MyriadCore")) {
			log.warning("["+getDescription().getName()+"] MyriadCore is currently disabled / not on server.");
		} else {
			Plugin p = pm.getPlugin("MyriadCore");
			if(p instanceof MyriadCore) {
				log.info("["+getDescription().getName()+"] Found MyriadCore.");
				core=(MyriadCore)p;
				ConnectCore();
			} else {
				log.warning("["+getDescription().getName()+"] Found MyriadCore, but it is incorrect.");
			}
		}
	}
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}
	public void onConnect() {}
	public void onDisconnect() {}
	void ConnectCore() {
		if(core!=null && !isConnected())
			core.add(this);
	}
	void DisconnectCore() {
		if(isConnected())
			core.remove(this);
	}
	boolean isConnected() {
		if(core==null) return false;
		return core.isConnected(this);
	}

	public String getPluginName() {
		if(!isConnected()) return getDescription().getName();
		return core.getName(this);
	}
	public String getTag() {
		if(!isConnected()) return getDescription().getName();
		return core.getTag(this);
	}
	public  void setName(String name) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.setName(this, name);
	}
	public  void setTag(String tag) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.setTag(this, tag);
	}
	public  void setTagColor(ChatColor col) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.setTagColor(this, col);
	}
	public  void setTextColor(ChatColor col) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.setTextColor(this, col);
	}

	public  void setTempLevel(Player player, int level, boolean tell) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.setTempLevel(this,player,level, tell);
	}

	public  void resetTempLevel(Player player) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		core.resetTempLevel(this,player);
	}
	
	// BEGIN CRAZY OVERLOAD FUNCTIONS
	public void broadcastMessage(String message) {sendMessage(null,message,0,true);}
	public void broadcastMessage(String message, int level) {sendMessage(null,message,level,true);}
	public void broadcastMessage(String message, int level, boolean tag) {sendMessage(null,message,level,tag);}
	public void broadcastMessage(String message[]) {sendMessage(null,message,0,true);}
	public void broadcastMessage(String message[], int level) {sendMessage(null,message,level,true);}
	public void broadcastMessage(String message[], int level, boolean tag) {sendMessage(null,message,level,tag);}
	
	public void sendMessage(Player player, String message) {sendMessage(player,message,0,true);}
	public void sendMessage(Player player, String message, int level) {sendMessage(player,message,level,true);}
	public void sendMessage(Player player, String message, int level, boolean tag) {sendMessage((CommandSender)player,message,level,tag);}
	
	public void sendMessage(Player player, String message[]) {sendMessage(player,message,0,true);}
	public void sendMessage(Player player, String message[], int level) {sendMessage(player,message,level,true);}
	public void sendMessage(Player player, String message[], int level, boolean tag) {sendMessage((CommandSender)player,message,level,tag);}
	
	public void sendMessage(CommandSender cs, String message) {sendMessage(cs,message,0,true);}
	public void sendMessage(CommandSender cs, String message, int level) {sendMessage(cs,message,level,true);}
	public void sendMessage(CommandSender cs, String message, int level, boolean tag) {
		if(isConnected()) {
			sendMessage(cs, core.getToAll(message), level, tag);
		} else sendMessage(cs, new String[] {message}, level, tag);

	}

	public void sendMessage(CommandSender cs, String message[]) {sendMessage(cs,message,0,true);}
	public void sendMessage(CommandSender cs, String message[], int level) {sendMessage(cs,message,level,true);}
	//NOW THE ACTUAL FUNCTION :O
	public void sendMessage(CommandSender cs, String message[], int level, boolean tag) {
		if(message.length==0) return;
		if(isConnected()) {
			if(core.debug) core.log.info("[MyriadCore][Debug] Sending chat");
			core.sendMessage(cs, message, tag, level, this);
			return;
		}
		String msg=null;
		for(int i=0;i<message.length && msg==null;i++) msg=message[i];
		if(tag) {
			msg=ChatColor.YELLOW+"["+getDescription().getName()+"] "+ChatColor.WHITE+msg;
		}
		if(cs==null) {
			if(level==0) {
				getServer().broadcastMessage(msg);
			} else for(Player p: getServer().getOnlinePlayers()) sendMessage(p,message,level,tag);
		} else {
			if(level==0 || cs.isOp()) cs.sendMessage(msg);
		}
	}
	

	public LivingTracker newScan(Class<? extends LivingEntity> theClass) throws CoreNotConnectedException {return newScan(theClass,this);}
	public LivingTracker newScan(Class<? extends LivingEntity> theClass, Object owner) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		return core.newScan(theClass,owner,this);
	}
	public LivingTracker newScan(LivingEntity anEntity) throws CoreNotConnectedException {return newScan(anEntity,this);}
	public LivingTracker newScan(LivingEntity anEntity, Object owner) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		return core.newScan(anEntity.getClass(),owner,this);
	}
	public BlockTracker newScan(Material mat) throws CoreNotConnectedException {return newScan(mat,this);}
	public BlockTracker newScan(Material mat, Object owner) throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		return core.newScan(mat,owner,this);
	}
	
	public SignTracker newSign() throws CoreNotConnectedException {
		if(!isConnected()) throw new CoreNotConnectedException();
		return core.newSign();
	}
}
