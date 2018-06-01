package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandCurrent extends Command {

    public CommandCurrent(IMumbleClient client) {
        super(client);
        aliases.add("current");
    }

    @Override
    public void execute(String args) {
        // get current song, find artist and song name
        String message = "Current song: ";
        // send message to the channel
        client.sendMessage(message);
    }

    @Override
    public String getManual() {
        return "- displays current song.";
    }
}
