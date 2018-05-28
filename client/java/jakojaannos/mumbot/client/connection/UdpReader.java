package jakojaannos.mumbot.client.connection;

import com.google.common.primitives.UnsignedBytes;
import jakojaannos.mumbot.client.util.crypto.OcbPmac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
    private final byte[] history = new byte[256];

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

    private boolean decrypt(byte[] source, int dataLen, byte[] dest) {
        if (source.length < 4) {
            System.out.println("No enough data to decrypt");
            return false;
        }

        int plainLength = dataLen - 4;

        byte[] saveiv = new byte[AES_BLOCK_SIZE];
        byte[] tag = new byte[AES_BLOCK_SIZE];

        byte ivbyte = source[0];
        boolean restore = false;

        int lost = 0;
        int late = 0;

        System.arraycopy(nonce, 0, saveiv, 0, AES_BLOCK_SIZE);

        if (nonce[0] + 1 == ivbyte) {
            // In order as expected.
            if (UnsignedBytes.compare(ivbyte, nonce[0]) > 0) {// (ivbyte > nonce[0]) {
                nonce[0] = ivbyte;
                System.out.println("this?");
            } else if (UnsignedBytes.compare(ivbyte, nonce[0]) < 0) {//(ivbyte < nonce[0]) {
                nonce[0] = ivbyte;
                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if (++nonce[i] != 0)
                        break;
                }
                System.out.println("or this?");
            } else {
                return false;
            }
        } else {
            System.out.println("EVERYTHING IS LOST ==================================================================");
            int diff = UnsignedBytes.toInt(ivbyte) - UnsignedBytes.toInt(nonce[0]);
            if (diff > 128) {
                diff = diff - 256;
            } else if (diff < -128) {
                diff = diff + 256;
            }

            if ((UnsignedBytes.compare(ivbyte, nonce[0]) < 0) && (diff > -30) && (diff < 0)) {
                late = 1;
                lost = -1;
                nonce[0] = ivbyte;
                restore = true;
            } else if ((UnsignedBytes.compare(ivbyte, nonce[0]) > 0) && (diff > -30) && (diff < 0)) {
                late = 1;
                lost = -1;

                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if (nonce[i]-- != 0) {
                        break;
                    }
                }

                restore = true;
            } else if ((UnsignedBytes.compare(ivbyte, nonce[0]) > 0) && (diff > 0)) {
                lost = UnsignedBytes.toInt(ivbyte) - UnsignedBytes.toInt(nonce[0]) - 1;
                nonce[0] = ivbyte;
            } else if ((UnsignedBytes.compare(ivbyte, nonce[0]) < 0) && (diff > 0)) {
                lost = 256 - UnsignedBytes.toInt(nonce[0]) + UnsignedBytes.toInt(ivbyte) - 1;
                nonce[0] = ivbyte;
                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if (++nonce[i] != 0)
                        break;
                }
            } else {
                return false;
            }

            if (history[UnsignedBytes.toInt(nonce[0])] == nonce[1]) {
                System.arraycopy(saveiv, 0, nonce, 0, AES_BLOCK_SIZE);
                return false;
            }
        }

        if (plainLength == 0) {
            return true;
        }

        byte[] plain = cipher.decrypt(source, 4, plainLength, this.nonce, 0, source, 1);
        if (plain == null) {
            System.out.println("Message validation failed");
            System.arraycopy(saveiv, 0, nonce, 0, AES_BLOCK_SIZE);
            return false;
        }

        if (plain.length != plainLength)
            throw new IllegalStateException("Plaintext length mismatch");

        System.arraycopy(plain, 0, dest, 0, plainLength);

        history[nonce[0]] = nonce[1];

        if (restore) {
            System.arraycopy(saveiv, 0, nonce, 0, AES_BLOCK_SIZE);
        }

        //uiGood++;
        //uiLate += late;
        //uiLost += lost;
        //tLastGood.restart();

        return true;
    }
}
