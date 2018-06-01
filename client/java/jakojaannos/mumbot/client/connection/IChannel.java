package jakojaannos.mumbot.client.connection;

import javax.annotation.Nullable;

public interface IChannel<TPacket> {
    /**
     * Connects the channel to the given host.
     *
     * @param hostname Hostname to connect to
     * @param port     Port to connect to
     */
    void connect(String hostname, int port);

    /**
     * Disconnects from connected host.
     */
    void disconnect();

    /**
     * Queues packet for sending. Note that sending may not happen immediately.
     */
    void send(TPacket packet);

    /**
     * Are there any packets received, waiting for handling.
     */
    boolean hasReceivedPackets();

    /**
     * Gets the oldest packet in the read packet queue. Behavior is undefined if {@link #hasReceivedPackets()} returns
     * false.
     */
    @Nullable
    TPacket popReceivedPacket();
}
