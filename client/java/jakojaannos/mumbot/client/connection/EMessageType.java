package jakojaannos.mumbot.client.connection;

public enum EMessageType {
    Version,
    UDPTunnel,
    Authenticate,
    Ping,
    Reject,
    ServerSync,
    ChannelRemove,
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

    public static EMessageType fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
