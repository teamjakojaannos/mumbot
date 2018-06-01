package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.AbstractMessage;
import jakojaannos.mumbot.client.IAudioFrame;
import jakojaannos.mumbot.client.IConnection;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.util.VarInt;
import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
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
    private final Thread incomingHandler;
    private final Thread outgoingHandler;

    private boolean cryptValid;
    private boolean hasCrypt;

    @Override
    public boolean isConnected() {
        return tcpChannel.isConnected() && udpChannel.isConnected();
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
        incomingHandler = new Thread(null, () -> incomingHandlerLoop(client), "Connection/Handler/In");
        outgoingHandler = new Thread(null, () -> outgoingHandlerLoop(client), "Connection/Handler/Out");
        pingService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(null, r, "Connection/Ping"));
    }

    @Override
    public void connect(String hostname, int port) {
        tcpChannel.connect(hostname, port);
        udpChannel.connect(hostname, port);
        incomingHandler.start();
        pingService.scheduleAtFixedRate(this::sendPing, PING_INITIAL_DELAY, PING_RATE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {
        tcpChannel.disconnect();
        udpChannel.disconnect();
        incomingHandler.interrupt();
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

        // We can/must send pings even if crypt hasn't been validated yet as validating the crypt involves receiving
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

    private void incomingHandlerLoop(MumbleClient client) {
        LOGGER.trace(Markers.CONNECTION, "Connection incoming handler thread entering loop!");
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
                switch (type) {
                    case Ping:
                        this.cryptValid = true;
                        break;
                    case AudioOPUS:
                        LOGGER.trace(Markers.AUDIO, "Received Opus audio frame!");

                        UdpAudioPacket packet = new UdpAudioPacket();
                        ByteBuffer buffer = ByteBuffer.wrap(message.getData());
                        packet.deserialize(buffer);
                        if (client.getOutputHandler() != null) {
                            client.getOutputHandler().receiveFrame(packet);
                        }
                        break;
                    case AudioCELT:
                    case AudioSpeex:
                    case AudioCELTBeta:
                        LOGGER.error(Markers.UDP, "Received unsupported audio packet!");
                        break;
                    case Unused5:
                    case Unused6:
                    case Unused7:
                    default:
                        LOGGER.error(Markers.UDP, "Received unknown UDP packet type!");
                        break;
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        LOGGER.trace(Markers.CONNECTION, "Connection incoming handler thread leaving loop!");

        disconnect();
    }

    private void outgoingHandlerLoop(MumbleClient client) {
        // TODO: Recycle buffers, scrap current VarInt implementation and replace with stream/buffer style impl
        LOGGER.trace(Markers.CONNECTION, "Connection outgoing Handler thread entering loop!");
        ByteBuffer buffer = ByteBuffer.allocate(0x3FFF);
        while (isConnected()) {
            if (client.getInputHandler() != null) {
                while (client.getInputHandler().canProvideAudio()) {
                    IAudioFrame frame = client.getInputHandler().popAudioFrame();
                    byte header = (byte) 0x80;

                    UdpAudioPacket packet = new UdpAudioPacket(
                            header,
                            VarInt.encode(client.getLocalSession()),
                            VarInt.encode(frame.getSequenceNumber()),
                            new UdpAudioPacket.Payload(frame.getSize(), frame.getData()),
                            new float[]{0f, 0f, 0f},
                            frame.isEndOfTransmission());

                    packet.serialize(buffer);
                    buffer.flip();

                    byte[] data = new byte[buffer.limit()];
                    System.arraycopy(buffer.array(), 0, data, 0, data.length);
                    send(new UdpMessage(data));
                }

                synchronized (client.getInputHandler().getLock()) {
                    try {
                        client.getInputHandler().getLock().wait();
                    } catch (InterruptedException ignored) {
                    }
                }

            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }

        LOGGER.trace(Markers.CONNECTION, "Connection outgoing Handler thread leaving loop!");
    }

    byte[] getEncryptIv() {
        return udpChannel.getCryptState().getEncryptIv();
    }
}
