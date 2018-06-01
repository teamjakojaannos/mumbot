package jakojaannos.mumbot.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpMessage.class.getSimpleName());

    /**
     * Gets the type (last 3 bits of the 8-bit header) from the header
     */
    byte getType() {
        return (byte) ((data[0] >> 5) & 0x7);
    }

    /**
     * Gets the target (first 5 bits of the 8-bit header) from the header
     */
    public byte getTarget() {
        return (byte) (data[0] & 0x1F);
    }

    byte[] getData() {
        return data;
    }

    private final byte[] data;


    UdpMessage(byte[] data) {
        this.data = data;
    }

    int getLength() {
        return data.length;
    }
}
