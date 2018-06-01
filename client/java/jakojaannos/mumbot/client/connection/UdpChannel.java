package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;

public class UdpChannel extends ChannelBase<UdpMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpChannel.class.getSimpleName());

    private static final int MAX_PACKET_SIZE = 2048;

    private final DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
    private final CryptState cryptState = new CryptState();

    @Nullable
    private DatagramSocket socket;

    @Nullable
    private InetAddress address;
    private int port;

    CryptState getCryptState() {
        return cryptState;
    }

    UdpChannel() {
        super("UDP");
    }

    @Override
    public void connect(String hostname, int port) {
        try {
            address = InetAddress.getByName(hostname);
            socket = new DatagramSocket();
        } catch (UnknownHostException e) {
            LOGGER.warn(Markers.CONNECTION, "Could not resolve host");
            return;
        } catch (SocketException e) {
            LOGGER.warn(Markers.CONNECTION, "UDP Socket could not be bound!");
            return;
        }
        assert address != null;

        this.port = port;
        socket.connect(address, port);
        super.connect(hostname, port);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (socket != null) {
            socket.close();
            //socket.disconnect();
            socket = null;
        }
    }

    @Override
    void write(UdpMessage message) {
        if (socket == null || !isConnected()) {
            LOGGER.error(Markers.UDP, "Tried to write to UDP with invalid socket!");
            throw new IllegalStateException("Tried to write to UDP with invalid socket");
        }

        if (!cryptState.isValid()) {
            LOGGER.error(Markers.UDP, "Tried to write to UDP with invalid crypt state");
            throw new IllegalStateException("Tried to write to UDP with invalid crypt state");
        }

        byte[] encrypted = new byte[message.getLength() + 4];
        cryptState.encrypt(message.getData(), encrypted, message.getLength());
        DatagramPacket datagramPacket = new DatagramPacket(encrypted, encrypted.length);
        datagramPacket.setAddress(address);
        datagramPacket.setPort(port);

        LOGGER.trace(Markers.UDP, "Sending datagram packet of type \"{}\"", UdpMessageType.fromRaw(message.getType()));
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    UdpMessage read() {
        if (socket == null) {
            LOGGER.error(Markers.UDP, "Tried to read packet with invalid UDP socket");
            return null;
        }

        try {
            socket.receive(packet);
        } catch (IOException e) {
            if (isConnected()) {
                LOGGER.warn(Markers.UDP, "An IOException occurred while receiving UDP datagram packet: {}", e.toString());
            }
            return null;
        }

        final byte[] buffer = packet.getData();
        byte[] encrypted = new byte[packet.getLength()];
        System.arraycopy(buffer, packet.getOffset(), encrypted, 0, packet.getLength());

        byte[] plain = new byte[encrypted.length];
        cryptState.decrypt(encrypted, plain, encrypted.length);

        return new UdpMessage(plain);
    }

    void updateCrypto(@Nullable byte[] key, @Nullable byte[] clientNonce, byte[] serverNonce) {
        cryptState.setKey(key, clientNonce, serverNonce);
    }
}
