package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

import java.util.List;

public class CommandAdd extends Command {

    public CommandAdd(MumbleClient client){
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
        return "[URL] - adds song to queue.";
    }

}
