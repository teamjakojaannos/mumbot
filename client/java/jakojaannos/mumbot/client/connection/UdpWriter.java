package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.crypto.OcbPmac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UdpWriter extends SocketWriterBase<UdpMessage> {
    private static final int AES_BLOCK_SIZE = 16;

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;

    private final OcbPmac cipher;
    private byte[] nonce;


    UdpWriter(DatagramSocket socket, InetAddress address, int port, Connection connection) {
        super(connection);

        this.socket = socket;
        this.address = address;
        this.port = port;

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
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < AES_BLOCK_SIZE; i++)
            if (++nonce[i] != 0)
                break;

        byte[] plain = message.getData();

        // TODO: Recycle buffers
        byte[] buf = new byte[plain.length + 4];

        byte[] ciphertext = cipher.encrypt(plain, 0, plain.length, nonce, 0, buf, 1);
        buf[0] = nonce[0];
        System.arraycopy(ciphertext, 0, buf, 4, ciphertext.length);

        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, address, port);

        try {
            System.out.println("Plain:");
            for (byte b : plain)
                System.out.printf("%d ", b);

            System.out.println("\n\nCiphertext:");
            for (byte b : ciphertext)
                System.out.printf("%d ", b);

            System.out.println("\n\nPacket:");
            for (byte b : buf)
                System.out.printf("%d ", b);
            System.out.println();


            System.out.println("Sending UDP packet with length=" + packet.getLength());
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error writing UDP message:");
            e.printStackTrace();
        }
    }
}
