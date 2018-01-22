package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandSkip extends Command {

    public CommandSkip(MumbleClient client){
        super(client);
        aliases.add("skip");
    }

    @Override
    public void execute(String args) {

        // print message to channel
        client.sendMessage("song skipped");
    }

    @Override
    public String getManual() {
        return "- skips current song.";
    }

}
