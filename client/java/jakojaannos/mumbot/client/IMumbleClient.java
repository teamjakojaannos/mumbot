package jakojaannos.mumbot.client;

import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

/**
 * Headless mumble client. Provides methods for querying user and channel information, sending text messages,
 * registering chat message listeners and sending and receiving voice data.
 */
public interface IMumbleClient {

    IConnection getConnection();

    default void connect(String address, int port) {
        connect(address, port, "");
    }

    void connect(String address, int port, String password);

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

    default void sendMessage(String message) {
        sendMessage(getCurrentChannel(), message);
    }

    void sendMessage(Channel channel, String message);

    void sendMessage(UserInfo target, String message);

    void setServerPassword(String serverPassword);

    void addToken(String token);

    default boolean isConnected() {
        return getConnection().isConnected();
    }
}
