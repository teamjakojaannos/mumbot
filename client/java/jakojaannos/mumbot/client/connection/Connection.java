package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.AbstractMessage;
import jakojaannos.mumbot.client.IConnection;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Connection implements IConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class.getSimpleName());

    private static final long PING_INITIAL_DELAY = 0; // ms
    private static final long PING_RATE = 5000; // ms

    private final TcpChannel tcpChannel;
    private final UdpChannel udpChannel;

    private final ScheduledExecutorService pingService;
    private final Thread handler;

    private boolean cryptValid;
    private boolean hasCrypt;

    @Override
    public boolean isConnected() {
        return tcpChannel.isConnected() && udpChannel.isConnected();
    }

    private boolean isCryptValid() {
        return udpChannel.getCryptState().isValid() && cryptValid;
    }

    public Connection(MumbleClient client) {
        SocketFactory factory;
        try {
            factory = new SocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Could not initialize SocketFactory.");
        }

        udpChannel = new UdpChannel();
        tcpChannel = new TcpChannel(factory, udpChannel);
        handler = new Thread(null, () -> handlerLoop(client), "Connection/Handler");
        pingService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(null, r, "Connection/Ping"));
    }

    @Override
    public void connect(String hostname, int port) {
        tcpChannel.connect(hostname, port);
        udpChannel.connect(hostname, port);
        handler.start();
        pingService.scheduleAtFixedRate(this::sendPing, PING_INITIAL_DELAY, PING_RATE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {
        tcpChannel.disconnect();
        udpChannel.disconnect();
        handler.interrupt();
        pingService.shutdownNow();
    }

    /**
     * Sends a protobuf message trough TCP
     */
    @Override
    public void send(TcpMessageType messageType, AbstractMessage message) {
        byte[] data = messageType.serialize(message);
        TcpChannel.Packet packet = new TcpChannel.Packet((short) messageType.ordinal(), data.length, data);
        tcpChannel.send(packet);
    }

    /**
     * Sends UDP message. If crypto hasn't been validated yet, the packet is tunneled through TCP.
     */
    @Override
    public void send(UdpMessage message) {
        if (hasCrypt && !cryptValid) {
            tcpChannel.send(new TcpChannel.Packet((short) TcpMessageType.UDPTunnel.ordinal(), message.getLength(), message.getData()));
        } else {
            sendUdp(message);
        }
    }

    /**
     * Forces sending over UDP. Normally audio packets won't be sent over UDP until server has validated that the
     * UDP channel is working by answering to our ping packets. This method overrides that requirement and sends the
     * packet anyways.
     */
    @Override
    public void sendUdp(UdpMessage message) {
        udpChannel.send(message);
    }

    void updateUdpCrypto(@Nullable byte[] key, @Nullable byte[] clientNonce, byte[] serverNonce) {
        udpChannel.updateCrypto(key, clientNonce, serverNonce);
        hasCrypt = true;
    }

    private void sendPing() {
        Mumble.Ping ping = Mumble.Ping.newBuilder()
                .setTimestamp(0L) // TODO: set proper timestamp
                .build();

        send(TcpMessageType.Ping, ping);

        // We can/need to send pings even if crypt hasn't been validated yet as validating the crypt involves receiving
        // answer to our pings.
        if (hasCrypt) {
            byte[] data = new byte[2]; // Header + varint
            data[0] = 0x20; // Ping packet header 00100000

            // 0-prefixed varints are treated 7-bit unsigned integers (whole varint fits a single byte)
            // --> just write 0 to the array and we have a valid varint encoded 0-timestamp
            data[1] = 0; // 0-timestamp

            sendUdp(new UdpMessage(data));
        }
    }

    private void handlerLoop(MumbleClient client) {
        LOGGER.trace(Markers.CONNECTION, "Connection handler thread entering loop!");
        main:
        while (isConnected()) {
            while (tcpChannel.hasReceivedPackets()) {

                TcpChannel.Packet packet = tcpChannel.popReceivedPacket();
                if (packet == null) break main;
                if (packet.getType().shouldLog()) {
                    LOGGER.trace(Markers.TCP, "Processing TCP channel packet...");
                }
                packet.getType().handle(client, packet.getData());
            }

            while (udpChannel.hasReceivedPackets()) {
                UdpMessage message = udpChannel.popReceivedPacket();
                if (message == null) break main;

                UdpMessageType type = UdpMessageType.fromRaw(message.getType());
                LOGGER.trace(Markers.UDP, "Processing UDP channel packet of type \"{}\"...", type);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        LOGGER.trace(Markers.CONNECTION, "Connection handler thread leaving loop!");

        disconnect();
    }

    void setCryptValid(boolean cryptValid) {
        this.cryptValid = cryptValid;
    }

    byte[] getEncryptIv() {
        return udpChannel.getCryptState().getEncryptIv();
    }
}
