package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.SocketWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerUserRemove extends TcpMessageHandler.Handler<Mumble.UserRemove> {
    public HandlerUserRemove(MumbleClient client) {
        super(client);
    }

    @Override
    public void handle(SocketWriter writer, Mumble.UserRemove userRemove) {
    }
}
