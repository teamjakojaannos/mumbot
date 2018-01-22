package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandSong extends Command {

    public CommandSong(MumbleClient client){
        super(client);
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
        return "[URL] - plays a song. Doesn't modify song queue";
    }

}
