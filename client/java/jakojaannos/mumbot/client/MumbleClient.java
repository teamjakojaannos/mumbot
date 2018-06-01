package jakojaannos.mumbot.client;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.channels.ChannelManager;
import jakojaannos.mumbot.client.connection.Connection;
import jakojaannos.mumbot.client.connection.TcpMessageType;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;
import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * see {@link IMumbleClient}
 */
public class MumbleClient implements IMumbleClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MumbleClient.class.getSimpleName());

    private static final long CONNECT_TIMEOUT = 10000; // ms


    private static final String VERSION_PREFIX = "mumbot";
    private static final String VERSION_RELEASE = "ALPHA";
    private static final short VERSION_MAJOR = 0;
    private static final byte VERSION_MINOR = 1;
    private static final byte VERSION_PATCH = 0;

    private final ChannelManager channels = new ChannelManager();
    private final UserManager users = new UserManager();
    private final List<IChatListener> chatListeners = new ArrayList<>();

    private final IConnection connection;
    private final String username;

    private int session;
    private String serverPassword;
    private Set<String> tokens = new TreeSet<>();
    private IAudioOutputHandler outputHandler;
    private IAudioInputHandler inputHandler;

    @Override
    public IConnection getConnection() {
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
        this("Mumbot", "");
    }

    public MumbleClient(String username, String serverPassword) {
        this.serverPassword = serverPassword;
        this.session = -1;
        this.username = username;
        this.connection = new Connection(this);
    }

    /**
     * Connects to a server. Blocks until a ServerSync message is received or connection timeout is reached.
     */
    @Override
    public void connect(String hostname, int port, String password) {
        if (isConnected()) {
            LOGGER.warn(Markers.CLIENT, "Cannot connect: Already connected to a server!");
            return;
        }

        LOGGER.info(Markers.CLIENT, "Connecting to {}:{}", hostname, port);
        connection.connect(hostname, port);

        Mumble.Version version = Mumble.Version.newBuilder()
                .setVersion((VERSION_MAJOR << 16) | (VERSION_MINOR << 8) | VERSION_PATCH)
                .setRelease(VERSION_PREFIX + "-" + VERSION_RELEASE)
                .build();

        connection.send(TcpMessageType.Version, version);

        // Wait for ServerSync
        synchronized (this) {
            try {
                this.wait(CONNECT_TIMEOUT);
            } catch (InterruptedException e) {
                LOGGER.warn(Markers.CONNECTION, "Main thread was interrupted before ServerSync was received! This may result in undefined behavior! Cause: {}", e.toString());
            }
        }
    }

    @Override
    public void disconnect() {
        if (!isConnected()) {
            LOGGER.warn(Markers.CLIENT, "Cannot disconnect: Not connected to a server!");
            return;
        }

        LOGGER.info(Markers.CLIENT, "Client disconnecting...");
        Mumble.UserRemove userRemove = Mumble.UserRemove.newBuilder()
                .setSession(session)
                .build();

        connection.send(TcpMessageType.UserRemove, userRemove);
        connection.disconnect();
    }


    @Override
    public void changeChannel(String channelName) {
        if (!isConnected()) {
            LOGGER.warn(Markers.CLIENT, "Cannot change channel: Not connected to a server!");
            return;
        }

        LOGGER.info(Markers.CLIENT, "Trying to change channel to \"{}\"", channelName);
        Channel channel = channels.getByName(channelName);

        if (channel == null) {
            LOGGER.warn(Markers.CLIENT, "Error! No matching channels for name \"{}\"", channelName);
            return;
        }

        Mumble.UserState userState = Mumble.UserState.newBuilder()
                .setChannelId(channel.getId())
                .build();
        connection.send(TcpMessageType.UserState, userState);

        // Wait for UserState or PermissionDenied
        synchronized (this) { // TODO: Instead of using "this" as a lock, use separate lock object
            try {
                this.wait();
            } catch (InterruptedException e) {
                LOGGER.warn(Markers.CHANNELS, "Main thread was interrupted before confirmation was received! This may result in undefined behavior! Cause: {}", e.toString());
            }
        }
    }

    @Override
    public void sendMessage(Channel channel, String message) {
        if (!isConnected()) {
            LOGGER.warn(Markers.CLIENT, "Cannot send message: Not connected to a server!");
            return;
        }

        LOGGER.info(Markers.CLIENT, "To channel \"{}\": {}", channel.getName(), message);
        Mumble.TextMessage msg = Mumble.TextMessage.newBuilder()
                .setMessage(message)
                .addChannelId(channel.getId())
                .build();
        connection.send(TcpMessageType.TextMessage, msg);
    }

    @Override
    public void sendMessage(UserInfo target, String message) {
        if (!isConnected()) {
            LOGGER.warn(Markers.CLIENT, "Cannot send message: Not connected to a server!");
            return;
        }

        LOGGER.info("To user \"{}\": {}", target.getName(), message);
        Mumble.TextMessage msg = Mumble.TextMessage.newBuilder()
                .setMessage(message)
                .addSession(target.getUserId())
                .build();
        connection.send(TcpMessageType.TextMessage, msg);
    }


    @Override
    public void registerChatListener(IChatListener listener) {
        LOGGER.debug(Markers.CLIENT, "Registering a new chat listener of type \"{}\"", listener.getClass().getName());
        chatListeners.add(listener);
    }

    @Override
    public void setServerPassword(String serverPassword) {
        LOGGER.debug(Markers.CLIENT, "Setting server password");
        this.serverPassword = serverPassword;
    }

    @Override
    public void addToken(String token) {
        LOGGER.debug(Markers.CLIENT, "Adding a token");
        this.tokens.add(token);
    }


    public List<IChatListener> getChatListeners() {
        return new ArrayList<>(chatListeners);
    }

    public Iterable<String> getTokens() {
        return tokens;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void onConnectReady(int session) {
        this.session = session;
        LOGGER.debug(Markers.CONNECTION, "Connected to the server. Local session is {} and we are currently on channel {}", session, getCurrentChannel().getName());
    }

    public void triggerNotify() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    public String getUsername() {
        return session == -1 ? username : getLocalUser().getName();
    }

    public IAudioOutputHandler getOutputHandler() {
        return outputHandler;
    }

    public IAudioInputHandler getInputHandler() {
        return inputHandler;
    }
}
