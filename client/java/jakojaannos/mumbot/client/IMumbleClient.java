package jakojaannos.mumbot.client;

import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

import java.util.List;

/**
 * Headless mumble client. Provides methods for querying user and channel information, sending text messages,
 * registering chat message listeners and sending and receiving voice data.
 */
public interface IMumbleClient {
    IConnection getConnection();

    void connect(String address, int port);

    void disconnect();


    UserManager getUsers();

    int getLocalSession();

    default UserInfo getLocalUser() {
        return getUsers().getBySession(getLocalSession());
    }

    default Channel getCurrentChannel() {
        return getChannels().getById(getLocalUser().getChannelId());
    }


    ChannelManager getChannels();

    void changeChannel(String channelName);


    void registerChatListener(IChatListener listener);

    List<IChatListener> getChatListeners();

    void sendMessage(String message);

    void sendMessage(Channel channel, String message);

    void sendMessage(UserInfo target, String message);
}
