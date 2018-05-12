package jakojaannos.mumbot.client;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.Connection;
import jakojaannos.mumbot.client.connection.ETcpMessageType;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.UdpMessageHandler;
import jakojaannos.mumbot.client.connection.messages.*;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * see {@link IMumbleClient}
 */
public class MumbleClient implements IMumbleClient {
    private final ChannelManager channels = new ChannelManager();
    private final UserManager users = new UserManager();
    private final List<IChatListener> chatListeners = new ArrayList<>();

    private final TcpMessageHandler tcpMessageHandler = new TcpMessageHandler();
    private final UdpMessageHandler udpMessageHandler = new UdpMessageHandler();

    private AtomicBoolean connected;
    private Connection connection;

    private int session;

    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public ChannelManager getChannels() {
        return channels;
    }

    @Override
    public UserManager getUsers() {
        return users;
    }

    @Override
    public int getLocalSession() {
        return session;
    }

    public MumbleClient() {
        this.session = -1;
        this.connected = new AtomicBoolean(false);

        if (Security.addProvider(new BouncyCastleProvider()) != -1) {
            System.out.println("Successfully installed BC provider");
        } else {
            System.out.println("BC provider already exists. This is not an error.");
        }

        registerTcpMessageHandlers();
    }

    @Override
    public void connect(String address, int port) {
        try {
            connection = new Connection(this, tcpMessageHandler, udpMessageHandler, address, port);

            final short major = 1; // TODO: Read these from config/set via buildscript
            final byte minor = 0;
            final byte patch = 0;
            Mumble.Version version = Mumble.Version.newBuilder()
                    .setVersion((major << 2) + (minor << 1) + patch)
                    .setRelease("mumbot")
                    .build();

            connection.sendTcp(ETcpMessageType.Version, version);
        } catch (IOException e) {
            e.printStackTrace();
            this.connected.set(false);
        }
    }

    @Override
    public void disconnect() {
        if (!connected.get()) {
            System.out.println("Could not disconnect: Not connected to a server");
            return;
        }

        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected.set(false);
    }


    @Override
    public void changeChannel(String channelName) {
        System.out.println("Trying to change channel to '" + channelName + "'.");
        Channel channel = channels.getByName(channelName);

        if (channel == null) {
            System.err.printf("Error! No matching channels for argument '%s'.\n", channelName);
            return;
        }

        Mumble.UserState userState = Mumble.UserState.newBuilder()
                .setChannelId(channel.getId())
                .build();
        connection.sendTcp(ETcpMessageType.UserState, userState);
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(getCurrentChannel(), message);
    }

    @Override
    public void sendMessage(Channel channel, String message) {
        System.out.printf("Sending message to channel: '%s', id = %d\n", channel.getName(), channel.getId());
        Mumble.TextMessage msg = Mumble.TextMessage.newBuilder()
                .setMessage(message)
                .addChannelId(channel.getId())
                .build();
        connection.sendTcp(ETcpMessageType.TextMessage, msg);
    }

    @Override
    public void sendMessage(UserInfo target, String message) {
        System.out.printf("Sending message to user: '%s', session = %d\n", target.getName(), target.getSession());
        Mumble.TextMessage msg = Mumble.TextMessage.newBuilder()
                .setMessage(message)
                .addSession(target.getUserId())
                .build();
        connection.sendTcp(ETcpMessageType.TextMessage, msg);
    }


    @Override
    public void registerChatListener(IChatListener listener) {
        chatListeners.add(listener);
    }

    @Override
    public List<IChatListener> getChatListeners() {
        return new ArrayList<>(chatListeners);
    }


    private void registerTcpMessageHandlers() {
        tcpMessageHandler.register(ETcpMessageType.Version, new HandlerVersion(), Mumble.Version::parseFrom, Mumble.Version::toByteArray);
        tcpMessageHandler.register(ETcpMessageType.CryptSetup, new HandlerCryptSetup(), Mumble.CryptSetup::parseFrom, Mumble.CryptSetup::toByteArray);
        tcpMessageHandler.register(ETcpMessageType.ChannelState, new HandlerChannelState(), Mumble.ChannelState::parseFrom, Mumble.ChannelState::toByteArray);
        tcpMessageHandler.register(ETcpMessageType.UserState, new HandlerUserState(), Mumble.UserState::parseFrom, Mumble.UserState::toByteArray);
        tcpMessageHandler.register(ETcpMessageType.ServerSync, new HandlerServerSync(), Mumble.ServerSync::parseFrom, Mumble.ServerSync::toByteArray);

        tcpMessageHandler.register(ETcpMessageType.TextMessage, new HandlerTextMessage(), Mumble.TextMessage::parseFrom, Mumble.TextMessage::toByteArray);


        // TODO: Properly handle these messages (stubs are here to prevent exceptions due to missing handlers)
        tcpMessageHandler.register(ETcpMessageType.Ping, (client, o) -> {}, Mumble.Ping::parseFrom, Mumble.Ping::toByteArray);
        tcpMessageHandler.register(ETcpMessageType.Authenticate, (client, o) -> {}, Mumble.Authenticate::parseFrom, Mumble.Authenticate::toByteArray);
    }

    // TODO: Figure out something to hide these from the public interface
    public void onConnectReady(int session) {
        this.session = session;
        this.connected.set(true);
    }

    public void setupCrypt(byte[] key, byte[] clientNonce, byte[] serverNonce) {
        connection.setupUdpCrypt(key, clientNonce, serverNonce);
    }
}
