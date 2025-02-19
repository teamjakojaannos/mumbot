package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandClear extends Command {

    public CommandClear(IMumbleClient client) {
        super(client);
        aliases.add("clear");
    }

    @Override
    public void execute(String args) {
        // clear queue

        // print message to channel
        client.sendMessage("queue cleared");
    }

    @Override
    public String getManual() {
        return "- clears song queue.";
    }

}
