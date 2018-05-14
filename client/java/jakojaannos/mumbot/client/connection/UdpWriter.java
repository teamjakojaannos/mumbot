package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.crypto.OcbPmac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UdpWriter extends SocketWriterBase<UdpMessage> {
    private static final int AES_BLOCK_SIZE = 16;

    private final DatagramSocket socket;
    private final InetSocketAddress address;

    private final OcbPmac cipher;
    private byte[] nonce;


    UdpWriter(DatagramSocket socket, InetSocketAddress inetSocketAddress, Connection connection) {
        super(connection);

        this.socket = socket;
        this.address = inetSocketAddress;

        this.cipher = new OcbPmac();
    }

    void initCipher(byte[] key, byte[] nonce) {
        this.nonce = nonce;
        cipher.init(key, 128);
    }

    @Override
    public void run() {
        System.out.println("UdpWriter entering loop");
        super.run();
        System.out.println("UdpWriter leaving loop");
    }

    @Override
    void write(UdpMessage message) {
        for (int i = 0; i < AES_BLOCK_SIZE; i++)
            if (++nonce[i] != 0)
                break;

        byte[] plain = message.getData();

        // TODO: Recycle buffers
        byte[] buf = new byte[plain.length + 4];
        buf[0] = nonce[0];

        byte[] ciphertext = cipher.encrypt(plain, 0, plain.length, nonce, 0, buf, 1);
        System.arraycopy(ciphertext, 0, buf, 4, ciphertext.length);

        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, address);

        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error writing UDP message:");
            e.printStackTrace();
        }
    }
}
