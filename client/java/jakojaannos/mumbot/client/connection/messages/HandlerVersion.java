package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.connection.EMessageType;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.connection.TcpConnection;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

/**
 * Receives version information from the server and sends back authenticate message
 */
public class HandlerVersion implements TcpMessageHandler.IHandler<Mumble.Version> {
    @Override
    public void handle(TcpWriter writer, Mumble.Version version) {
        System.out.printf("Received server version info: %s, %s, %s\n", version.getRelease(), version.getOsVersion(), version.getOs());

        final String username = "MumbotReborn"; // TODO: Read these from config
        final String password = "";
        Mumble.Authenticate authenticate = Mumble.Authenticate.newBuilder()
                .setUsername(username)
                .setPassword(password)
                //.setTokens(0, "TokenHere")
                .build();

        writer.queue(new TcpConnection.PacketData((short) EMessageType.Authenticate.ordinal(), authenticate.toByteArray()));
    }
}
