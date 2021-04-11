package discordAuthme;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class cmds implements CommandExecutor, TabExecutor{
	public boolean onCommand(CommandSender sender,  Command cmd, String arg2,
			 String[] arg3) {
		Player p = ((Player) sender);
		if(!Main.isSetBotToken) {
			p.sendMessage(ChatColor.AQUA + "Tell your administrator to set the discord bot token");
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("link")) {
			if(arg3.length<1 || arg3.length>1) {
				p.sendMessage(ChatColor.RED + "Correct usage:");
				p.sendMessage(ChatColor.GRAY + "/link name#0000");
				return true;
			} else {
				String name = arg3[0];
				if(name.split("#").length==2) {
					if(Main.guildIDMain.toString().equals("000000000000000000")) {
						p.sendMessage(ChatColor.YELLOW + "Say to your administrator to enter the discord Guild ID"
								+ " in the config");
						return true;
					}
					File customYml = new File(Main.instance.getDataFolder()+"/players.yml");
				    FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
				    if(customConfig.getString(p.getName()+".isLinked").equals("null") || 
				    		customConfig.getString(p.getName()+".isLinked").equals("false")) {
				    	if(customConfig.isSet(p.getName() + ".lastLink")) {
					    	double lastLink = customConfig.getDouble(p.getName() + ".lastLink");
					    	double now = System.currentTimeMillis() - lastLink;
					    	if(now>20*60*1000) {
					    		execute(customYml, customConfig, p, name);
					    	} else {
					    		Main.sendMessage(Main.minimumRequestDelay, p);
					    	}
					    } else {
					    	execute(customYml, customConfig, p, name);
					    }
				    } else {
				    	Main.sendMessage(Main.accountAlreadyLinked, p);
				    }
				    
				} else {
					p.sendMessage(ChatColor.RED + "Please specify the discord ID:");
					p.sendMessage(ChatColor.GRAY + "/link name#0000");
					return true;
				}
			}
		} else if(cmd.getName().equalsIgnoreCase("confirm")) {
			if(arg3.length>0) {
				File customYml = new File(Main.instance.getDataFolder()+"/players.yml");
			    FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
			    boolean isLinked = customConfig.getBoolean(p.getName()+".isLinked");
			    if(!isLinked) {
			    	String linkID = customConfig.getString(p.getName()+".linkPsw");
			    	int numero = linkID.length();
			    	if(numero==6) {
			    		if(linkID.equalsIgnoreCase(arg3[0])) {
			    			Main.sendMessage(Main.successfullyLinked, p);
			    			customConfig.set(p.getName()+".isLinked", true);
			    			Main.saveCustomYml(customConfig, customYml);
			    		}
			    	}
			    } else {
			    	Main.sendMessage(Main.accountAlreadyLinked, p);
			    }
			} else {
				p.sendMessage(ChatColor.RED + "Incorrect usage:");
				p.sendMessage(ChatColor.GRAY + "/confirm 123456");
			}
		} else if(cmd.getName().equalsIgnoreCase("recover")) {
			if(arg3.length>0) {
				String discordTag = arg3[0];
				File customYml = new File(Main.instance.getDataFolder()+"/players.yml");
			    FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
			    boolean isLinked = customConfig.getBoolean(p.getName()+".isLinked");
			    if(isLinked) {
			    	int numero = discordTag.length();
			    	if(discordTag.split("#").length==2) {
			    		double lastRecoverLink;
			    		if(!customConfig.isSet(p.getName() + ".lastRecoverLink")) {
			    			lastRecoverLink = System.currentTimeMillis() - 13*60*1000;
			    		} else {
			    			lastRecoverLink = customConfig.getDouble(p.getName() + ".lastRecoverLink");
			    		}
				    	double now = System.currentTimeMillis() - lastRecoverLink;
				    	if(now>12*60*1000) {
				    		String id = customConfig.getString(p.getName()+".discordID");
					    	String discordID = discord.getIdByTAG(discordTag);
					    	if(id.equals(discordID)) {
					    		Random r = new Random();
					    		String alphabet = "ABCDEFGHILMNOPQRSTUVZ";
					    	    String numbers = "0123456789";
					    	    String finale = "";
					    	    for (int i = 0; i < 3; i++) {
					    	        finale = finale+(alphabet.charAt(r.nextInt(alphabet.length())));
					    	    }
					    	    for (int i = 0; i < 3; i++) {
					    	        finale = finale+(numbers.charAt(r.nextInt(numbers.length())));
					    	    }
					    	    customConfig.set(p.getName() + ".lastRecoverLink", System.currentTimeMillis());
					    		customConfig.set(p.getName()+".recoverPsw", finale);
					    		Main.saveCustomYml(customConfig, customYml);
					    		Main.sendMessage(Main.coloredFormat(Main.sentAnRecoverPassword, p), p);
					    	    discord.sendMessage(discordID, Main.coloredFormat(Main.discordRecoverMessage, p));
					    	    discord.sendMessage(discordID, Main.coloredFormat(Main.discordRecoverMessage2, p));
					    	}
				    	} else {
				    		Main.sendMessage(Main.coloredFormat(Main.minimumRecoverDelay, p), p);
				    		return true;
				    	}
			    	} else if (numero==6) {
			    		String recoverId = customConfig.getString(p.getName()+".recoverPsw");
			    		if(recoverId.equals(discordTag)) {
			    			AuthMeHook.authMeApi.forceUnregister(p);
			    			customConfig.set(p.getName()+".recoverPsw", "nowisunused");
			    			Main.saveCustomYml(customConfig, customYml);
			    		}
			    	} else {
			    		Main.sendMessage(Main.coloredFormat("&4Correct usage: \n&g/recover name#0000", p), p);
			    	}
			    } else {
			    	Main.sendMessage(Main.accountNeverLinked, p);
			    }
			}
		} else if(cmd.getName().equalsIgnoreCase("unlink")) {
			File customYml = new File(Main.instance.getDataFolder()+"/players.yml");
		    FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
		    boolean isLinked = customConfig.getBoolean(p.getName()+".isLinked");
		    if(isLinked) {
		    	customConfig.set(p.getName()+".isLinked", "false");
		    	customConfig.set(p.getName()+".linkPsw", "disabled");
		    	Main.sendMessage(Main.successfullyUnLinked, p);
		    	Main.saveCustomYml(customConfig, customYml);
		    } else {
		    	Main.sendMessage(Main.accountNeverLinked, p);
		    }
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1,
			String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return null;
	}
	public void recover() {
		
	}
	public boolean execute(File customYml, FileConfiguration customConfig, Player p, String name) {
		customConfig.set(p.getName() + ".lastLink", System.currentTimeMillis());
		if (discord.getIdByTAG(name).equalsIgnoreCase("failed")) {
			Main.sendMessage(Main.failedGetAccountId, p);
			return false;
		} else if (discord.getIdByTAG(name).equalsIgnoreCase("nonmember")) {
			Main.sendMessage(Main.nonMemberOfDiscordServer, p);
			return false;
		}
		discord.sendMessage(discord.getIdByTAG(name), Main.coloredFormat(Main.discordConfirmMessage, p));
		//discord.sendMessage(discord.getIdByTAG(name), "Hi there!, here's your code:");
		customConfig.set(p.getName() + ".discordID", discord.getIdByTAG(name));
		customConfig.set(p.getName() + ".discordName", name);
		customConfig.set("_ids."+discord.getIdByTAG(name) + ".pending", p.getName());
		Random r = new Random();
	    String alphabet = "0123456789";
	    String finale = "";
	    for (int i = 0; i < 6; i++) {
	        finale = finale+(alphabet.charAt(r.nextInt(alphabet.length())));
	    }
		Main.sendMessage(Main.inGameSuccessSentLinkMessage, p);
	    customConfig.set(p.getName() + ".linkPsw", finale);
	    Main.saveCustomYml(customConfig, customYml);
	    discord.sendMessage(discord.getIdByTAG(name), Main.coloredFormat(Main.discordConfirmMessage2, p));

	    return true;
	}
}
