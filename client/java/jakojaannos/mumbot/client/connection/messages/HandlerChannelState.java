package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerChannelState implements TcpMessageHandler.IHandler<Mumble.ChannelState> {
    private final ChannelManager channelManager;

    public HandlerChannelState(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.ChannelState channelState) {
        //System.out.printf("Received channel state: #%d %s - %s\n", channelState.getChannelId(), channelState.getName(), channelState.getDescription());
        System.out.printf("Received channel state: #%d %s\n", channelState.getChannelId(), channelState.getName());
        Channel channel = new Channel(channelState.getChannelId());
        channel.setName(channelState.getName());

        channelManager.addChannel(channel);

    }
}
