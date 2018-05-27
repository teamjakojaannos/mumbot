package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        MumbleClient client = new MumbleClient();
        client.connect("saltandrng.net", 64738);

        MessageParser parser = new MessageParser(client);
        client.registerChatListener(parser);

        while (!client.isConnected()) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            client.changeChannel("Syötä petoa");
            Thread.sleep(500);

            client.sendMessage("Haistakee kakke");
            Thread.sleep(100);
            //client.disconnect();


        } catch (InterruptedException e) {
        }


    }
}
