package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public abstract class Command {

    protected final MumbleClient client;

    public Command(MumbleClient client){
        this.client = client;
    }

    public final void execute(String args){
        doExecute(args);
        String msg = getFinisherMessage();

        if(!msg.equals("")){
            System.out.println(msg);
            client.sendMessage(msg);
        }

    }

    protected abstract void doExecute(String args);

    /**
     * return info on how to use the command
     * for example "help !song" could return:
     * "!song [URL] - plays a song"
     *
     * !help command uses these to print infos
     * @return
     */
    public abstract String getManual();

    /**
     * this message will be displayed to the channel after doExecute() has been done.
     * For example user types in "!add (song url)", this song is added to the list and
     * a message "song added to list" will be sent to channel.
     *
     * You can leave this empty to not display a message after doExecute
     * @return
     */
    public String getFinisherMessage(){
        return "";
    }
}
