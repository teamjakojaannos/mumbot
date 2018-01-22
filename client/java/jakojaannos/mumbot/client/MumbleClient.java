package jakojaannos.mumbot.client;

import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.server.ServerInfo;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

/**
 *
 */
public class MumbleClient {
    private final ChannelManager channels = new ChannelManager();
    private final UserManager users = new UserManager();

    private ServerInfo serverInfo;
    private Channel currentChannel;

    public ChannelManager getChannels() {
        return channels;
    }

    public UserManager getUsers() {
        return users;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }


    public MumbleClient() {

    }

    public void connect(String address, int port) {

    }

    public void disconnect() {

    }

    public void changeChannel(Channel channel) {

    }

    public void sendMessage(String message) {

    }

    public void sendMessage(Channel channel, String message) {

    }

    public void sendMessage(UserInfo target, String message) {

    }

    public void registerChatListener(IChatListener listener) {

    }
}
