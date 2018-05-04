package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        MumbleClient client = new MumbleClient();
        client.connect("saltandrng.net", 64738);

        MessageParser parser = new MessageParser(client);
        client.registerChatListener(parser);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
        }
        client.changeChannel("Ohjelmointicorner");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e){
        }

        client.sendMessage("aaa<br>bbb<br />ccc!!!");

    }
}
