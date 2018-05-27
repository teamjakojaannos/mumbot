package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.crypto.OcbPmac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Runnable task for reading voice packets over UDP. Reads and decrypts packets and adds them to FIFO queue which can
 * be queried thread-safe.
 * <p>
 * TODO: Move crypto somewhere else. Implement something like CryptState in the original mumble client to keep things clean
 */
public class UdpReader extends SocketReaderBase<UdpMessage> {
    private static final int AES_BLOCK_SIZE = 16;
    private final DatagramSocket socket;

    private final byte[] buffer = new byte[1024];

    private final OcbPmac cipher;
    private byte[] nonce;

    UdpReader(DatagramSocket socket, Connection connection) {
        super(connection);

        this.socket = socket;

        this.cipher = new OcbPmac();
    }

    void initCipher(byte[] key, byte[] nonce) {
        this.nonce = nonce;
        cipher.init(key, 128);
    }

    @Override
    public void run() {
        System.out.println("UdpReader entering loop");
        super.run();
        System.out.println("UdpReader leaving loop");
    }

    @Override
    UdpMessage read() {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        System.out.println("Receiving UDP datagram");
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
            terminate();
        }

        System.out.println("Decrypting...");
        byte[] data = new byte[packet.getLength() - 4];
        if (!decrypt(buffer, packet.getLength(), data)) {
            // TODO: Record time of good/bad packets and request crypt resync when decrypting packets has failed for long enough
            System.out.println("Decrypting failed.");
            return null;
        }

        System.out.println("Decryption successful!");

        return new UdpMessage(data);
    }

    public UdpMessage read(DatagramPacket packet) {
        System.out.println("Receiving UDP datagram");

        System.out.println("Decrypting...");
        byte[] data = new byte[packet.getLength() - 4];
        if (!decrypt(packet.getData(), packet.getLength(), data)) {
            // TODO: Record time of good/bad packets and request crypt resync when decrypting packets has failed for long enough
            System.out.println("Decrypting failed.");
            return null;
        }

        System.out.println("Decryption successful!");

        return new UdpMessage(data);
    }

    private boolean decrypt(byte[] source, int dataLen, byte[] dest) {
        if (source.length < 4)
            return false;

        int plainLength = dataLen - 4;

        byte ivbyte = source[0];

        if (true || ((nonce[0] + 1) & 0xFF) == ivbyte) {
            // In order as expected.
            if (ivbyte > nonce[0]) {
                nonce[0] = ivbyte;
            } else if (ivbyte < nonce[0]) {
                nonce[0] = ivbyte;
                for (int i = 1; i < AES_BLOCK_SIZE; i++)
                    if (++nonce[i] != 0)
                        break;
            } else {
                return false;
            }
        } else {
            // TODO: do magic tricks with associated data (fix nonce etc)
        }

        if (plainLength == 0) {
            return true;
        }

        byte[] plain = cipher.decrypt(source, 4, plainLength, this.nonce, 0, source, 1);
        if (plain == null)
            System.err.println("Message validation failed");

        if (plain.length != plainLength)
            throw new IllegalStateException("Plaintext length mismatch");

        System.arraycopy(plain, 0, dest, 0, plainLength);

        return true;
    }
}
