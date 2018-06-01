package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandSkip extends Command {

    public CommandSkip(IMumbleClient client) {
        super(client);
        aliases.add("skip");
    }

    @Override
    public void execute(String args) {

        // print message to channel
        client.sendMessage("song skipped");
    }

    @Override
    public String getManual() {
        return "- skips current song.";
    }

}
