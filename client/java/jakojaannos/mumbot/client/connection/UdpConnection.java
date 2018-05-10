package jakojaannos.mumbot.client.connection;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UdpConnection {
    private final DatagramSocket socket;

    private final UdpWriter writer;
    private final UdpReader reader;

    public UdpConnection(String hostname, int port) {
        this.socket = createSocket(hostname, port);
        if (socket == null) {
            this.reader = null;
            this.writer = null;

            System.out.println("Error creating UDP socket!");
            return;
        }

        this.reader = new UdpReader(this.socket, this::isConnected);
        this.writer = new UdpWriter(this.socket, this::isConnected);

        new Thread(reader).start();
        new Thread(writer).start();
        new Thread(this::loop).start();
    }

    private void loop() {
        while (isConnected()) {
            while (reader.hasPackets())

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    public void close() {
        socket.close();
    }

    private static DatagramSocket createSocket(String hostname, int port) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(new InetSocketAddress(hostname, port));
            socket.setReceiveBufferSize(1024);
        } catch (SocketException e) {
            socket = null;
        }

        return socket;
    }
}
