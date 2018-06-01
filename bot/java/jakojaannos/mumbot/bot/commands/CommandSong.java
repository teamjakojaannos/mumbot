package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandSong extends Command {

    public CommandSong(IMumbleClient client) {
        super(client);
        aliases.add("song");
    }

    @Override
    public void execute(String args) {
        // sets current song to the one specified in arguments
        // don't touch queue

        // print message to channel
        client.sendMessage("playing song");
    }

    @Override
    public String getManual() {
        return "[URL] - plays a song. Doesn't modify song queue.";
    }

}
