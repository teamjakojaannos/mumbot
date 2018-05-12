package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;

import java.util.function.Supplier;

/**
 * Runnable task for sending ping/keepalive messages at set interval to the server.
 */
class TcpKeepalive implements Runnable {
    private final Connection connection;
    private final Supplier<Boolean> running;

    private long pingInterval;

    /**
     * Gets the current ping interval
     *
     * @return current ping interval in milliseconds
     */
    public long getPingInterval() {
        return pingInterval;
    }

    /**
     * Sets the ping interval
     *
     * @param pingInterval ping interval in milliseconds
     */
    public void setPingInterval(long pingInterval) {
        this.pingInterval = pingInterval;
    }


    TcpKeepalive(Connection connection, long pingInterval, Supplier<Boolean> running) {
        this.connection = connection;
        this.running = running;

        this.pingInterval = pingInterval;
    }

    @Override
    public void run() {
        while (running.get()) {
            sendPing();
            doWait();
        }
    }

    private void sendPing() {

        Mumble.Ping ping = Mumble.Ping.newBuilder()
                .setTimestamp(0L) // FIXME: Handle timestamps
                .build();

        connection.sendTcp(ETcpMessageType.Ping, ping);
    }

    private void doWait() {
        synchronized (this) {
            try {
                this.wait(pingInterval);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
