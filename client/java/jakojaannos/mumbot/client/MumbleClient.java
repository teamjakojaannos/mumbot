package jakojaannos.mumbot.client;

import MumbleProto.Mumble;

public class MumbleClient {
    public MumbleClient() {
        System.out.println(Mumble.Authenticate.newBuilder().setOpus(true).build());
    }
}
