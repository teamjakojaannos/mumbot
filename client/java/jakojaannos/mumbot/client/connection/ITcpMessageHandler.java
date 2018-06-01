package jakojaannos.mumbot.client.connection;

import com.google.protobuf.AbstractMessage;
import jakojaannos.mumbot.client.MumbleClient;

public interface ITcpMessageHandler<TMessage extends AbstractMessage> {
    void handle(MumbleClient client, TMessage message);
}
