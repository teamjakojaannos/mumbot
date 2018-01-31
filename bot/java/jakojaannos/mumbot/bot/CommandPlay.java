package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandPlay extends Command{


    public CommandPlay(MumbleClient client){
        super(client);
        aliases.add("play");
    }

    @Override
    public void execute(String args) {
        // continue music

        // print message to channel
        client.sendMessage("song resumed");
    }

    @Override
    public String getManual() {
        return "- continues paused music.";
    }

}
