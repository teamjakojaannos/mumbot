package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class SocketReaderBase<TMessage> implements Runnable {
    private final Connection connection;
    private final Queue<TMessage> inQueue;
    private final AtomicBoolean hasPackets;

    private boolean terminate;

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

    /**
     * Performs thread-safe check if the reader has queued packets ready for handling.
     *
     * @return true if there are packets in the queue
     */
    boolean hasPackets() {
        return hasPackets.get();
    }

    SocketReaderBase(Connection connection) {
        this.connection = connection;

        this.inQueue = new ArrayDeque<>();
        this.hasPackets = new AtomicBoolean(false);
    }


    /**
     * Pops the first element from the queue. Operation blocks until calling thread claims the lock on inQueue
     */
    TMessage pop() {
        TMessage data;
        synchronized (inQueue) {
            data = inQueue.poll();
            hasPackets.set(!inQueue.isEmpty());
            inQueue.notifyAll();
        }

        return data;
    }

    @Override
    public void run() {
        while (connection.isConnected() && !terminate) {
            TMessage packet = read();
            if (packet == null)
                continue;

            // System.out.println("queuing packet");
            synchronized (inQueue) {
                inQueue.add(packet);
                hasPackets.set(true);
            }
        }
    }

    abstract TMessage read();
}
