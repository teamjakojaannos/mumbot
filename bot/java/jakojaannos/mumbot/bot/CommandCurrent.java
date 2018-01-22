package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandCurrent extends Command {

    public CommandCurrent(MumbleClient client){
        super(client);
        aliases.add("current");
    }

    @Override
    public void execute(String args) {
        // get current song, find artist and song name
        String message = "Current song: ";
        // send message to the channel
        client.sendMessage(message);
    }

    @Override
    public String getManual() {
        return "- displays current song.";
    }
}
