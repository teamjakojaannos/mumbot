package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandPause extends Command {


    public CommandPause(IMumbleClient client) {
        super(client);
        aliases.add("pause");
    }

    @Override
    public void execute(String args) {
        // pause music

        // print message to channel
        client.sendMessage("song paused");
    }

    @Override
    public String getManual() {
        return "- pauses music.";
    }

}
