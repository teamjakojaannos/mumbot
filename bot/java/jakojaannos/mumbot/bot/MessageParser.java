package jakojaannos.mumbot.bot;


import jakojaannos.mumbot.client.IChatListener;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.users.UserInfo;

import java.util.HashMap;

public class MessageParser implements IChatListener {


    private HashMap<String, Command> commandMap;

    public MessageParser(MumbleClient client){
        this.commandMap = new HashMap<>();

        commandMap.put("help", new CommandHelp(client, commandMap));
        commandMap.put("add", new CommandAdd(client));
        commandMap.put("clear", new CommandClear(client));
        commandMap.put("current", new CommandCurrent(client));
        commandMap.put("pause", new CommandPause(client));
        commandMap.put("play", new CommandPlay(client));
        commandMap.put("queue", new CommandQueue(client));
        commandMap.put("skip", new CommandSkip(client));
        commandMap.put("song", new CommandSong(client));

    }

    @java.lang.Override
    public void receive(UserInfo userInfo, String message) {
        if(message.startsWith("!")){
            // remove "!"
            message = message.substring(1);

            String command = "", arg = "";

            int i = message.indexOf(" ");
            if(i == -1){
                // no spaces found -> command doesn't have arguments (for example "!pause")
                command = message;
            }else{
                // spaces found, command has arguments "!command args"
                command = message.substring(0,i);
                arg = message.substring(i+1);
            }

            if(commandMap.containsKey(command)){
                commandMap.get(command).execute(arg);
            }else{
                // send message to channel "Invalid command"
                System.out.println("Invaliidi_kommando");
            }

        }
    }
}
