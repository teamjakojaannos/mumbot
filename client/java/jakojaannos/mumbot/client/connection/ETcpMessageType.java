package jakojaannos.mumbot.client.connection;

public enum ETcpMessageType {
    Version,
    UDPTunnel,
    Authenticate,
    Ping,
    Reject,
    ServerSync,
    ChannelRemove,
    ChannelState,
    UserRemove,
    UserState,
    BanList,
    TextMessage,
    PermissionDenied,
    ACL,
    QueryUsers,
    CryptSetup,
    ContextActionModify,
    ContextAction,
    UserList,
    VoiceTarget,
    PermissionQuery,
    CodecVersion,
    UserStats,
    RequestBlob,
    ServerConfig,
    SuggestConfig;

    public static ETcpMessageType fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
