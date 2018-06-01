package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.IMumbleClient;

public class CommandPlay extends Command {


    public CommandPlay(IMumbleClient client) {
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
