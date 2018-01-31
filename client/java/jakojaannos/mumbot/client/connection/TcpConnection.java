package jakojaannos.mumbot.client.connection;

import MumbleProto.Mumble;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpConnection {
    private final Queue<PacketData> inQueue = new LinkedBlockingQueue<>();
    private final Queue<PacketData> outQueue = new LinkedBlockingQueue<>();

    private final SocketChannel channel;
    private final SocketReader reader;
    private final SocketWriter writer;

    public TcpConnection(String hostname, int port) throws IOException {
        this.channel = SocketChannel.open(new InetSocketAddress(hostname, port));
        this.channel.configureBlocking(false);

        this.reader = new SocketReader(channel);
        this.writer = new SocketWriter(channel);

        final short major = 1;
        final byte minor = 2;
        final byte patch = 3;
        Mumble.Version builder = Mumble.Version.newBuilder()
                .setVersion(major << 2 + minor << 1 + patch)
                .setRelease("mumbot")
                .build();


        outQueue.add(new PacketData((short) 0, builder.toByteArray()));
    }

    public void loopRead() {
        reader.doRead(inQueue);
    }

    public void loopWrite() {
        writer.doWrite(outQueue);
    }

    public void close() throws IOException {
        channel.close();
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
