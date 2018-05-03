package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.SocketWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerChannelState implements TcpMessageHandler.Handler<Mumble.ChannelState> {
    private final ChannelManager channelManager;

    public HandlerChannelState(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void handle(SocketWriter writer, Mumble.ChannelState channelState) {
        System.out.printf("Received channel state: #%d %s - %s\n", channelState.getChannelId(), channelState.getName(), channelState.getDescription());
    }
}
