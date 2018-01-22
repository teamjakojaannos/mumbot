package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandClear extends Command {

    public CommandClear(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {
        // clear queue
    }

    @Override
    public String getManual() {
        return "- clears song queue";
    }

    @Override
    public String getFinisherMessage(){
        return "queue cleared";
    }
}
