package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.TcpWriter;

public class HandlerServerSync implements TcpMessageHandler.IHandler<Mumble.ServerSync> {

    private final MumbleClient client;

    public HandlerServerSync(MumbleClient client) {
        this.client = client;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.ServerSync sync) {
        System.out.println("Session: " + sync.getSession());
        //System.out.println("Welcome message: " + sync.getWelcomeText());


        client.setSession(sync.getSession());
        client.updateChannel();
    }
}
