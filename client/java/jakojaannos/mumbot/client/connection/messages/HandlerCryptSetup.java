package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerCryptSetup implements TcpMessageHandler.IHandler<Mumble.CryptSetup> {
    @Override
    public void handle(MumbleClient client, Mumble.CryptSetup message) {
        client.setupCrypt(message.getKey().toByteArray(), message.getClientNonce().toByteArray(), message.getServerNonce().toByteArray());
    }
}
