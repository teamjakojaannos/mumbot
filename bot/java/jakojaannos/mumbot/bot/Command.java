package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public abstract class Command {

    protected final MumbleClient client;

    public Command(MumbleClient client){
        this.client = client;
    }


    protected abstract void execute(String args);

    /**
     * return info on how to use the command
     * for example "help !song" could return:
     * "!song [URL] - plays a song"
     *
     * !help command uses these to print infos
     * @return
     */
    public abstract String getManual();

}
