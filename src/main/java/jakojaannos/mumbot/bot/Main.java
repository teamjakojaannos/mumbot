package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class Main {
    public static void main(String[] args) {
        System.out.println("adasda");
        MumbleClient client = new MumbleClient();
        client.connect("saltandrng.net", 64738);
    }
}
