package jakojaannos.mumbot.bot.commands;

import jakojaannos.mumbot.bot.Command;
import jakojaannos.mumbot.client.MumbleClient;

public class CommandVolume extends Command {


    public CommandVolume(MumbleClient client) {
        super(client);
        aliases.add("volume");
        aliases.add("vol");
    }

    @Override
    protected void execute(String args) {
        // set volume
        try {
            float f = Float.parseFloat(args);
            System.out.println("Vol: " + f);

        } catch (NumberFormatException e) {
            client.sendMessage("Failed to adjust volume.");
            return;
        }
        // print message to channel
        client.sendMessage("Volume adjusted");
    }

    @Override
    public String getManual() {
        return "[0.0 - 1.0] - sets the volume. Use floating points (for example 0.5).";
    }
}
