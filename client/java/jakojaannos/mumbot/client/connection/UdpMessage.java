package jakojaannos.mumbot.client.connection;

public class UdpMessage {
    /**
     * Gets the raw header byte
     */
    public byte getHeader() {
        return data[0];
    }

    /**
     * Gets the type (last 3 bits of the 8-bit header) from the header
     */
    public byte getType() {
        return (byte) ((getHeader() >> 5) & 0x7);
    }

    /**
     * Gets the target (first 5 bits of the 8-bit header) from the header
     */
    public byte getTarget() {
        return (byte) (getHeader() & 0x1F);
    }

    public int payloadSize() {
        return data.length - 1;
    }

    public void copyPayload(byte[] target) {
        if (target.length != payloadSize())
            throw new IllegalArgumentException("target.length must be equal to payload size!");

        System.arraycopy(data, 1, target, 0, target.length);
    }

    public byte[] getData() {
        return data;
    }

    private final byte[] data;

    public UdpMessage(byte[] data) {
        this.data = data;
    }
}
