package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.TcpWriter;

public class HandlerCryptSetup implements TcpMessageHandler.IHandler<Mumble.CryptSetup> {

    private final MumbleClient client;

    public HandlerCryptSetup(MumbleClient client) {
        this.client = client;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.CryptSetup message) {
        client.setupCrypt(message.getKey().toByteArray(), message.getClientNonce().toByteArray(), message.getServerNonce().toByteArray());
    }
}
