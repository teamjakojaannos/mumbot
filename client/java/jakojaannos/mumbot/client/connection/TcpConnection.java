package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.Socket;

public class TcpConnection {
    private final Socket socket;

    private SocketReader reader;
    private SocketWriter writer;

    public TcpConnection(String hostname, int port) throws IOException {
        this.socket = SocketUtil.openSSLSocket(hostname, port);
        if (socket == null) {
            System.err.println("Could not connect, opening SSL socket failed!");
            return;
        }

        this.reader = new SocketReader(socket, this::isRunning);
        this.writer = new SocketWriter(socket, this::isRunning);

        new Thread(reader).start();
        new Thread(writer).start();
        new Thread(this::loop).start();

        final short major = 1;
        final byte minor = 2;
        final byte patch = 19;
        Mumble.Version version = Mumble.Version.newBuilder()
                .setVersion((major << 2) + (minor << 1) + patch)
                .setRelease("mumbot")
                .build();

        writer.queue(new PacketData((short) EMessageType.Version.ordinal(), version.toByteArray()));
    }

    private boolean isRunning() {
        return socket.isConnected() && !socket.isClosed();
    }

    private void loop() {
        while (isRunning()) {
            while (reader.hasPackets()) {
                System.out.println("Iterating inQueue");

                PacketData data = reader.dequeue();
                EMessageType type = EMessageType.fromOrdinal(data.type);

                try {
                    handle(type, data.data);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handle(EMessageType type, byte[] data) throws InvalidProtocolBufferException {
        System.out.printf("Handling message [type=%s]\n", type.name());

        switch (type) {
            case Version:
                handleVersion(Mumble.Version.parseFrom(data));
                break;
            case Authenticate:
                handleAuthenticate(Mumble.Authenticate.parseFrom(data));
                break;
            case ChannelState:
                handleChannelState(Mumble.ChannelState.parseFrom(data));
                break;
            case UserState:
                handleUserState(Mumble.UserState.parseFrom(data));
                break;
            case Ping:
                break;
            case Reject:
                break;
            case ServerSync:
                break;
            case ChannelRemove:
                break;
            case UserRemove:
                break;
            case BanList:
                break;
            case TextMessage:
                break;
            case PermissionDenied:
                break;
            case ACL:
                break;
            case QueryUsers:
                break;
            case ContextActionModify:
                break;
            case ContextAction:
                break;
            case UserList:
                break;
            case VoiceTarget:
                break;
            case PermissionQuery:
                break;
            case CodecVersion:
                break;
            case UserStats:
                break;
            case RequestBlob:
                break;
            case ServerConfig:
                break;
            case SuggestConfig:
                break;
            case CryptSetup:
                // TODO
            case UDPTunnel:
            default:
                // Ignored
                break;
        }
    }

    private void handleAuthenticate(Mumble.Authenticate authenticate) {
        // TODO: initialize crypto
        System.out.println("--- IGNORING AUTHENTICATE PACKET");
    }

    private void handleVersion(Mumble.Version version) {
        System.out.println(String.format("Received version info: %s, %s, %s", version.getRelease(), version.getOsVersion(), version.getOs()));

        final String username = "MumbotReborn";
        final String password = "";
        Mumble.Authenticate authenticate = Mumble.Authenticate.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
        writer.queue(new PacketData((short) EMessageType.Authenticate.ordinal(), authenticate.toByteArray()));
    }

    private void handleChannelState(Mumble.ChannelState channelState) {
        System.out.printf("Received channel state: #%d %s - %s\n", channelState.getChannelId(), channelState.getName(), channelState.getDescription());



        final int id = channelState.getChannelId();

    }

    private void handleUserState(Mumble.UserState userState) {
        System.out.printf("Received user state: #%d (#%d) %s, %s", userState.getSession(), userState.getUserId(), userState.getName(), userState.getComment());
    }

    public void close() throws IOException {
        socket.close();
    }

    public static class PacketData {
        short type;
        byte[] data;

        PacketData(short type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }
}
