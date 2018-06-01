package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.AbstractMessage;
import jakojaannos.mumbot.client.IChatListener;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.channels.Channel;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TcpMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandler.class.getSimpleName());

    static class Ignore<TMessage extends AbstractMessage> implements ITcpMessageHandler<TMessage> {
        @Override
        public void handle(MumbleClient client, TMessage message) {
            LOGGER.trace(Markers.CLIENT, "Ignoring message of type \"{}\"", message.getClass().getName());
        }
    }

    static class Version implements ITcpMessageHandler<Mumble.Version> {
        @Override
        public void handle(MumbleClient client, Mumble.Version version) {
            LOGGER.debug(Markers.CONNECTION, "Received server version info: {}, {}, {}", version.getRelease(), version.getOsVersion(), version.getOs());

            final String username = client.getUsername();
            final String password = client.getServerPassword();
            Mumble.Authenticate.Builder authenticate = Mumble.Authenticate.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setOpus(true);

            int index = 0;
            for (String token : client.getTokens()) {
                authenticate.setTokens(index++, token);
            }

            client.getConnection().send(TcpMessageType.Authenticate, authenticate.build());
        }
    }

    static class CryptSetup implements ITcpMessageHandler<Mumble.CryptSetup> {
        @Override
        public void handle(MumbleClient client, Mumble.CryptSetup message) {
            LOGGER.debug(Markers.CONNECTION, "Received crypt setup.");

            ((Connection) client.getConnection()).updateUdpCrypto(
                    message.getKey().toByteArray(),
                    message.getClientNonce().toByteArray(),
                    message.getServerNonce().toByteArray());

            // TODO: unify to use the connection ping methods
            byte[] data = new byte[2]; // Header + varint
            data[0] = 0x20; // Ping packet header 00100000

            // 0-prefixed varints are treated 7-bit unsigned integers (whole varint fits a single byte)
            // --> just write 0 to the array and we have a valid varint encoded 0-timestamp
            data[1] = 0; // 0-timestamp

            client.getConnection().send(new UdpMessage(data));
        }
    }

    static class ServerSync implements ITcpMessageHandler<Mumble.ServerSync> {
        @Override
        public void handle(MumbleClient client, Mumble.ServerSync sync) {
            LOGGER.debug(Markers.CLIENT, "Connected to a server! Welcome message:\n{}", sync.getWelcomeText()); // TODO: Provide client access to the message
            LOGGER.debug(Markers.CONNECTION, "Our session id: {}", sync.getSession());

            client.onConnectReady(sync.getSession());
        }
    }

    static class ChannelState implements ITcpMessageHandler<Mumble.ChannelState> {
        @Override
        public void handle(MumbleClient client, Mumble.ChannelState channelState) {
            LOGGER.debug(Markers.CHANNELS, "Received channel state: #{} {}", channelState.getChannelId(), channelState.getName());

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

    static class UserState implements ITcpMessageHandler<Mumble.UserState> {
        @Override
        public void handle(MumbleClient client, Mumble.UserState userState) {
            LOGGER.debug(Markers.USERS, "Received user state: #{}({}) {}", userState.getSession(), userState.getUserId(), userState.getName());

            UserInfo user = client.getUsers().getBySession(userState.getSession());

            if (user == null) {
                user = new UserInfo();
                client.getUsers().addUser(user);
            }

            // @formatter:off
            if (userState.hasSession())         user.setSession(userState.getSession());
            if (userState.hasName())            user.setName(userState.getName());
            if (userState.hasUserId())          user.setUserId(userState.getUserId());
            if (userState.hasChannelId())       user.setChannelId(userState.getChannelId());
            if (userState.hasMute())            user.setMute(userState.getMute());
            if (userState.hasDeaf())            user.setDeaf(userState.getDeaf());
            if (userState.hasSelfMute())        user.setSelf_mute(userState.getSelfMute());
            if (userState.hasSelfDeaf())        user.setSelf_deaf(userState.getSelfDeaf());
            if (userState.hasPluginContext())   user.setPlugin_context(userState.getPluginContext().toByteArray());
            if (userState.hasPluginIdentity())  user.setPlugin_identity(userState.getPluginIdentity());
            if (userState.hasComment())         user.setComment(userState.getComment());
            if (userState.hasHash())            user.setHash(userState.getHash());
            if (userState.hasCommentHash())     user.setComment_hash(userState.getCommentHash().toByteArray());
            if (userState.hasPrioritySpeaker()) user.setPriority_speaker(userState.getPrioritySpeaker());
            if (userState.hasRecording())       user.setRecording(userState.getRecording());
            // @formatter:on
        }
    }

    static class TextMessage implements ITcpMessageHandler<Mumble.TextMessage> {
        @Override
        public void handle(MumbleClient client, Mumble.TextMessage textMessage) {
            LOGGER.debug("Received a text message: \"{0}\"", textMessage.getMessage());
            for (IChatListener listener : client.getChatListeners()) {
                listener.receive(new UserInfo(), textMessage.getMessage());
            }
        }
    }

    static class UserRemove implements ITcpMessageHandler<Mumble.UserRemove> {
        @Override
        public void handle(MumbleClient client, Mumble.UserRemove userRemove) {
            if (userRemove.hasActor() && userRemove.getActor() != userRemove.getSession()) {
                final String reason = userRemove.hasReason() ? userRemove.getReason() : "No reason";
                final String kicked = client.getUsers().getBySession(userRemove.getSession()).getName();
                final String actor = client.getUsers().getBySession(userRemove.getActor()).getName();
                LOGGER.debug(Markers.USERS, "User {} is being kicked by {}: {}", kicked, actor, reason);
            } else {
                final String leaving = client.getUsers().getBySession(userRemove.getSession()).getName();
                LOGGER.debug(Markers.USERS, "User {} is leaving", leaving);
            }

            if (userRemove.getSession() == client.getLocalUser().getSession()) {
                client.getConnection().disconnect();
                return;
            }

            client.getUsers().removeBySession(userRemove.getSession());
        }
    }
}
