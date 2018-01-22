package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    protected final MumbleClient client;

    protected List<String> aliases;

    public Command(MumbleClient client){
        this.client = client;

        this.aliases = new ArrayList<>();
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

    public List<String> getAliases(){
        return aliases;
    }

    public boolean hasAlias(String alias){
        return this.aliases.contains(alias);
    }

}
