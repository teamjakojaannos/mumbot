package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

class SocketWriter {
    private final SocketChannel channel;

    SocketWriter(SocketChannel channel) {
        this.channel = channel;
    }

    void doWrite(Queue<TcpConnection.PacketData> outQueue) {
        while (outQueue.peek() != null) {
            write(outQueue.poll());
        }
    }

    private void write(TcpConnection.PacketData packetData) {
        ByteBuffer buffer = ByteBuffer.allocate(packetData.data.length + 6);
        buffer.putShort(packetData.type);
        buffer.putInt(packetData.data.length);
        buffer.put(packetData.data);

        buffer.flip();
        try {
            channel.write(buffer);
        } catch (IOException e) {
            System.err.println("Error writing to channel:");
            e.printStackTrace();
        }
    }
}
