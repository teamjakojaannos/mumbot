package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.UdpMessage;

public class HandlerCryptSetup implements TcpMessageHandler.IHandler<Mumble.CryptSetup> {
    @Override
    public void handle(MumbleClient client, Mumble.CryptSetup message) {
        client.setupCrypt(message.getKey().toByteArray(), message.getClientNonce().toByteArray(), message.getServerNonce().toByteArray());

        byte[] data = new byte[2]; // Header + varint
        data[0] = 0x20; // Ping packet header 00100000

        // 0-prefixed varints are treated 7-bit unsigned integers (whole varint fits a single byte)
        // --> just write 0 to the array and we have a valid varint encoded 0-timestamp
        data[1] = 0; // 0-timestamp

        System.out.println("=========================================================================================");
        System.out.println(" ENABLING UDP CHANNEL");
        System.out.println("=========================================================================================");
        client.getConnection().sendUdp(new UdpMessage(data));
    }
}
