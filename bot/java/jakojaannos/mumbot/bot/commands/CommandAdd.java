package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandAdd extends Command {

    public CommandAdd(IMumbleClient client) {
        super(client);
        aliases.add("add");
    }

    @Override
    public void execute(String args) {
        // add song to queue

        // print message to channel
        client.sendMessage("song added to queue");
    }

    @Override
    public String getManual() {
        return "[URL] - adds song to queue and displays its name.";
    }

}
