package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandPlay extends Command{


    public CommandPlay(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {
        // continue music

    }

    @Override
    public String getManual() {
        return "- continues paused music";
    }

    @Override
    public String getFinisherMessage(){
        return "song resumed";
    }
}
