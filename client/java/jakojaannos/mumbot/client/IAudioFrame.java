package jakojaannos.mumbot.client;

/**
 * Provides a single Opus-encoded audio frame
 */
public interface IAudioFrame {
    long getSize();

    long getSequenceNumber();

    boolean isEndOfTransmission();

    byte[] getData();

    /**
     * Session Id identifying who is talking on incoming packets. Not needed on audio input.
     */
    default long getSessionId() {
        return -1;
    }
}
