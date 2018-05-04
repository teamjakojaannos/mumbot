package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Handles reading mumble protocol packets from non-blocking {@link SocketChannel}
 */
class TcpReader implements Runnable {
    private static final int PREFIX_LENGTH = 6;
    private static final int MESSAGE_MAX_LENGTH = 8388608; // 8MiB - 1B = 8388608B
    private static final int BUFFER_MAX_CAPACITY = PREFIX_LENGTH + MESSAGE_MAX_LENGTH;

    private final Socket socket;
    private final Supplier<Boolean> running;
    private final Deque<TcpConnection.PacketData> inQueue;
    private final AtomicBoolean hasPackets = new AtomicBoolean();

    private ByteBuffer buffer;

    TcpReader(Socket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;

        this.buffer = ByteBuffer.allocate(BUFFER_MAX_CAPACITY);
        this.inQueue = new ArrayDeque<>();
    }

    TcpConnection.PacketData dequeue() {
        TcpConnection.PacketData data;

        synchronized (inQueue) {
            data = inQueue.pollLast();
            hasPackets.set(!inQueue.isEmpty());
            inQueue.notifyAll();
        }

        return data;
    }

    @Override
    public void run() {
        System.out.println("TcpReader entering loop");
        while (running.get() && !doRead())
            ; // NO-OP

        inQueue.notifyAll();
        System.out.println("TcpReader leaving loop");
    }

    private boolean doRead() {
        try {
            InputStream stream = socket.getInputStream();

            // Read prefix
            System.out.println("reading prefix");
            if (readBytes(stream, PREFIX_LENGTH)) {
                return true;
            }
            buffer.flip();

            short msgType = buffer.getShort();
            int msgLength = buffer.getInt();

            buffer.clear();


            // Read message
            System.out.println("reading message");
            if (readBytes(stream, msgLength - buffer.position())) {
                return true;
            }
            buffer.flip();

            byte[] data = new byte[msgLength];
            buffer.get(data);

            buffer.clear();


            System.out.println("queuing packet");
            synchronized (inQueue) {
                System.out.println("read queue packet - claimed lock");
                inQueue.add(new TcpConnection.PacketData(msgType, data));
                hasPackets.set(true);
                inQueue.notifyAll();
            }
            System.out.println("read queue packet - released lock");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

    boolean hasPackets() {
        return hasPackets.get();
    }
}
