package jakojaannos.mumbot.client.connection;

public enum UdpMessageType {
    AudioCELT,
    Ping,
    AudioSpeex,
    AudioCELTBeta,
    AudioOPUS,
    Unused5,
    Unused6,
    Unused7;

    public static UdpMessageType fromRaw(byte type) {
        return values()[type];
    }
}
