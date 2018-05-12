package jakojaannos.mumbot.client.connection;

public enum EUdpMessageType {
    CELT,
    Ping,
    Speex,
    BetaCELT,
    OPUS,
    UNKNOWN;

    public static EUdpMessageType fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal > 7) {
            throw new IllegalStateException("UDP message type ordinal out of bounds!");
        }
        return ordinal >= 5 ? UNKNOWN : EUdpMessageType.values()[ordinal];
    }
}
