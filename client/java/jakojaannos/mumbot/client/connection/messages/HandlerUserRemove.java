package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerUserRemove extends TcpMessageHandler.Handler<Mumble.UserRemove> {
    public HandlerUserRemove(MumbleClient client) {
        super(client);
    }

    @Override
    public void handle(TcpWriter writer, Mumble.UserRemove userRemove) {
    }
}
