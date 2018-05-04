package jakojaannos.mumbot.client.connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class TcpWriter implements Runnable {
    private final Socket socket;
    private final Deque<TcpConnection.PacketData> outQueue;
    private final Supplier<Boolean> running;

    TcpWriter(Socket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;

        this.outQueue = new ArrayDeque<>();
    }

    public void queue(TcpConnection.PacketData packet) {
        synchronized (outQueue) {
            outQueue.add(packet);
            outQueue.notifyAll();
        }
    }

    @Override
    public void run() {
        System.out.println("TcpWriter entering loop");
        while (running.get()) {
            doWrite();
            doWait();
        }
        System.out.println("TcpWriter leaving loop");
    }

    private void doWrite() {
        synchronized (outQueue) {
            while (!outQueue.isEmpty()) {
                // System.out.println("Writing!");
                write(outQueue.pollLast());
            }
        }
    }

    private void write(TcpConnection.PacketData packetData) {
        ByteBuffer buffer = ByteBuffer.allocate(packetData.data.length + 6);
        buffer.putShort(packetData.type);
        buffer.putInt(packetData.data.length);
        buffer.put(packetData.data);

        buffer.flip();
        try {
            socket.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            System.err.println("Error writing to channel:");
            e.printStackTrace();
        }
    }

    private void doWait() {
        synchronized (outQueue) {
            try {
                outQueue.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
