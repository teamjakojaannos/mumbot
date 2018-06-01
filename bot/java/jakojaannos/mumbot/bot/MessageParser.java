package jakojaannos.mumbot.bot;


import jakojaannos.mumbot.bot.commands.*;
import jakojaannos.mumbot.client.IChatListener;
import jakojaannos.mumbot.client.IMumbleClient;
import jakojaannos.mumbot.client.users.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class MessageParser implements IChatListener {


    private List<Command> commandList;

    public MessageParser(IMumbleClient client) {
        commandList = new ArrayList<>();

        commandList.add(new CommandHelp(client, commandList));
        commandList.add(new CommandAdd(client));
        commandList.add(new CommandClear(client));
        commandList.add(new CommandCurrent(client));
        commandList.add(new CommandPause(client));
        commandList.add(new CommandPlay(client));
        commandList.add(new CommandQueue(client));
        commandList.add(new CommandSkip(client));
        commandList.add(new CommandSong(client));
        commandList.add(new CommandVolume(client));

    }

    @Override
    public void receive(UserInfo userInfo, String message) {
        if (message.startsWith("!")) {
            // remove "!"
            message = message.substring(1);

            String command = "", arg = "";

            int i = message.indexOf(" ");
            if (i == -1) {
                // no spaces found -> command doesn't have arguments (for example "!pause")
                command = message;
            } else {
                // spaces found, command has arguments "!command args"
                command = message.substring(0, i);
                arg = message.substring(i + 1);
            }

            boolean match = false;

            // try to find matching command
            for (Command comm : commandList) {
                if (comm.hasAlias(command)) {
                    // found a command, execute it (preferably with a pistol)
                    comm.execute(arg);

                    match = true;
                    break;
                }
            }

            if (!match) {
                System.out.println("Invalid command");
            }

        }
    }
}
