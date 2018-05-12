package jakojaannos.mumbot.client.connection;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class UdpWriter implements Runnable {
    private final DatagramSocket socket;
    private final Supplier<Boolean> running;


    public UdpWriter(DatagramSocket socket, InetSocketAddress inetSocketAddress, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;
    }

    @Override
    public void run() {

    }

    void initCipher(byte[] key, byte[] clientNonce) {

    }
}
