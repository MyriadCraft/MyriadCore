package me.meiamsome.myriadcore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import me.meiamsome.myriadbase.MyriadPlugin;
import me.meiamsome.myriadcore.PluginHandler.PluginSettings;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;

public class ChatHandler implements CommandExecutor, Listener {
	final int high = 5;
	MyriadCore par;
	Permission [] perms;
	final int defLevel=0xFFFFF0;
	HashMap<MyriadPlugin,HashMap<String,Integer>> ignoreList=new HashMap<MyriadPlugin,HashMap<String,Integer>>();
	HashMap<Player,Integer> curChannels=new HashMap<Player, Integer>();
	ChatHandler(MyriadCore parent) {
		par=parent;
		perms=new Permission[6];
		perms[0]=new Permission("mc.owner");
		perms[1]=new Permission("mc.admin");
		perms[2]=new Permission("mc.mod");
		perms[3]=new Permission("mc.dev");
		perms[4]=new Permission("mc.guide");
		perms[5]=new Permission("mc.staff");
		par.pm.registerEvents(this, par);
	}
	public void load(MyriadPlugin pl, File folder) throws IOException, InvalidConfigurationException {
		File file=new File(folder,"chat.yml");
		FileConfiguration fc=new YamlConfiguration();
		fc.load(file);
		if(!ignoreList.containsKey(pl)) ignoreList.put(pl, new HashMap<String, Integer>()); 
		int i=0;
		for(String a:fc.getValues(false).keySet()) {
			ignoreList.get(pl).put(a, (fc.getInt(a) & 0xFFFF) | (getDefault(a) & 0xFF0000));
			i++;
		}
		par.log.info("[MyriadCore] loaded "+i+" entr(y/ies) for "+pl.getPluginName());
	}
	public void save(MyriadPlugin pl, File folder) throws IOException {
		if(!ignoreList.containsKey(pl)) return;
		File file=new File(folder,"chat.yml");
		FileConfiguration fc=new YamlConfiguration();
		try {
			fc.load(file);
		} catch(Exception e) {}//Silently error here
		int i=0;
		HashMap<String, Integer> hm = ignoreList.get(pl);
		for(String a: hm.keySet()) {
			int res=permOnly(hm.get(a));
			if(res!=permOnly(defLevel)) {
				fc.set(a,res);
				i++;
			}
		}
		par.log.info("[MyriadCore] Saved "+i+" entr(y/ies) for "+pl.getPluginName());
		fc.save(file);
	}
	public int permOnly(int in) {
		int out=0;
		for(int i=0; in>0; i++) {
			out+= (in & 0xF)<<4*i;
			in>>=8;
		}
		return out;
	}
	public int permToFull(int in) {
		int out=0;
		for(int i=0; in>0; i++) {
			out+= (in & 0xF)<<8*i;
			in>>=4;
		}
		return out;
	}
	
	public void sendMessage(CommandSender player, String[] message, boolean useTag, int mode, MyriadPlugin plugin) {
		if(player==null) {// Broadcasting to all pl0ayers
			for(Player p : par.getServer().getOnlinePlayers()) {
				sendMessage(p,message,useTag,mode,plugin);
			}
			sendMessage(par.getServer().getConsoleSender(),message,useTag,mode,plugin);
		} else {
			String tag="";
			if(useTag) tag= par.getTag(plugin);
			if(mode==par.CHAT_ADMIN) {
				for(int i=0;i<message.length;i++) if(message[i]!=null && message[i].length()>0) {
					switch(i) {
						case 0:message[i]=ChatColor.RED+"[Owner Channel] "+tag+message[i];break;
						case 1:message[i]=ChatColor.DARK_BLUE+"[Admin Channel] "+ChatColor.BLUE+tag+message[i];break;
						case 2:message[i]=ChatColor.DARK_GREEN+"[Mod. Channel] "+tag+message[i];break;
						case 3:message[i]=ChatColor.AQUA+"[Dev. Channel] "+tag+message[i];break;
						case 4:message[i]=ChatColor.GREEN+"[Guide Channel] "+tag+message[i];break;
						case 5:message[i]=ChatColor.GOLD+"[Staff Channel] "+tag+message[i];break;
					}
				}
				tag="";
			}
			if(!(player instanceof Player)) {
				for(int i=0;i<=high && i< message.length;i++) {
					if(message[i]!=null) {
						if(message[i].length()>0) {
							player.sendMessage(tag+message[i]);
							return;//Only break if sent a message
						}
					}
				}
				return;
			}
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
		//if(ignoreList.containsKey(par) && ignoreList.get(par).containsKey(user)) lev=(0xFF & ignoreList.get(par).get(user)) | (lev&~0xFF);
		lev |= 0xFF0000;
		if(par.getServer().getPlayer(user)!=null) for(int i=5; i>=0; i--) {
			if(par.getServer().getPlayer(user).hasPermission(perms[i])) {
				lev = (lev & ~0xFF0000) | ((0xF0+i)<<16);
			}
		}
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
		return (ignoreList.get(p).get(username) & ~0xFF0000) | (getDefault(username) & 0xFF0000) ;
	}
	private void setLevel(MyriadPlugin p, String username, int level) {
		if(!ignoreList.containsKey(p)) ignoreList.put(p,new HashMap<String, Integer>());
		ignoreList.get(p).put(username, level);
	}
	public void changeLevel(MyriadPlugin p, Player player, int level, int type, boolean temp, boolean tell) {
		int old=getLevel(p,player.getName());
		old=(old & ~((temp?0xF0:0xFF)<<(8*type)))|(level<<(8*type+(temp?4:0))) | (temp?0:(0xF0<<8*type));
		setLevel(p,player.getName(),old);
		if(tell) {
			String out=p.getPluginName();
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
				par.sendMessage(cs,"Usage: /ignoreplugin <Plugin name> [type] (new level)");
				par.sendMessage(cs,"i.e.:  /ignoreplugin MyriadCore full");
				par.sendMessage(cs,"or: /ignoreplugin MyriadCore temporary full");
				return true;
			} else {
				if(args[0].equalsIgnoreCase("list")) {
					String a="Plugins connected to the core: ";
					for(PluginSettings p: par.ph.plugins.values()) a+=p.name+", ";
					a=a.substring(0, a.length()-2);
					par.sendMessage(cs, a);
					return true;
				} else if(args[0].equalsIgnoreCase("save")) {
					if(cs.hasPermission("mc.admin")) {
						par.ph.saveAll();
					} else par.sendMessage(cs, ChatColor.RED+"You cannot use this command");
					return true;
				}
				int arg=0;
				int type=0;
				boolean temp=false, clear=false;
				if(firstMatch(args[0].toLowerCase(), new String[]{"c(lear)?"})!=-1){
					arg=1;
					clear=true;
				};
				if(firstMatch(args[0].toLowerCase(), new String[]{"t(emp(orary)?)?"})!=-1){
					arg=1;
					temp=true;
				}
				if(args.length<1+arg) {
					par.sendMessage(cs,"Please supply a plugin");
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
				if(args.length<1+arg) {
					par.sendMessage( cs,"Please supply a plugin");
					return true;
				}
				MyriadPlugin plu=par.get(args[arg]);
				if(plu==null) {
					par.sendMessage(cs, "Unknown plugin '"+args[arg]+"'");
					return true;
				}
				if(clear) {
					if(ignoreList.containsKey(plu)) ignoreList.get(plu).remove(cs.getName());
					par.sendMessage(cs, "Level cleared.");
					return true;
				}
				if(args.length==arg+1) {
					String lvl="";
					switch((getLevel(plu, cs.getName()) & 0xFF00)>>8) {
						case 0x0: 
							lvl="Developer Full and ";
							break;
						case 0x1:
						case 0x10:
						case 0x11:
						case 0x12:
							lvl="Developer Limited and ";
					}
					if((getLevel(plu, cs.getName()) & 0xF0>>4) != (getLevel(plu, cs.getName()) & 0xF))switch(getLevel(plu, cs.getName()) & 0xF0) {
						case 0x0:
							lvl+="Temporarily Full and ";//Temporarily
							break;
						case 0x10:
							lvl+="Temporarily Limited and ";
							break;
						case 0x20:
							lvl+="Temporarily Blocked and ";
							break;
					}
					switch(getLevel(plu, cs.getName()) & 0xF) {
						case 0x0:
							lvl+="Full";
							break;
						case 0x1:
							lvl+="Limited";
							break;
						case 0x2:
							lvl+="Blocked";
							break;
					}
					par.sendMessage(cs, "Your current level for "+plu.getPluginName()+" is "+lvl);
					return true;
				}
				if(firstMatch(args[arg+1].toLowerCase(), new String[]{"f(ull)?"})!=-1) {
					changeLevel(plu, (Player)cs, 0, type, temp, true);
				} else if(firstMatch(args[arg+1].toLowerCase(), new String[]{"l(im(ited)?)?","p(ar(tial)?)?"})!=-1) {
					changeLevel(plu, (Player)cs, 1, type, temp, true);
				} else if(firstMatch(args[arg+1].toLowerCase(), new String[]{"b(lock(ed)?)?"})!=-1) {
					changeLevel(plu, (Player)cs, 2, type, temp, true);
				}
			}
			return true;
		} else if(command.getName().equalsIgnoreCase("talk")) {
			if(args.length<1) {
				par.sendMessage(cs, "Supply a channel and text to talk.");
				return true;
			}
			int chan=firstMatch(args[0].toLowerCase(),new String[]{"none","owner[+]?","admin[+]?","mod[+]?","dev[+]?","guide[+]?","staff[+]?"})-1;
			if(chan>0) {
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
				if(args.length==1) {
					if(!(cs instanceof Player)) {
						par.sendMessage(cs, "Please supply text to talk");
						return true;
					}
					curChannels.put((Player)cs,mode?(-chan-1):(chan+1));
					par.sendMessage(cs, "Moved into channel");
					return true;
				}
				for(int i=1;i<args.length;i++) msg+=args[i]+" ";
				String[] msgArr = new String[chan+1];
				for(int i=0;i<msgArr.length;i++) {
					if(i==msgArr.length-1) {
						msgArr[i]=msg;
					} else if(i==msgArr.length-2 && mode) {
						msgArr[i]="";
					} else msgArr[i]=null;
				}
				par.broadcastMessage(msgArr,par.CHAT_ADMIN,false);
			} else {
				if(chan==-2) {
					par.sendMessage(cs, ChatColor.RED+"Unknown channel!");
				} else par.sendMessage(cs, "Default channel!");
				if(cs instanceof Player) curChannels.remove((Player)cs);
			}
			return true;
		}
		return false;
	}
	@EventHandler (priority=EventPriority.HIGHEST)
	public void playerSendMessage(PlayerChatEvent e) {
		if(curChannels.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			int chan=curChannels.get(e.getPlayer());
			boolean limited=false;
			if(chan<0) {
				limited=true;
				chan=-chan;
			}
			String[] out = new String[chan];
			chan--;
			out[chan]=e.getPlayer().getName()+": "+e.getMessage();
			if(limited) out[chan-1]="";
			par.broadcastMessage(out,par.CHAT_ADMIN,false);
		}
	}
	@EventHandler
	public void playerLogout(PlayerQuitEvent e) {
		curChannels.remove(e.getPlayer());
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
