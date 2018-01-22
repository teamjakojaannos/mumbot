package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandAdd extends Command {

    public CommandAdd(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {

    }

    @Override
    public String getManual() {
        return "[URL] - adds song to queue";
    }

    @Override
    public String getFinisherMessage(){
        return "song added to queue";
    }
}
