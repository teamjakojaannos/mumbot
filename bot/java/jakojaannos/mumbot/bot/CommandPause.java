package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandPause extends Command {


    public CommandPause(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {
        // pause music
    }

    @Override
    public String getManual() {
        return "- pauses music";
    }

    @Override
    public String getFinisherMessage(){
        return "song paused";
    }
}
