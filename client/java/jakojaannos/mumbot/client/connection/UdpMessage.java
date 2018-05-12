package jakojaannos.mumbot.client.connection;

public class UdpMessage {
    /**
     * Gets the raw header byte
     */
    public byte getHeader() {
        return header;
    }

    /**
     * Gets the type (last 3 bits of the 8-bit header) from the header
     */
    public byte getType() {
        return (byte) (header >> 5);
    }

    /**
     * Gets the target (first 5 bits of the 8-bit header) from the header
     */
    public byte getTarget() {
        return (byte) (header & 0x1F);
    }

    public byte[] getPayload() {
        return payload;
    }

    private final byte header;
    private final byte[] payload;

    public UdpMessage(byte header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }
}
