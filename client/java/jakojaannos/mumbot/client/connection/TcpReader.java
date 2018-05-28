package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Handles reading mumble protocol packets from command-channel {@link Socket TCP-socket}. Implementation guarantees
 * thread safe behavior for {@link #pop}
 */
class TcpReader extends SocketReaderBase<TcpPacketData> {
    private static final int PREFIX_LENGTH = 6;
    private static final int MESSAGE_MAX_LENGTH = 8388608; // 8MiB - 1B = 8388608B
    private static final int BUFFER_MAX_CAPACITY = PREFIX_LENGTH + MESSAGE_MAX_LENGTH;

    private final Socket socket;
    private ByteBuffer buffer;

    /**
     * Constructs a new instance. Assigns fields to given values and allocates message buffer.
     *
     * @param socket     Socket to read from
     * @param connection connection this reader is bound to
     */
    TcpReader(Socket socket, Connection connection) {
        super(connection);
        this.socket = socket;

        this.buffer = ByteBuffer.allocate(BUFFER_MAX_CAPACITY);
    }

    @Override
    public void run() {
        System.out.println("TcpReader entering loop");
        super.run();
        System.out.println("TcpReader leaving loop");
    }

    @Override
    TcpPacketData read() {
        try {
            InputStream stream = socket.getInputStream();

            // Read prefix
            // System.out.println("reading prefix");
            if (readBytes(stream, PREFIX_LENGTH)) {
                terminate();
                return null;
            }
            buffer.flip();

            short msgType = buffer.getShort();
            int msgLength = buffer.getInt();

            buffer.clear();


            // Read message
            // System.out.println("reading message");
            if (readBytes(stream, msgLength - buffer.position())) {
                terminate();
                return null;
            }
            buffer.flip();

            byte[] data = new byte[msgLength];
            buffer.get(data);

            buffer.clear();

            // Special case for TCP tunneled UDP packet
            if (ETcpMessageType.fromOrdinal(msgType) == ETcpMessageType.UDPTunnel) {
                getConnection().getUdpReader().queue(new UdpMessage(data));
            }

            return new TcpPacketData(ETcpMessageType.fromOrdinal(msgType), data);
        } catch (IOException e) {
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
     * @throws IOException If reading stream throws an exception
     */
    private boolean readBytes(InputStream stream, int n) throws IOException {
        while (buffer.position() < n) {
            int a = stream.read();
            if (a == -1) return true;

            buffer.put((byte) a);
        }
        return false;
    }
}
