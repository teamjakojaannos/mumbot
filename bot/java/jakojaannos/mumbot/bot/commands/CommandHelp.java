package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.MumbleClient;

import java.util.List;

public class CommandHelp extends Command {

    private List<Command> commandList;

    public CommandHelp(MumbleClient client, List<Command> commandList) {
        super(client);
        this.commandList = commandList;
        aliases.add("help");
    }


    @Override
    public void execute(String args) {
        String message = "Available commands:";

        for (Command command : commandList) {
            // message += "commandName [arg1] - how to use. Aliases: cmd, commandName"
            List<String> aliases = command.getAliases();
            message += "<br>" + aliases.get(0) + " " + command.getManual();

            if (aliases.size() > 1) {
                // list alises
                message += " Aliases: ";

                for (int i = 0; i < aliases.size(); i++) {
                    message += aliases.get(i);
                    // don't add comma on last round
                    if (i != aliases.size() - 1) message += ", ";
                }

            }

        }


        // send it to channel
        client.sendMessage(message);
        System.out.println(message);
    }

    @Override
    public String getManual() {
        return "- displays all commands.";
    }
}
