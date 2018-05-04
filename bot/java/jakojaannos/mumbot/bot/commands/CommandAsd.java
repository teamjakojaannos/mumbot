package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.MumbleClient;

public class CommandAsd extends Command {

    public CommandAsd(MumbleClient client){
        super(client);
        aliases.add("asd");
    }

    @Override
    public void execute(String args) {
        // add song to queue

        // print message to channel
        client.sendMessage("song added to queue");
    }

    @Override
    public String getManual() {
        return "[URL] - adds song to queue but doesn't display its name.";
    }

}
