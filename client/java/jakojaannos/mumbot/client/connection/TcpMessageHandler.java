package jakojaannos.mumbot.client.connection;

import com.google.protobuf.InvalidProtocolBufferException;
import jakojaannos.mumbot.client.MumbleClient;

import java.util.HashMap;
import java.util.Map;

public class TcpMessageHandler {
    private final Map<EMessageType, DataMapper> mappers = new HashMap<>();
    private final Map<EMessageType, IHandler> handlers = new HashMap<>();

    /**
     * Registers handler which implements both the Handler and the DataMapper interfaces
     */
    public <TMessage, THandler extends Handler<TMessage> & DataMapper<TMessage>> void register(EMessageType type, THandler handler) {
        register(type, handler, handler);
    }

    /**
     * Registers a handler and a mapper for given message type.
     */
    public <TMessage> void register(EMessageType type, IHandler<TMessage> handler, DataMapper<TMessage> mapper) {
        if (mappers.containsKey(type) || handlers.containsKey(type)) {
            System.out.println("Handler already registered for type \"" + type + "\"");
            return;
        }

        mappers.put(type, mapper);
        handlers.put(type, handler);
    }

    void handle(TcpWriter writer, EMessageType type, byte[] data) throws InvalidProtocolBufferException {
        if (!mappers.containsKey(type) || !handlers.containsKey(type)) {
            System.out.println("No handler registered for type \"" + type + "\"");
            return;
        }

        handlers.get(type).handle(writer, mappers.get(type).apply(data));
    }


    /**
     * Handles message of given type
     */
    public interface IHandler<TMessage> {
        void handle(TcpWriter writer, TMessage message);
    }

    /**
     * Handles message of given type. Provides access to mumble client
     */
    public static abstract class Handler<TMessage> implements IHandler<TMessage> {

        private final MumbleClient client;

        protected Handler(MumbleClient client) {
            this.client = client;
        }

        protected MumbleClient getClient(){
            return client;
        }

        @Override
        public abstract void handle(TcpWriter writer, TMessage message);
    }

    /**
     * Maps raw byte data to message type
     */
    public interface DataMapper<TMessage> {
        TMessage apply(byte[] data) throws InvalidProtocolBufferException;
    }
}
