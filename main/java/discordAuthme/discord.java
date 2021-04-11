package discordAuthme;

import java.io.File;

import javax.security.auth.login.LoginException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class discord extends ListenerAdapter{
	private static JDA jda;
	public static void main() throws LoginException
    {
        jda = new JDABuilder(AccountType.BOT)
            .setToken(Main.botToken)
            .addEventListeners(new discord())
            .build();
        try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.isFromGuild()) {
        if(!event.getMember().getUser().isBot()) {
        	Message msg = event.getMessage();
        		if (msg.getContentRaw().equals("!ping"))
                {
            		event.getChannel().sendMessage("Tried").queue();
                    //MessageChannel channel = event.getChannel();
                    //long time = System.currentTimeMillis();
                } else if (msg!=null) {
                	//String tag = event.getAuthor().getAsTag();
                	//event.getChannel().sendMessage(event.getAuthor().getId()).queue();
                	//User d = jda.getUserByTag(tag);
                	//.openPrivateChannel().queue((channel) ->
                    //{
                    //    channel.sendMessage("ciao").queue();
                    //});
                	//event.getChannel().sendMessage("Hi, it's a test!").queue();
                }
        	}
        	
        } else {
        	File customYml = new File(Main.instance.getDataFolder()+"/players.yml");
        	FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);
        	String original = customConfig.getString("_ids."+event.getAuthor().getId()+".pending");
        	String idpending = customConfig.getString(original+".discordID");
        	if(event.getAuthor().getId().equalsIgnoreCase(idpending)) {
        		event.getChannel().sendMessage("Hi " + original).queue();
        	}
        }
    }
	
	public static void sendMessage(String idDiscord, String Message) {
		User d1 = jda.getUserById(idDiscord);
		d1.openPrivateChannel().queue((channel) ->
        {
        	for(int i = 0; i<Message.split("\n").length; i++) {
				channel.sendMessage(Message.split("\n")[i]).queue();
        	}
        });
	}
	
	public static String getIdByTAG(String nameTag) {
		try {
			User d1 = jda.getUserByTag(nameTag);
			if(jda.getGuildById(Main.guildIDMain).isMember(d1)) {
				String id = d1.getId();
				return id;
			} else {
				return "nonmember";
			}
			}
			catch(Exception e) {
			  //  Block of code to handle errors
				return "failed";
			}
	}
	public static void disableDiscord() {
		jda.shutdownNow();
	}

}
