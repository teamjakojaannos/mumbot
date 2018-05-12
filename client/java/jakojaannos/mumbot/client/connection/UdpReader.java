package jakojaannos.mumbot.client.connection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Runnable task for reading voice packets over UDP. Reads and decrypts packets and adds them to FIFO queue which can
 * be queried thread-safe.
 *
 * TODO: Move crypto somewhere else. Implement something like CryptState in the original mumble client to keep things clean
 */
public class UdpReader implements Runnable {
    private final DatagramSocket socket;
    private final Supplier<Boolean> running;

    private final Deque<UdpMessage> queue;
    private final AtomicBoolean hasPackets;

    private final Cipher cipher;
    private final byte[] buffer = new byte[1024];

    /**
     * Performs thread-safe check if the reader has queued packets ready for handling.
     *
     * @return true if there are packets in the queue
     */
    public boolean hasPackets() {
        return hasPackets.get();
    }

    /**
     * Pops the first element from the queue. Method is thread-safe.
     *
     * @return topmost element of the queue.
     */
    public UdpMessage dequeue() {
        UdpMessage msg;
        synchronized (queue) {
            msg = queue.pop();
        }

        return msg;
    }

    UdpReader(DatagramSocket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;

        this.queue = new ArrayDeque<>();
        this.hasPackets = new AtomicBoolean(false);

        this.cipher = createCipher();
        // TODO: Handle null-cipher

    }

    private static Cipher createCipher() {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/OCB/NoPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException ignored) {
            cipher = null;
        }

        return cipher;
    }

    void initCipher(byte[] key, byte[] nonce) {
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(nonce));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ignored) {
            System.out.println("Initializing cipher failed!");
        }
    }

    @Override
    public void run() {
        while (running.get() && !doRead()) ;
    }

    private boolean doRead() {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = new byte[packet.getLength() - 4];
        decrypt(buffer, packet.getLength(), data);

        byte header = data[0];

        byte[] payload = new byte[data.length - 1];
        System.arraycopy(data, 1, payload, 0, payload.length);

        synchronized (queue) {
            queue.addLast(new UdpMessage(header, payload));
        }

        return false;
    }

    private boolean decrypt(byte[] source, int dataLen, byte[] dest) {
        if (source.length < 4)
            return false;

        int plainLength = dataLen - 4;

        byte[] plain;
        try {
            // TODO: do magic tricks with associated data
            plain = cipher.doFinal(source, 4, plainLength);
        } catch (IllegalBlockSizeException | BadPaddingException ignored) {
            return false;
        }

        System.arraycopy(plain, 0, dest, 0, plainLength);
        return true;
    }
}
