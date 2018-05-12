package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerUserRemove implements TcpMessageHandler.IHandler<Mumble.UserRemove> {
    @Override
    public void handle(MumbleClient client, Mumble.UserRemove userRemove) {
        if (userRemove.getSession() == client.getLocalUser().getSession()) {
            // TODO: disconnect
        }

        // TODO: Remove user
    }
}
