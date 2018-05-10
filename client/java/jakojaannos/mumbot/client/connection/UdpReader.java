package jakojaannos.mumbot.client.connection;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.OCBBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.Cipher;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class UdpReader implements Runnable {
    private final DatagramSocket socket;
    private final Supplier<Boolean> running;

    private final AtomicBoolean hasPackets;

    private final Cipher cipher;

    UdpReader(DatagramSocket socket, Supplier<Boolean> running) {
        this.socket = socket;
        this.running = running;

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
        DatagramPacket packet = new DatagramPacket(bufferArr, bufferArr.length);

        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = new byte[packet.getLength() - 4];
        decrypt(bufferArr, packet.getLength(), data);


        return false;
    }

    private boolean decrypt(byte[] source, int dataLen, byte[] dest) {
        if (source.length < 4)
            return false;

        int plainLength = source.length - 4;

        byte[] saveiv = new byte[AES_BLOCK_SIZE];
        byte ivbyte = source[0];

        boolean restore = false;

        System.arraycopy(saveiv, 0, decrypt_iv);
    }

    public boolean hasPackets() {
        return hasPackets.get();
    }
}
