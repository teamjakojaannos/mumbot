package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.Socket;

/**
 * Facilitates the TCP "command channel" connection with the mumble server
 */
public class TcpConnection {
    private final Socket socket;

    private final TcpReader reader;
    private final TcpWriter writer;
    private final TcpKeepalive keepalive;

    private final TcpMessageHandler messageHandler;

    public TcpWriter getWriter() {
        return writer;
    }

    /**
     * Checks if the socket is still connected
     *
     * @return true if socket is open and connected, false otherwise
     */
    private boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    public TcpConnection(TcpMessageHandler messageHandler, String hostname, int port) throws IOException {
        this.socket = SocketUtil.openSSLSocket(hostname, port);
        this.messageHandler = messageHandler;
        if (socket == null) {
            this.reader = null;
            this.writer = null;
            this.keepalive = null;

            System.err.println("Could not connect, opening SSL socket failed!");
            return;
        }

        this.reader = new TcpReader(socket, this::isConnected);
        this.writer = new TcpWriter(socket, this::isConnected);
        this.keepalive = new TcpKeepalive(writer, 15000L, this::isConnected);

        new Thread(reader).start();
        new Thread(writer).start();
        new Thread(keepalive).start();
        new Thread(this::loop).start(); // TODO: Move looping to its own class. Try keep the classes clean!


        final short major = 1; // TODO: Read these from config/set via buildscript
        final byte minor = 0;
        final byte patch = 0;
        Mumble.Version version = Mumble.Version.newBuilder()
                .setVersion((major << 2) + (minor << 1) + patch)
                .setRelease("mumbot")
                .build();

        writer.queue(new PacketData((short) EMessageType.Version.ordinal(), version.toByteArray()));
    }

    private void loop() {
        while (isConnected()) {
            while (reader.hasPackets()) {
                // System.out.println("Iterating inQueue");

                PacketData data = reader.dequeue();
                EMessageType type = EMessageType.fromOrdinal(data.type);

                try {
                    messageHandler.handle(writer, type, data.data);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ignored) {
            }
        }

        synchronized (keepalive) {
            keepalive.notifyAll();
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public static class PacketData {
        short type;
        byte[] data;

        public PacketData(short type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }
}
