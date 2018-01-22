package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

import java.util.HashMap;

public class CommandHelp extends Command {

    private HashMap<String,Command> commandMap;

    public CommandHelp(MumbleClient client, HashMap<String, Command> commandMap){
        super(client);
        this.commandMap = commandMap;
    }


    @Override
    public void doExecute(String args) {
        String message = "Available commands:";
        for (String key: commandMap.keySet()) {
            message += "\n" + key + " " + commandMap.get(key).getManual();
        }


        // send it to channel
        client.sendMessage(message);
        System.out.println(message);
    }

    @Override
    public String getManual() {
        return "- displays all commands";
    }
}
