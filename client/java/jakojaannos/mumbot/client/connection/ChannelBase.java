package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class ChannelBase<TPacket> implements IChannel<TPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelBase.class.getSimpleName());

    private final String name;

    private final Thread reader;
    private final ExecutorService writer;

    private final BlockingQueue<TPacket> readQueue = new LinkedBlockingQueue<>();

    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    boolean isConnected() {
        return isConnected.get();
    }

    ChannelBase(String name) {
        this.name = name;
        this.reader = new Thread(null, this::doReadLoop, "Connection/" + name + "/Reader");
        this.writer = Executors.newSingleThreadExecutor(r -> new Thread(null, r, "Connection/" + name + "/Writer"));
    }

    abstract void write(TPacket packet);

    @Nullable
    abstract TPacket read();

    @Override
    public void connect(String hostname, int port) {
        LOGGER.trace(Markers.CONNECTION, "Channel \"{}\" connecting to {}:{}", name, hostname, Objects.toString(port));
        isConnected.set(true);
        reader.start();
    }

    @Override
    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        LOGGER.trace(Markers.CONNECTION, "Channel \"{}\" disconnecting!", name);
        isConnected.set(false);
        writer.shutdown(); // NOTE: Do not call .shutdownNow() as the TCP channel needs to send UserRemove messages etc
        reader.interrupt();
    }

    @Override
    public void send(TPacket packet) {
        writer.submit(() -> write(packet));
    }

    @Override
    public boolean hasReceivedPackets() {
        return !readQueue.isEmpty();
    }

    @Nullable
    @Override
    public TPacket popReceivedPacket() {
        if (!hasReceivedPackets())
            throw new IllegalStateException("Read queue cannot be empty! Check queue status before access!");

        try {
            return readQueue.take();
        } catch (InterruptedException e) {
            LOGGER.warn(Markers.CONNECTION, "Could not pop packet from queue, thread got interrupted: {}", e.toString());
            return null;
        }
    }

    private void doReadLoop() {
        LOGGER.trace(Markers.CONNECTION, "Channel \"{}\" entering read loop", name);
        while (isConnected.get()) {
            TPacket packet = read();
            if (packet == null) {
                if (isConnected()) {
                    LOGGER.warn(Markers.CONNECTION, "Channel \"{}\" reader encountered a null packet while connected!", name);
                }

                continue;
            }

            try {
                readQueue.put(packet);
            } catch (InterruptedException e) {
                LOGGER.warn(Markers.CONNECTION, "Channel \"{}\" Read-thread interrupted while queuing packet to readQueue: {}", name, e.toString());
            }
        }
        LOGGER.trace(Markers.CONNECTION, "Channel \"{0}\" exiting read loop", name);
    }
}
