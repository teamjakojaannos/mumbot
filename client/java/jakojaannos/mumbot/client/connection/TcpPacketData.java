package jakojaannos.mumbot.client.connection;

/**
 * Temporary data storage unit for TCP messages that are either received and queued for handling or queued for sending
 * over network.
 */
class TcpPacketData {
    private short type;
    private byte[] data;

    short getType() {
        return type;
    }

    byte[] getData() {
        return data;
    }

    TcpPacketData(ETcpMessageType type, byte[] data) {
        this.type = (short) type.ordinal();
        this.data = data;
    }
}
