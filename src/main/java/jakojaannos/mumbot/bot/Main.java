package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        MumbleClient client = new MumbleClient();
        client.connect("saltandrng.net", 64738);

        MusicBot parser = new MusicBot(client);
        client.registerChatListener(parser);

        try {
            Thread.sleep(1000);

            client.changeChannel("Ohjelmointicorner");
            Thread.sleep(500);
            client.sendMessage("Hello programmers!");


            Thread.sleep(500);
            System.out.println("Trying to move to restricted channel.");
            client.changeChannel("Lassin tirkistelycorner");
            Thread.sleep(500);
            client.sendMessage("Hello again programmers!");

            Thread.sleep(500);
            System.out.println("Trying to move to nonexistent channel.");
            Thread.sleep(500);
            client.changeChannel("A non-existing channel");
            Thread.sleep(500);
            client.sendMessage("Still programming?!");

        } catch (InterruptedException e) {
        }



    }
}
