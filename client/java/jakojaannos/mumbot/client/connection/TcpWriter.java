package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Runnable task for writing command channel messages to the SSL socket output stream.
 */
class TcpWriter extends SocketWriterBase<TcpPacketData> {
    private final Socket socket;

    /**
     * Constructs a new instance. Assigns fields to default values.
     */
    TcpWriter(Socket socket, Connection connection) {
        super(connection);

        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("TcpWriter entering loop");
        super.run();
        System.out.println("TcpWriter leaving loop");
    }

    @Override
    void write(TcpPacketData packetData) {
        ByteBuffer buffer = ByteBuffer.allocate(packetData.getData().length + 6);
        buffer.putShort(packetData.getType());
        buffer.putInt(packetData.getData().length);
        buffer.put(packetData.getData());

        buffer.flip();
        try {
            socket.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            System.err.println("Error writing to channel:");
            e.printStackTrace();
            terminate();
        }
    }
}
