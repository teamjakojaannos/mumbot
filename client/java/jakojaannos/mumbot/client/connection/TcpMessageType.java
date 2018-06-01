package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.annotation.Nullable;

public enum TcpMessageType {
    // NOTE: DO NOT CHANGE THE ORDER OF THE ENTRIES (unless it for some reason is actually verifiably incorrect)
    //       the ordering must be correct as ordinal is used directly as the packet type
    // @formatter:off
    Version             (new TcpMessageHandler.Version(),           Mumble.Version::parseFrom),
    UDPTunnel           (new TcpMessageHandler.Ignore(),            data -> Mumble.UDPTunnel.getDefaultInstance(), true),
    Authenticate        (null,                                      Mumble.Authenticate::parseFrom),
    Ping                (null,                                      Mumble.Ping::parseFrom, true),
    Reject              (null,                                      Mumble.Reject::parseFrom),
    ServerSync          (new TcpMessageHandler.ServerSync(),        Mumble.ServerSync::parseFrom),
    ChannelRemove       (null,                                      Mumble.ChannelRemove::parseFrom),
    ChannelState        (new TcpMessageHandler.ChannelState(),      Mumble.ChannelState::parseFrom),
    UserRemove          (new TcpMessageHandler.UserRemove(),        Mumble.UserRemove::parseFrom),
    UserState           (new TcpMessageHandler.UserState(),         Mumble.UserState::parseFrom),
    BanList             (null,                                      Mumble.BanList::parseFrom),
    TextMessage         (new TcpMessageHandler.TextMessage(),       Mumble.TextMessage::parseFrom),
    PermissionDenied    (new TcpMessageHandler.PermissionDenied(),  Mumble.PermissionDenied::parseFrom),
    ACL                 (null,                                      Mumble.ACL::parseFrom),
    QueryUsers          (null,                                      Mumble.QueryUsers::parseFrom),
    CryptSetup          (new TcpMessageHandler.CryptSetup(),        Mumble.CryptSetup::parseFrom),
    ContextActionModify (null,                                      Mumble.ContextActionModify::parseFrom),
    ContextAction       (null,                                      Mumble.ContextAction::parseFrom),
    UserList            (null,                                      Mumble.UserList::parseFrom),
    VoiceTarget         (null,                                      Mumble.VoiceTarget::parseFrom),
    PermissionQuery     (null,                                      Mumble.PermissionQuery::parseFrom),
    CodecVersion        (null,                                      Mumble.CodecVersion::parseFrom),
    UserStats           (null,                                      Mumble.UserStats::parseFrom),
    RequestBlob         (null,                                      Mumble.RequestBlob::parseFrom),
    ServerConfig        (null,                                      Mumble.ServerConfig::parseFrom),
    SuggestConfig       (null,                                      Mumble.SuggestConfig::parseFrom);
    // @formatter:on

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageType.class.getSimpleName());
    private static final boolean STOP_SPAM = true;

    private final boolean reduceLogSpam;

    private final ITcpMessageHandler<AbstractMessage> handler;
    private final IMessageSerializer<AbstractMessage> serializer;
    private final IMessageDeserializer<AbstractMessage> deserializer;

    void handle(MumbleClient client, byte[] data) {
        if (handler == null) {
            LOGGER.warn(Markers.TCP, "No handler for TCP Message type \"{}\"", this.toString());
            return;
        }

        try {
            if (!reduceLogSpam || !STOP_SPAM) {
                //noinspection ConstantConditions
                LoggingFunc loggingFunc = reduceLogSpam ? LOGGER::trace : LOGGER::debug;
                loggingFunc.log(Markers.TCP, "Handling TCP message of type \"{}\"", this.toString());
            }
            handler.handle(client, deserializer.deserialize(data));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    byte[] serialize(AbstractMessage message) {
        return this.serializer.serialize(message);
    }

    <TMessage extends AbstractMessage> TcpMessageType(@Nullable ITcpMessageHandler<TMessage> handler, IMessageDeserializer<TMessage> deserializer) {
        this(handler, deserializer, false);
    }

    @SuppressWarnings("unchecked")
    <TMessage extends AbstractMessage> TcpMessageType(@Nullable ITcpMessageHandler<TMessage> handler, IMessageDeserializer<TMessage> deserializer, boolean reduceLogSpam) {
        this.reduceLogSpam = reduceLogSpam;

        this.handler = (ITcpMessageHandler<AbstractMessage>) handler;
        this.serializer = AbstractMessageLite::toByteArray;
        this.deserializer = (IMessageDeserializer<AbstractMessage>) deserializer;
    }

    public static TcpMessageType fromRaw(short type) {
        return values()[type];
    }


    interface IMessageDeserializer<TMessage> {
        TMessage deserialize(byte[] data) throws InvalidProtocolBufferException;
    }

    interface IMessageSerializer<TMessage> {
        byte[] serialize(TMessage message);
    }

    interface LoggingFunc {
        void log(Marker marker, String msg, Object... args);
    }
}


