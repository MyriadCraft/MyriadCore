package me.meiamsome.myriadcore;

import java.util.HashMap;

import me.meiamsome.myriadbase.MyriadPlugin;
import me.meiamsome.myriadcore.PluginHandler.PluginSettings;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatHandler implements CommandExecutor {
	final int high = 5;
	MyriadCore par;
	final int defLevel=0xFFFF00;
	HashMap<MyriadPlugin,HashMap<String,Integer>> ignoreList=new HashMap<MyriadPlugin,HashMap<String,Integer>>();
	ChatHandler(MyriadCore parent) {
		par=parent;
	}

	public void sendMessage(CommandSender player, String[] message, boolean useTag, int mode, MyriadPlugin plugin) {
		if(player==null) {// Broadcasting to all pl0ayers
			for(Player p : par.getServer().getOnlinePlayers()) {
				sendMessage(p,message,useTag,mode,plugin);
			}
			sendMessage(par.getServer().getConsoleSender(),message,useTag,mode,plugin);
		} else {
			if(mode==par.CHAT_ADMIN) {
				for(int i=0;i<message.length;i++) if(message[i]!=null && message[i].length()>0) {
					switch(i) {
						case 0:message[i]=ChatColor.RED+"[Owner Channel] "+message[i];break;
						case 1:message[i]=ChatColor.DARK_BLUE+"[Admin Channel] "+ChatColor.BLUE+message[i];break;
						case 2:message[i]=ChatColor.DARK_GREEN+"[Mod. Channel] "+message[i];break;
						case 3:message[i]=ChatColor.AQUA+"[Dev. Channel] "+message[i];break;
						case 4:message[i]=ChatColor.GREEN+"[Guide Channel] "+message[i];break;
						case 5:message[i]=ChatColor.GOLD+"[Staff Channel] "+message[i];break;
					}
				}
			}
			if(!(player instanceof Player)) {
				for(int i=0;i<=high && i< message.length;i++) {
					if(message[i]!=null) {
						if(message[i].length()>0) player.sendMessage((useTag?plugin.getTag():"")+message[i]);
						return;//Only break if sent a message
					}
				}
				return;
			}
			String tag="";
			if(useTag) tag= par.getTag(plugin);
			int ignore=getLevel(plugin,player.getName());
			ignore>>=8*mode;
			ignore&=0xFF;
			if((ignore & 0xF0)>>4!=getDefaultPart(player.getName(),mode*2+1)) ignore=(ignore & 0xF0)>>4;
			ignore &= 0xF;
			for(int i=ignore;i<=high && i< message.length;i++) {
				if(message[i]!=null) {
					if(message[i].length()>0) player.sendMessage(tag+message[i]);
					break;//Only break if sent a message
				}
			}
		}
	}
	private int getDefaultPart(String user,int position) {
		return 0xF&(getDefault(user)>>position*4);
	}
	private int getDefault(String user) {
		int lev=defLevel;
		if(ignoreList.containsKey(par) && ignoreList.get(par).containsKey(user)) lev=(0xFF & ignoreList.get(par).get(user)) | (lev&~0xFF);
		if(par.getServer().getPlayer(user)!=null) if(par.getServer().getPlayer(user).hasPermission("mc.staff")) lev &= 0xF4FFFF;
		if(par.getServer().getPlayer(user)!=null) if(par.getServer().getPlayer(user).hasPermission("mc.guide")) lev &= 0xF3FFFFF;
		if(par.getServer().getPlayer(user)!=null) if(par.getServer().getPlayer(user).hasPermission("mc.mod")) lev &= 0xF2FFFF;
		if(par.getServer().getPlayer(user)!=null) if(par.getServer().getPlayer(user).hasPermission("mc.admin")) lev &= 0xF1FFFF;
		if(par.getServer().getPlayer(user)!=null) if(par.getServer().getPlayer(user).hasPermission("mc.owner")) lev &= 0xF0FFFF;
		return lev;
	}
	private int getLevel(MyriadPlugin p, String username) {
		if(!ignoreList.containsKey(p)) {
			if(p!=par) return getDefault(username);
			ignoreList.put(par, new HashMap<String, Integer>());
		}
		if(!ignoreList.get(p).containsKey(username)) {
			if(p!=par) return getDefault(username);
			if(par.getServer().getPlayer(username)!=null && par.getServer().getPlayer(username).hasPermission("mc.dev")) {
				return getDefault(username)&~0xFF00;
			} else return getDefault(username);
		}
		return ignoreList.get(p).get(username);
	}
	private void setLevel(MyriadPlugin p, String username, int level) {
		if(!ignoreList.containsKey(p)) ignoreList.put(p,new HashMap<String, Integer>());
		ignoreList.get(p).put(username, level);
	}
	public void changeLevel(MyriadPlugin p, Player player, int level, int type, boolean temp, boolean tell) {
		int old=getLevel(p,player.getName());
		old=(old & ~((temp?0xF0:0xFF)<<(8*type)))|(level<<(8*type+(temp?4:0)));
		setLevel(p,player.getName(),old);
		if(tell) {
			String out=p.getName();
			if(temp) out+=" temporary";
			switch(type) {
				case 1:out+=" developer";break;
			}
			out+=" level set to";
			switch(level) {
				case 0:out+=" allowed.";break;
				case 1:out+=" limited.";break;
				case 2:out+=" blocked.";break;
			}
			par.sendMessage(player, out.replaceFirst(".", out.substring(0, 1).toUpperCase()));
		}
	}
	@Override
	public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("ignoreplugin")) {
			if(args.length==0) {
				return false;
			} else {
				if(args[0].equalsIgnoreCase("list")) {
					String a="Plugins connected to the core: ";
					for(PluginSettings p: par.ph.plugins.values()) a+=p.name+", ";
					a=a.substring(0, a.length()-2);
					par.sendMessage(cs, a);
					return true;
				}
				int arg=0;
				int type=0;
				if(args.length<2) {
					par.sendMessage(cs,"Please supply a plugin and level");
					return true;
				}
				boolean temp=false;
				if(firstMatch(args[0].toLowerCase(), new String[]{"t(emp(orary)?)?"})!=-1){
					arg=1;
					temp=true;
				}
				if(args.length<2+arg) {
					par.sendMessage(cs,"Please supply a plugin and level");
					return true;
				}
				if(firstMatch(args[arg].toLowerCase(), new String[]{"d(ev(eloper)?)?"})!=-1){
					if(!cs.hasPermission("mc.dev")) par.sendMessage(cs, ChatColor.RED+"You do not have access.");
					arg+=1;
					type=1;
				}
				if(!(cs instanceof Player)) {
					cs.sendMessage("You cannot use this command");
				}
				if(args.length<2+arg) {
					par.sendMessage( cs,"Please supply a plugin and level");
					return true;
				}
				MyriadPlugin plu=par.get(args[arg]);
				if(plu==null) {
					par.sendMessage(cs, "Unknown plugin '"+args[arg]+"'");
					return true;
				}
				if(args[arg+1].equalsIgnoreCase("full") || args[arg+1].equalsIgnoreCase("f")) {
					changeLevel(plu, (Player)cs, 0, type, temp, true);
				} else if(args[arg+1].equalsIgnoreCase("limited") || args[arg+1].equalsIgnoreCase("l")) {
					changeLevel(plu, (Player)cs, 1, type, temp, true);
				} else if(args[arg+1].equalsIgnoreCase("blocked") || args[arg+1].equalsIgnoreCase("b")) {
					changeLevel(plu, (Player)cs, 2, type, temp, true);
				}
			}
			return true;
		} else if(command.getName().equalsIgnoreCase("talk")) {
			if(args.length<1) {
				par.sendMessage(cs, "Supply a channel and text to talk.");
				return true;
			}
			if(firstMatch(args[0].toLowerCase(),new String[]{"owner[+]?","admin[+]?","mod[+]?","dev[+]?","guide[+]?","staff[+]?"})!=-1) {
				String type=args[0];
				boolean mode=false;
				if(type.matches(".*[+]")) {
					type=type.substring(0, type.length()-1);
					mode=true;
				}
				if(!cs.hasPermission("mc."+type)) {
					par.sendMessage(cs, "You don't have the rights for that channel.");
					return true;
				}
				String msg=cs.getName()+": ";
				for(int i=1;i<args.length;i++) msg+=args[i]+" ";
				String[] msgArr = new String[firstMatch(args[0].toLowerCase(),new String[]{"owner[+]?","admin[+]?","mod[+]?","dev[+]?","guide[+]?","staff[+]?"})+1];
				for(int i=0;i<msgArr.length;i++) {
					if(i==msgArr.length-1) {
						msgArr[i]=msg;
					} else if(i==msgArr.length-2 && mode) {
						msgArr[i]="";
					} else msgArr[i]=null;
				}
				par.broadcastMessage(msgArr,par.CHAT_ADMIN,false);
			} else {
				par.sendMessage(cs, ChatColor.RED+"Unknown channel!");
			}
			return true;
		}
		return false;
	}
	public int firstMatch(String a, String[] b) {
		for(int i=0;i<b.length;i++) if(a.matches(b[i])) return i;
		return -1;
	}

	public String[] getToAll(String str) {
		String[] r = new String[high+1];
		for(int i=0;i<high;i++) r[i]=null;
		r[high]=str;
		return r;
	}
}
