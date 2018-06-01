package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

class TcpChannel extends ChannelBase<TcpChannel.Packet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpChannel.class.getSimpleName());

    private static final int HEADER_SIZE = 6; // 2b type + 4b length

    private static final int MESSAGE_MAX_LENGTH = 8388608; // 8MiB - 1B = 8388608B
    private static final int BUFFER_MAX_CAPACITY = HEADER_SIZE + MESSAGE_MAX_LENGTH;

    private final SocketFactory socketFactory;
    private final UdpChannel udpChannel;

    @Nullable
    private Socket socket;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_MAX_CAPACITY);

    TcpChannel(SocketFactory socketFactory, UdpChannel udpChannel) {
        super("TCP");
        this.socketFactory = socketFactory;
        this.udpChannel = udpChannel;
    }

    @Override
    public void connect(String hostname, int port) {
        try {
            socket = socketFactory.createSSLSocket(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        super.connect(hostname, port);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
        }
    }

    @Override
    void write(Packet packet) {
        if (socket == null) {
            throw new IllegalStateException("Tried to write to TCP Channel with an invalid socket");
        }

        if (packet.getType().shouldLog()) {
            LOGGER.trace(Markers.TCP, "TCP Channel writing packet");
        }

        ByteBuffer buffer = ByteBuffer.allocate(packet.length + HEADER_SIZE);
        buffer.putShort(packet.type);
        buffer.putInt(packet.length);
        buffer.put(packet.data);
        buffer.flip();

        try {
            socket.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            if (isConnected()) {
                LOGGER.error(Markers.TCP, "Error writing to TCP Socket output stream: {}", e.toString());
            }
            disconnect(); // If output stream fails, just terminate the connection
        }
    }

    @Nullable
    @Override
    Packet read() {
        if (socket == null) {
            throw new IllegalStateException("Tried to read from TCP Channel with an invalid socket");
        }

        try {
            InputStream stream = socket.getInputStream();

            // Read header
            if (readBytes(stream, HEADER_SIZE)) {
                disconnect();
                return null;
            }
            buffer.flip();

            short messageType = buffer.getShort();
            int messageLength = buffer.getInt();

            buffer.clear();


            // Read message
            if (TcpMessageType.fromRaw(messageType).shouldLog()) {
                LOGGER.trace(Markers.TCP, "Reading TCP Packet");
            }

            if (readBytes(stream, messageLength - buffer.position())) {
                disconnect();
                return null;
            }
            buffer.flip();

            byte[] data = new byte[messageLength];
            buffer.get(data);

            buffer.clear();

            // Special case for TCP tunneled UDP packet
            if (TcpMessageType.fromRaw(messageType) == TcpMessageType.UDPTunnel) {
                LOGGER.trace(Markers.UDP_TUNNEL, "Redirecting TCP Tunneled UDP Packet");
                udpChannel.externalQueue(new UdpMessage(data));
            }

            return new Packet(messageType, messageLength, data);
        } catch (IOException e) {
            // TODO: Handle
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Reads n bytes from the given input stream
     *
     * @param stream input stream to read from
     * @param n      number of bytes to read
     * @return true if stream terminated before n bytes was read. False otherwise
     */
    private boolean readBytes(InputStream stream, int n) {
        while (buffer.position() < n) {
            int a;
            try {
                a = stream.read();
            } catch (IOException e) {
                return true;
            }

            if (a == -1) return true;

            buffer.put((byte) a);
        }
        return false;
    }

    static final class Packet {

        private short type;
        private int length;
        private byte[] data;

        TcpMessageType getType() {
            return TcpMessageType.fromRaw(type);
        }

        byte[] getData() {
            return data;
        }

        Packet(short type, int length, byte[] data) {
            this.type = type;
            this.length = length;
            this.data = data;
        }
    }
}
