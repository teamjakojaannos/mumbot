package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandSkip extends Command {

    public CommandSkip(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {

    }

    @Override
    public String getManual() {
        return "- skips current song";
    }

    public String getFinisherMessage(){
        return "song skipped";
    }

}
