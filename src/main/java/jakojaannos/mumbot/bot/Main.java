package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        MumbleClient client = new MumbleClient();
        client.connect("localhost", 64738);
    }
}
