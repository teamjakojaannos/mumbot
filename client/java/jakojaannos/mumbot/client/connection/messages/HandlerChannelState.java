package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;

public class HandlerChannelState implements TcpMessageHandler.IHandler<Mumble.ChannelState> {
    @Override
    public void handle(MumbleClient client, Mumble.ChannelState channelState) {
        //System.out.printf("Received channel state: #%d %s - %s\n", channelState.getChannelId(), channelState.getName(), channelState.getDescription());
        System.out.printf("Received channel state: #%d %s\n", channelState.getChannelId(), channelState.getName());

        Channel channel = client.getChannels().getById(channelState.getChannelId());

        if (channel == null) {
            channel = new Channel(channelState.getChannelId());
            client.getChannels().addChannel(channel);
        }

        if (channelState.hasName()) channel.setName(channelState.getName());
        if (channelState.hasDescription()) channel.setDescription(channelState.getDescription());

        if (channelState.hasParent()) {
            Channel parent = client.getChannels().getById(channelState.getParent());
            if (parent != null) channel.setParent(parent);
        }

    }
}
