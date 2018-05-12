package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerServerSync implements TcpMessageHandler.IHandler<Mumble.ServerSync> {
    @Override
    public void handle(MumbleClient client, Mumble.ServerSync sync) {
        System.out.println("Session: " + sync.getSession());
        //System.out.println("Welcome message: " + sync.getWelcomeText());

        client.onConnectReady(sync.getSession());
    }
}
