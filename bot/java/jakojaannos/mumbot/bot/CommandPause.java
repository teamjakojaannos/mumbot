package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandPause extends Command {


    public CommandPause(MumbleClient client){
        super(client);
        aliases.add("pause");
    }

    @Override
    public void execute(String args) {
        // pause music

        // print message to channel
        client.sendMessage("song paused");
    }

    @Override
    public String getManual() {
        return "- pauses music.";
    }

}
