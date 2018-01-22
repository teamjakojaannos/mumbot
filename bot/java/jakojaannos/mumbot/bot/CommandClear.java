package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandClear extends Command {

    public CommandClear(MumbleClient client){
        super(client);
    }

    @Override
    public void execute(String args) {
        // clear queue

        // print message to channel
        client.sendMessage("queue cleared");
    }

    @Override
    public String getManual() {
        return "- clears song queue";
    }

}
