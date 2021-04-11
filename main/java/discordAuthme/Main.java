package discordAuthme;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.xephi.authme.events.LoginEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	public static Main instance;
	public static boolean isSetBotToken = true;
	public static String guildIDMain; 
	public static String botToken;
	public static String notLinkedMessage; 
	public static String inGameSuccessSentLinkMessage; 
	public static String nonMemberOfDiscordServer; 
	public static String failedGetAccountId;
	public static String minimumRequestDelay;
	public static String accountAlreadyLinked;
	public static String successfullyLinked;
	public static String accountNeverLinked;
	public static String successfullyUnLinked;
	public static String discordConfirmMessage;
	public static String discordConfirmMessage2;
	public static String discordRecoverMessage;
	public static String discordRecoverMessage2;
	public static String sentAnRecoverPassword;
	public static String minimumRecoverDelay;
	public static TextComponent messager;
	@Override
    public void onEnable() {
		instance=this;
		this.saveDefaultConfig();
		
		Metrics metrics = new Metrics(this, 7483);
		metrics.addCustomChart(new Metrics.SimplePie("ips", () -> Bukkit.getServer().getIp() ));
		metrics.addCustomChart(new Metrics.SimplePie("versions", () -> Bukkit.getVersion()));
		
		getLogger().info("The plugin is enabling");
		AuthMeHook authMeHook = new AuthMeHook();
		getLogger().info("After authmeHook");
		if (getServer().getPluginManager().isPluginEnabled("AuthMe")) {
			getLogger().info("Authme is enabled");
			getServer().getPluginManager().registerEvents(this, this);
		    // it's safe to get AuthMe's AuthMeApi instance, and so forth...
			authMeHook.initializeAuthMeHook();
			getLogger().info("Initialized authme");
		    File authmeYml = new File("plugins/AuthMe/config.yml");
		    FileConfiguration authmeConfig = YamlConfiguration.loadConfiguration(authmeYml);
		    getLogger().info("Loading authmeConfig");
		    List<String> cmdAllowed = authmeConfig.getStringList("settings.restrictions.allowCommands");
		    getLogger().info("Have taken the cmdAllowed: " + cmdAllowed.size());
		    if(!cmdAllowed.contains("/recover")) {
		    	getLogger().info("Authme config without /recover");
		    	cmdAllowed.add("/recover");
		    	authmeConfig.set("settings.restrictions.allowCommands", cmdAllowed);
		    	saveCustomYml(authmeConfig,authmeYml);
		    }
		    setUpPublicVariables();
		    this.getCommand("link").setExecutor(new cmds());
		    this.getCommand("unlink").setExecutor(new cmds());
		    this.getCommand("confirm").setExecutor(new cmds());
		    this.getCommand("recover").setExecutor(new cmds());
		    getLogger().info("I have set the cmds");
		  }
		if(botToken.equalsIgnoreCase("abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ123456789.abcdefg")) {
			getLogger().info(ChatColor.RED + "Please set the botToken in the config.yml");
			BukkitScheduler scheduler = Bukkit.getScheduler();
	        scheduler.scheduleSyncDelayedTask(JavaPlugin.getProvidingPlugin(Main.class) , new Runnable() {
	            @Override
	            public void run() {
	            	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	            	console.sendMessage(ChatColor.RED + "DiscordAuthme--> Please set the botToken in the config.yml");
	            	console.sendMessage(ChatColor.RED + "DiscordAuthme--> All the instructions are in the config.yml");
	            	isSetBotToken=false;
	            }
	        }, 200L);
		} else {
			try {
				discord.main();
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				isSetBotToken=false;
				e.printStackTrace();
			}
		}
	}
	@Override
    public void onDisable() {
		discord.disableDiscord();
	}
	
	@EventHandler
	public void onLogin(LoginEvent event) {
		//TextComponent mainComponent = new TextComponent( "Here's a question: " );
		//TextComponent subComponent = new TextComponent( "Maybe u r noob?" );
		//subComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Click me!" ).create() ) );
		//subComponent.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/wiki/the-chat-component-api/" ) );
		//mainComponent.addExtra( subComponent );
		//mainComponent.addExtra( " Does that answer your question?" );
		//event.getPlayer().spigot().sendMessage( mainComponent );
		//String mytest = "%name";
		//event.getPlayer().sendMessage(mytest.split("%").length + "");
		
		boolean doLink = getConfig().getBoolean("rememberToLink");
		if(doLink) {
			File customYml = new File(getDataFolder()+"/players.yml");
		    FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
		    Player player = event.getPlayer();
		    String playerName = player.getName();
		    if(customConfig.isBoolean(playerName+".isLinked")) {
			    boolean isLinked = customConfig.getBoolean(playerName+".isLinked");
			    if(!isLinked) {
			    	sendMessage(notLinkedMessage, player);
			    }
		    } else {
		    	customConfig.set(playerName+".isLinked", false);
		    	customConfig.set(playerName+".discordID", "null");
		    	customConfig.set(playerName+".discordName", "null");
		    	sendMessage(notLinkedMessage, player);
		    	saveCustomYml(customConfig,customYml);
		    }
		    
		} else if(!getConfig().isBoolean("rememberToLink")) {
			getConfig().set("rememberToLink", true);
			getConfig().set("test", ChatColor.RED + " test");
			saveConfig();
		}
	  System.out.println(event.getPlayer().getName() + " has logged in! Custom Plugin");
	}
	  public static void saveCustomYml(FileConfiguration ymlConfig, File ymlFile) {
		  try {
		  ymlConfig.save(ymlFile);
		  } catch (IOException e) {
		  e.printStackTrace();
		  }
		 }
	  public static String coloredFormat(String original, @Nullable Player p) {
		 // original = ChatColor.translateAlternateColorCodes('&', original);
		  original = original.replaceAll("&4", "" +ChatColor.RED + "");
		  original = original.replaceAll("&2", "" +ChatColor.GREEN + "");
		  original = original.replaceAll("&6", "" +ChatColor.YELLOW + "");
		  original = original.replaceAll("&a", "" +ChatColor.AQUA + "");
		  original = original.replaceAll("&e", "" +ChatColor.GOLD + "");
		  original = original.replaceAll("&l", "" +ChatColor.BOLD + "");
		  original = original.replaceAll("&g", "" +ChatColor.GRAY + "");
		  original = original.replaceAll("&f", "" +ChatColor.RESET + "");
		  original = original.replaceAll("&v", "" +ChatColor.LIGHT_PURPLE + "");
		  original = original.replaceAll("&vd", "" +ChatColor.DARK_PURPLE + "");
		  if(p!=null) {
			  File customYml = new File(instance.getDataFolder()+"/players.yml");
			  FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
			  String discordPIN = customConfig.getString(p.getName() + ".linkPsw");
			  String discordRecoverPIN = customConfig.getString(p.getName() + ".recoverPsw");
			  original = original.replaceAll("%playername", p.getName());
			  original = original.replaceAll("%discordPIN", discordPIN);
			  original = original.replaceAll("%discordRecoverPIN", discordRecoverPIN);
		  }
		  return original;
	  }
	  public static void setUpPublicVariables() {
		  guildIDMain = instance.getConfig().getString("guildID");
		  botToken=instance.getConfig().getString("botToken");
		  notLinkedMessage = instance.getConfig().getString("messages.notLinked");
		  nonMemberOfDiscordServer = instance.getConfig().getString("messages.nonMemberOfDiscordServer");
		  inGameSuccessSentLinkMessage = instance.getConfig().getString("messages.inGameSuccessSentLinkMessage");
		  failedGetAccountId=instance.getConfig().getString("messages.failedGetAccountId");
		  minimumRequestDelay=instance.getConfig().getString("messages.minimumRequestDelay");
		  accountAlreadyLinked=instance.getConfig().getString("messages.accountAlreadyLinked");
		  successfullyLinked=instance.getConfig().getString("messages.successfullyLinked");
		  accountNeverLinked=instance.getConfig().getString("messages.accountNeverLinked");
		  successfullyUnLinked=instance.getConfig().getString("messages.successfullyUnLinked");
		  discordConfirmMessage=instance.getConfig().getString("messages.discordConfirmMessage");
		  discordConfirmMessage2=instance.getConfig().getString("messages.discordConfirmMessage2");
		  discordRecoverMessage=instance.getConfig().getString("messages.discordRecoverMessage");
		  discordRecoverMessage2=instance.getConfig().getString("messages.discordRecoverMessage2");
		  sentAnRecoverPassword=instance.getConfig().getString("messages.sentAnRecoverPassword");
		  minimumRecoverDelay=instance.getConfig().getString("messages.minimumRecoverDelay");
	  }
	  public static void sendMessage(String message, Player p) {
		  String finale = coloredFormat(message, p);
		  for(int i = 0; i<finale.split("\n").length; i++) {
			  String cache = finale.split("\n")[i];
			  TextComponent messagecache = new TextComponent("");
			  for(int i1 = 0; i1<cache.split(" ").length; i1++) {
				  String cache2 = cache.split(" ")[i1];
				  TextComponent subComponent = null;
				  if(cache2.equalsIgnoreCase("%discordUrl")) {
					  messager = new TextComponent(" " + coloredFormat(instance.getConfig().getString("discordServerMessage"), p));
					  messager.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, instance.getConfig().getString("discordServerUrl")));
					  messager.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
							  new ComponentBuilder(coloredFormat(instance.getConfig().getString("hoverUrlMessage"), p)).create()));
					  subComponent = messager;
				  } else {
					  if(i1==0) {
						  subComponent = new TextComponent(cache2);
					  } else {
						  subComponent = new TextComponent(" " + cache2);
					  }
				  }
				  messagecache.addExtra(subComponent);
			  }
			  p.spigot().sendMessage(messagecache);
		  }
		  

	  }
}
