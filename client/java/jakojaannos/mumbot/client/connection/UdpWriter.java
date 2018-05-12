package jakojaannos.mumbot.client.connection;

import java.net.DatagramSocket;
import java.util.function.Supplier;

public class UdpWriter implements Runnable {
    private final DatagramSocket socket;
    private final Supplier<Boolean> running;


    public UdpWriter(DatagramSocket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;
    }

    @Override
    public void run() {

    }

    void initCipher(byte[] key, byte[] clientNonce) {

    }
}
