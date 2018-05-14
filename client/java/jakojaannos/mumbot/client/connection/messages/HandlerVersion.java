package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.*;

/**
 * Receives version information from the server and sends back authenticate message
 */
public class HandlerVersion implements TcpMessageHandler.IHandler<Mumble.Version> {
    @Override
    public void handle(MumbleClient client, Mumble.Version version) {
        // System.out.printf("Received server version info: %s, %s, %s\n", version.getRelease(), version.getOsVersion(), version.getOs());

        final String username = "MumbotReborn"; // TODO: Read these from config/command line
        final String password = "";
        Mumble.Authenticate authenticate = Mumble.Authenticate.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setOpus(true)
                //.setTokens(0, "TokenHere")
                .build();

        client.getConnection().sendTcp(ETcpMessageType.Authenticate, authenticate);
    }
}
