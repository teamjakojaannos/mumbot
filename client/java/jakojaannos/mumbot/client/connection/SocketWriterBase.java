package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runnable task for writing messages to an output stream. Implementation guarantees thread-safe FIFO behavior for
 * {@link #queue(TMessage)}-method.
 */
abstract class SocketWriterBase<TMessage> implements Runnable {
    private final Connection connection;
    private boolean terminate;

    private final AtomicBoolean hasPackets;
    private final Queue<TMessage> outQueue;

    void terminate() {
        terminate = true;
        if (connection.isConnected()) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    SocketWriterBase(Connection connection) {
        this.connection = connection;

        this.hasPackets = new AtomicBoolean(false);
        this.outQueue = new ArrayDeque<>();
    }

    /**
     * Queues a packet for sending. Blocks until calling thread claims lock on outQueue
     *
     * @param packet packet to queue for writing
     */
    void queue(TMessage packet) {
        synchronized (outQueue) {
            outQueue.add(packet);
            hasPackets.set(true);
            outQueue.notifyAll();
        }
    }

    @Override
    public void run() {
        while (connection.isConnected() && !terminate) {
            while (hasPackets.get()) {
                TMessage packetData;
                synchronized (outQueue) {
                    packetData = outQueue.poll();
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
    }

    abstract void write(TMessage packetData);
}
