package jakojaannos.mumbot.client;

import com.google.protobuf.AbstractMessage;
import jakojaannos.mumbot.client.connection.TcpMessageType;
import jakojaannos.mumbot.client.connection.UdpMessage;

/**
 *
 */
public interface IConnection {
    boolean isConnected();

    void connect(String hostname, int port);

    void disconnect();

    void send(TcpMessageType messageType, AbstractMessage message);

    void send(UdpMessage message);

    void sendUdp(UdpMessage message);
}
