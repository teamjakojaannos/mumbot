package jakojaannos.mumbot.client;

import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.TcpConnection;
import jakojaannos.mumbot.client.server.ServerInfo;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class MumbleClient {
    private final ChannelManager channels = new ChannelManager();
    private final UserManager users = new UserManager();
    private final List<IChatListener> chatListeners = new ArrayList<>();

    private ServerInfo serverInfo;
    private Channel currentChannel;
    private AtomicBoolean connected;

    private TcpConnection connection;
    private Thread loopThread;

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
        connected = new AtomicBoolean(false);
    }

    public void connect(String address, int port) {
        try {
            connection = new TcpConnection(address, port);
            connected.set(true);
            loopThread = new Thread(this::loop);
            loopThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected.set(false);
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
        chatListeners.add(listener);
    }

    private void loop() {
        while (connected.get()) {
            connection.loopRead();

            connection.loopWrite();
        }
    }
}
