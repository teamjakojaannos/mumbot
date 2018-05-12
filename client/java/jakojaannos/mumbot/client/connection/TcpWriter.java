package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Runnable task for writing command channel messages to the socket output stream. Implementation guarantees thread-safe
 * FIFO behavior for {@link #queue(TcpPacketData)}-method.
 */
class TcpWriter implements Runnable {
    private final Socket socket;
    private final Supplier<Boolean> running;

    private final Deque<TcpPacketData> outQueue;
    private final AtomicBoolean hasPackets;

    /**
     * Constructs a new instance. Initializes fields to default values.
     *
     * @param socket  TCP socket for writing to
     * @param running Supplier supplying connection status. Task loops until status is false
     */
    TcpWriter(Socket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;

        this.outQueue = new ArrayDeque<>();
        this.hasPackets = new AtomicBoolean(false);
    }

    /**
     * Queues a packet for sending. Blocks until calling thread claims lock on outQueue
     *
     * @param packet packet to queue for writing
     */
    void queue(TcpPacketData packet) {
        synchronized (outQueue) {
            outQueue.add(packet);
            hasPackets.set(true);
            outQueue.notifyAll();
        }
    }

    @Override
    public void run() {
        System.out.println("TcpWriter entering loop");
        while (running.get()) {
            while (hasPackets.get()) {
                TcpPacketData packetData;
                synchronized (outQueue) {
                    packetData = outQueue.pollLast();
                    hasPackets.set(!outQueue.isEmpty());
                    outQueue.notifyAll();
                }

                write(packetData);
            }

            synchronized (outQueue) {
                if (!hasPackets.get()) {
                    try {
                        outQueue.wait(1000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        System.out.println("TcpWriter leaving loop");
    }

    private void write(TcpPacketData packetData) {
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
        }
    }
}
