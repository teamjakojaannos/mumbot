package jakojaannos.mumbot.client.connection;

import com.google.protobuf.InvalidProtocolBufferException;
import jakojaannos.mumbot.client.IConnection;
import jakojaannos.mumbot.client.MumbleClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Facilitates the connection with the mumble server. Wraps both the TCP and the UDP connections
 */
public class Connection implements IConnection {
    private final Socket tcpSocket;
    private final DatagramSocket udpSocket;

    private final TcpReader tcpReader;
    private final TcpWriter tcpWriter;
    private final TcpKeepalive keepalive;

    private final UdpReader udpReader;
    private final UdpWriter udpWriter;

    private final TcpMessageHandler tcpHandler;
    private final UdpMessageHandler udpHandler;

    private final MumbleClient client;

    private boolean cryptValid;


    /**
     * Initializes UDP channel crypto with given keys and IVs
     */
    public void setupUdpCrypt(byte[] key, byte[] clientNonce, byte[] serverNonce) {
        udpReader.initCipher(key, serverNonce);
        udpWriter.initCipher(key, clientNonce);

        cryptValid = true;
    }

    /**
     * Checks if the socket is still connected
     *
     * @return true if socket is open and connected, false otherwise
     */
    public boolean isConnected() {
        return tcpSocket.isConnected() && !tcpSocket.isClosed();
    }

    public Connection(MumbleClient client, TcpMessageHandler tcpHandler, UdpMessageHandler udpHandler, String hostname, int port) throws IOException {
        this.tcpSocket = SocketUtil.openTcpSslSocket(hostname, port);
        this.udpSocket = SocketUtil.openUdpDatagramSocket();
        if (this.tcpSocket == null || this.udpSocket == null) {
            this.tcpReader = null;
            this.tcpWriter = null;
            this.keepalive = null;
            this.tcpHandler = null;

            this.udpReader = null;
            this.udpWriter = null;
            this.udpHandler = null;

            this.client = null;

            if (tcpSocket != null) tcpSocket.close();
            if (udpSocket != null) udpSocket.close();

            System.err.println("Could not connect, opening sockets failed!");
            return;
        }

        this.client = client;
        this.tcpHandler = tcpHandler;
        this.udpHandler = udpHandler;

        this.tcpReader = new TcpReader(tcpSocket, this);
        this.tcpWriter = new TcpWriter(tcpSocket, this);
        this.keepalive = new TcpKeepalive(this, 15000L);

        this.udpReader = new UdpReader(udpSocket, this);
        this.udpWriter = new UdpWriter(udpSocket, new InetSocketAddress(hostname, port), this);

        new Thread(tcpReader, "TCP Reader").start();
        new Thread(tcpWriter, "TCP Writer").start();
        new Thread(keepalive, "TCP Ping").start();

        new Thread(udpReader, "UDP Reader").start();
        new Thread(udpWriter, "UDP Writer").start();

        new Thread(this::loop).start();
    }

    /**
     * Queues a command channel message for sending
     */
    public void sendTcp(ETcpMessageType type, Object message) {
        tcpWriter.queue(new TcpPacketData(type, tcpHandler.toByteArray(type, message)));
    }

    /**
     * Queues voice channel message for sending
     */
    public void sendUdp(UdpMessage message) {
        udpWriter.queue(message);
    }


    private void loop() {
        while (isConnected()) {
            while (tcpReader.hasPackets()) {
                // System.out.println("Iterating inQueue");

                TcpPacketData data = tcpReader.pop();
                ETcpMessageType type = ETcpMessageType.fromOrdinal(data.getType());

                try {
                    tcpHandler.handle(client, type, data.getData());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            while (udpReader.hasPackets() && cryptValid) {
                UdpMessage message = udpReader.pop();
                udpHandler.handle(message);
            }

            try {
                Thread.sleep(1L);
            } catch (InterruptedException ignored) {
            }
        }

        synchronized (keepalive) {
            keepalive.notifyAll();
        }
    }

    public void close() throws IOException {
        tcpSocket.close();
        udpSocket.close();
    }
}
