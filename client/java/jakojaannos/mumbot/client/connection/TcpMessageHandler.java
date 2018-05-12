package jakojaannos.mumbot.client.connection;

import com.google.protobuf.InvalidProtocolBufferException;
import jakojaannos.mumbot.client.MumbleClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Relays command-channel messages (packets received from TCP connection) to appropriate handlers.
 */
public class TcpMessageHandler {
    private final Map<ETcpMessageType, DataMapper> dataMappers = new HashMap<>();
    private final Map<ETcpMessageType, MessageMapper> messageMappers = new HashMap<>();
    private final Map<ETcpMessageType, IHandler> handlers = new HashMap<>();

    /**
     * Registers a handler and mappers for given message type.
     */
    public <TMessage> void register(ETcpMessageType type, IHandler<TMessage> handler, DataMapper<TMessage> dataMapper, MessageMapper<TMessage> messageMapper) {
        if (dataMappers.containsKey(type) || handlers.containsKey(type)) {
            System.out.println("Handler already registered for type \"" + type + "\"");
            return;
        }

        dataMappers.put(type, dataMapper);
        messageMappers.put(type, messageMapper);
        handlers.put(type, handler);
    }

    /**
     * Handles the given message. Converts the raw data to a message object using registered data mapper and relays
     * the message to appropriate handler.
     */
    void handle(MumbleClient client, ETcpMessageType type, byte[] data) throws InvalidProtocolBufferException {
        if (!dataMappers.containsKey(type) || !handlers.containsKey(type)) {
            System.out.println("No handler registered for type \"" + type + "\"");
            return;
        }

        handlers.get(type).handle(client, dataMappers.get(type).apply(data));
    }

    /**
     * Converts given message object to byte array using a registered mapper.
     */
    public byte[] toByteArray(ETcpMessageType type, Object message) {
        if (!messageMappers.containsKey(type)) {
            throw new IllegalStateException("No mapper registered for TCP message type \"" + type + "\"!");
        }

        return messageMappers.get(type).apply(message);
    }

    /**
     * Handles message of given type
     */
    public interface IHandler<TMessage> {
        void handle(MumbleClient client, TMessage message);
    }

    /**
     * Maps raw byte data to message type
     */
    public interface DataMapper<TMessage> {
        TMessage apply(byte[] data) throws InvalidProtocolBufferException;
    }

    /**
     * Maps messages to raw data (byte arrays)
     */
    public interface MessageMapper<TMessage> {
        byte[] apply(TMessage message);
    }
}
