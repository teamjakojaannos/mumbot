package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.TcpWriter;

public class HandlerChannelState implements TcpMessageHandler.IHandler<Mumble.ChannelState> {
    private final ChannelManager channelManager;

    public HandlerChannelState(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.ChannelState channelState) {
        //System.out.printf("Received channel state: #%d %s - %s\n", channelState.getChannelId(), channelState.getName(), channelState.getDescription());
        System.out.printf("Received channel state: #%d %s\n", channelState.getChannelId(), channelState.getName());

        Channel channel = channelManager.getById(channelState.getChannelId());

        if (channel == null) {
            channel = new Channel(channelState.getChannelId());
            channelManager.addChannel(channel);
        }

        if (channelState.hasName()) channel.setName(channelState.getName());
        if (channelState.hasDescription()) channel.setDescription(channelState.getDescription());

        if (channelState.hasParent()) {
            Channel parent = channelManager.getById(channelState.getParent());
            if (parent != null) channel.setParent(parent);
        }

    }
}
