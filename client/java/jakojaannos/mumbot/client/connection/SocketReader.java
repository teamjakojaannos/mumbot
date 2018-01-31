package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * Handles reading mumble protocol packets from non-blocking {@link SocketChannel}
 */
public class SocketReader {
    private static final int HEADER_LENGTH = 6;
    private static final int MESSAGE_MAX_LENGTH = 8388608; // 8MiB - 1B = 8388608B
    private static final int BUFFER_MAX_CAPACITY = HEADER_LENGTH + MESSAGE_MAX_LENGTH;

    private final SocketChannel channel;

    private boolean hasHeader;
    private short msgType = -1;
    private int msgLength = 0;

    private ByteBuffer buffer;


    public SocketReader(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(BUFFER_MAX_CAPACITY);
    }


    public void doRead(Queue<TcpConnection.PacketData> dataQueue) {
        buffer.clear();

        int nRead;
        try {
            // Dump bytes from the socket channel to the buffer
            nRead = channel.read(buffer);
        } catch (IOException e) {
            System.err.println("Error reading from channel:");
            e.printStackTrace();
            return;
        }

        // Return if there is nothing to read
        if (nRead <= 0) {
            return;
        }

        // We got data, read packets until we hit an incomplete packet
        boolean canReadHeader = !hasHeader && buffer.remaining() >= 6;
        boolean canReadMessage = hasHeader && buffer.remaining() >= msgLength;
        boolean readNext = canReadHeader || canReadMessage;
        while (readNext) {
            if (canReadHeader) {
                doReadHeader();
                hasHeader = true;
            } else /* canReadMessage == true */ {
                doReadMessage(dataQueue);
                hasHeader = false;
            }

            canReadHeader = !hasHeader && buffer.remaining() >= 6;
            canReadMessage = hasHeader && buffer.remaining() >= msgLength;
            readNext = canReadHeader || canReadMessage;
        }

        // Compact the buffer
        buffer.compact();
    }

    /**
     * Reads a 6-byte packet header from the buffer. Implementation can safely assume that buffer has at least 6 bytes
     * of data remaining.
     */
    private void doReadHeader() {
        msgType = buffer.getShort();
        msgLength = buffer.getInt();
    }

    /**
     * Reads message from the buffer. Implementation can safely assume that buffer has at least {@link #msgLength} bytes
     * of data and {@link #msgLength} and {@link #msgType} are set.
     */
    private void doReadMessage(Queue<TcpConnection.PacketData> dataQueue) {
        byte[] messageBytes = new byte[msgLength];
        buffer.get(messageBytes);

        System.out.println("Received message:");
        for (byte messageByte : messageBytes) {
            System.out.print(messageByte + " ");
        }

        dataQueue.add(new TcpConnection.PacketData(msgType, messageBytes));

        System.out.println();
    }
}
