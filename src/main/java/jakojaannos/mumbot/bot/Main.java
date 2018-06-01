package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.IMumbleClient;
import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        IMumbleClient client = new MumbleClient("MumbotReborn", "");
        //client.connect("saltandrng.net", 64738);
        client.connect("localhost", 64738);

        MessageParser parser = new MessageParser(client);
        client.registerChatListener(parser);

        client.changeChannel("Offtopic");
        client.sendMessage("Haistakee kakke");
    }
}
