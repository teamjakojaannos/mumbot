package jakojaannos.mumbot.client;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.EMessageType;
import jakojaannos.mumbot.client.connection.TcpConnection;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.messages.*;
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

    private final TcpMessageHandler tcpMessageHandler = new TcpMessageHandler();

    private ServerInfo serverInfo;
    private Channel currentChannel;
    private AtomicBoolean connected;

    private int session = -1;

    private TcpConnection tcpConnection;
    //private UdpConnection udpConnection;

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

        registerTcpMessageHandlers();
    }

    public void connect(String address, int port) {
        try {
            tcpConnection = new TcpConnection(tcpMessageHandler, address, port);
            connected.set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            tcpConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected.set(false);
    }


    public void setSession(int session) {
        this.session = session;
    }

    public int getSession() {
        return session;
    }

    public void updateChannel() {
        UserInfo userInfo = users.getBySession(getSession());
        if (userInfo == null) {
            System.out.println("Something went wrong...");
            return;
        }

        Channel channel = channels.getById(userInfo.getChannelId());
        if (channel == null) {
            System.out.println("Something went wrong down here.");
            return;
        }
        currentChannel = channel;

    }

    public void changeChannel(String channelName) {
        System.out.println("Trying to change channel to '" + channelName + "'.");
        Channel channel = channels.getByName(channelName);

        if (channel == null) {
            System.err.printf("Error! No matching channels for argument '%s'.\n", channelName);
            return;
        }

        //currentChannel = channel; // FIXME: If server rejects the move, we don't really move! ==> This needs to be set in UserState message handler

        Mumble.UserState userState = Mumble.UserState.newBuilder()
                .setChannelId(channel.getId())
                .build();
        tcpConnection.getWriter().queue(new TcpConnection.PacketData((short) EMessageType.UserState.ordinal(), userState.toByteArray()));
    }

    public void sendMessage(String message) {
        System.out.printf("Sending message to channel: '%s', id = %d\n", currentChannel.getName(), currentChannel.getId());
        Mumble.TextMessage msg = Mumble.TextMessage.newBuilder()
                .setMessage(message)
                .addChannelId(currentChannel.getId())
                .build();
        tcpConnection.getWriter().queue(new TcpConnection.PacketData((short) EMessageType.TextMessage.ordinal(), msg.toByteArray()));
    }

    public void sendMessage(Channel channel, String message) {

    }

    public void sendMessage(UserInfo target, String message) {

    }

    public void registerChatListener(IChatListener listener) {
        chatListeners.add(listener);
    }

    public List<IChatListener> getChatListeners() {
        return new ArrayList<IChatListener>(chatListeners);
    }


    private void registerTcpMessageHandlers() {
        tcpMessageHandler.register(EMessageType.Version, new HandlerVersion(), Mumble.Version::parseFrom);
        tcpMessageHandler.register(EMessageType.ChannelState, new HandlerChannelState(channels), Mumble.ChannelState::parseFrom);
        tcpMessageHandler.register(EMessageType.UserState, new HandlerUserState(users, this), Mumble.UserState::parseFrom);
        tcpMessageHandler.register(EMessageType.TextMessage, new HandlerTextMessage(this), Mumble.TextMessage::parseFrom);
        tcpMessageHandler.register(EMessageType.ServerSync, new HandlerServerSync(this), Mumble.ServerSync::parseFrom);

        // Ignored messages
        tcpMessageHandler.register(EMessageType.Authenticate, (w, msg) -> {
        }, Mumble.Authenticate::parseFrom);
    }
}
