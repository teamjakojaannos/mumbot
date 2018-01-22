package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandSong extends Command {

    public CommandSong(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {
        // sets current song to the one specified in arguments
        // don't touch queue
    }

    @Override
    public String getManual() {
        return "[URL] - plays a song. Doesn't modify song queue";
    }

    public String getFinisherMessage(){
        return "playing song";
    }
}
