package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.util.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Ported from mumble's CryptState.h (and corresponding *.cpp(s))
 * <p>
 * Stores encryption/decryption state and facilitates the UDP channel OCB-AES128 crypto
 */
class CryptState {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptState.class.getSimpleName());

    private static final int AES_BLOCK_SIZE = 16;
    private static final int AES_KEY_SIZE_BITS = 128;
    private static final int AES_KEY_SIZE_BYTES = AES_KEY_SIZE_BITS / 8;

    private final byte[] key = new byte[AES_KEY_SIZE_BYTES];
    private final byte[] encryptIv = new byte[AES_BLOCK_SIZE];
    private final byte[] decryptIv = new byte[AES_BLOCK_SIZE];
    private final byte[] decryptHistory = new byte[256];

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    // TODO: wrap these in some POJO-object
    private int uiGood, uiLate, uiLost, uiResync;
    private int uiRemoteGood, uiRemoteLate, uiRemoteLost, uiRemoteResync;

    private boolean isValid;

    byte[] getEncryptIv() {
        return encryptIv;
    }

    boolean isValid() {
        return isValid;
    }

    void setKey(@Nullable byte[] key, @Nullable byte[] encryptIv, byte[] decryptIv) {
        System.arraycopy(decryptIv, 0, this.decryptIv, 0, AES_BLOCK_SIZE);
        if (encryptIv != null) System.arraycopy(encryptIv, 0, this.encryptIv, 0, AES_BLOCK_SIZE);

        if (key != null) {
            System.arraycopy(key, 0, this.key, 0, AES_KEY_SIZE_BYTES);
            try {
                encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
                decryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                LOGGER.error(Markers.CRYPTO, "Could not get cipher instance: {}", e.toString());
                return;
            }

            SecretKeySpec keySpec = new SecretKeySpec(this.key, "AES");

            try {
                encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);
                decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
            } catch (InvalidKeyException e) {
                LOGGER.error(Markers.CRYPTO, "Cipher init failed due to an invalid key: {}", e.toString());
                return;
            }

            isValid = true;
        }
    }

    void encrypt(final byte[] source, byte[] dst, int plainLength) {
        byte[] tag = new byte[AES_BLOCK_SIZE];

        for (int i = 0; i < AES_BLOCK_SIZE; i++) {
            if ((++encryptIv[i]) != 0)
                break;
        }

        try {
            ocbEncrypt(source, dst, plainLength, encryptIv, tag);
        } catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException e) {
            LOGGER.error(Markers.CRYPTO, "Encryption failed due to exception in cipher: {}", e.toString());
            isValid = false;
            return;
        }

        // TODO: Make ocbEncrypt(...) automagically offset everything by 4
        // Workaround: Manually shift dst by 4
        System.arraycopy(dst, 0, dst, 4, plainLength);

        dst[0] = encryptIv[0];
        dst[1] = tag[0];
        dst[2] = tag[1];
        dst[3] = tag[2];
    }

    boolean decrypt(final byte[] source, byte[] dst, int cryptedLength) {
        if (cryptedLength < 4)
            return false;

        int plainLength = cryptedLength - 4;

        byte[] saveiv = new byte[AES_BLOCK_SIZE];
        short ivbyte = (short) (source[0] & 0xFF);
        boolean restore = false;
        byte[] tag = new byte[AES_BLOCK_SIZE];

        int lost = 0;
        int late = 0;


        System.arraycopy(decryptIv, 0, saveiv, 0, AES_BLOCK_SIZE);

        if (((decryptIv[0] + 1) & 0xFF) == ivbyte) {
            // Packets in order (ivbyte greater than last time)

            // Counting normally
            if (ivbyte > (decryptIv[0] & 0xFF)) {
                decryptIv[0] = (byte) ivbyte;
            }
            // Wrapping around
            else if (ivbyte < (decryptIv[0] & 0xFF)) {
                decryptIv[0] = (byte) ivbyte;
                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if ((++decryptIv[i]) != 0)
                        break;
                }
            } else {
                // NOTE: reaching this is impossible?
                //      (if "iv[0] + 1 == ivbyte", then either "iv[0] < ivbyte" or "iv[0] > ivbyte" are always true)
                return false;
            }
        } else {
            // Out of order or receiving packet already received

            int diff = ivbyte - (decryptIv[0] & 0xFF);
            if (diff > 128) {
                diff -= 256;
            } else if (diff < -128) {
                diff += 256;
            }

            // Late packet, no wrap
            if ((ivbyte < (decryptIv[0] & 0xFF)) && (diff > -30) && (diff < 0)) {
                late = 1;
                lost = -1;
                decryptIv[0] = (byte) ivbyte;
                restore = true;
            }
            // Last was 0x02+, this is 0xFF- from before the wrap
            else if ((ivbyte > (decryptIv[0] & 0xFF)) && (diff > -30) && (diff < 0)) {
                late = 1;
                lost = -1;
                decryptIv[0] = (byte) ivbyte;
                restore = true;

                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if ((decryptIv[i]--) != 0)
                        break;
                }
            }
            // Lost a few packets, but beyond that we're good
            else if ((ivbyte > (decryptIv[0] & 0xFF)) && (diff > 0)) {
                lost = ivbyte - decryptIv[0] - 1;
                decryptIv[0] = (byte) ivbyte;
            }
            // Lost a few packets and wrapped around
            else if ((ivbyte < (decryptIv[0] & 0xFF)) && (diff > 0)) {
                lost = 256 - (decryptIv[0] & 0xFF) + ivbyte - 1;
                decryptIv[0] = (byte) ivbyte;
                for (int i = 1; i < AES_BLOCK_SIZE; i++) {
                    if ((++decryptIv[i]) != 0)
                        break;
                }
            } else {
                return false;
            }

            if (decryptHistory[decryptIv[0] & 0xFF] == decryptIv[1]) {
                System.arraycopy(saveiv, 0, decryptIv, 0, AES_BLOCK_SIZE);
                return false;
            }
        }

        byte[] sourceWithoutTag = new byte[cryptedLength - 4];
        System.arraycopy(source, 4, sourceWithoutTag, 0, cryptedLength - 4);


        try {
            ocbDecrypt(sourceWithoutTag, dst, plainLength, decryptIv, tag);
        } catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException e) {
            LOGGER.error(Markers.CRYPTO, "Decryption failed due to exception in cipher: {}", e.toString());
            isValid = false;
            return false;
        }

        // original: memcmp(tag, source+1, 3) != 0
        for (int i = 0; i < 3; i++) {
            if (tag[i] != source[i + 1]) {
                System.arraycopy(saveiv, 0, decryptIv, 0, AES_BLOCK_SIZE);
                return false;
            }
        }
        decryptHistory[decryptIv[0] & 0xFF] = decryptIv[1];

        if (restore) {
            System.arraycopy(saveiv, 0, decryptIv, 0, AES_BLOCK_SIZE);
        }

        uiGood++;
        uiLate += late;
        uiLost += lost;

        // lastGood.restart();
        return true;
    }

    private static final int SHIFTBITS = 7;

    private static void xor(byte[] dst, byte[] a, byte[] b) {
        for (int i = 0; i < AES_BLOCK_SIZE; i++) {
            dst[i] = (byte) (a[i] ^ b[i]);
        }
    }

    private static void s2(byte[] block) {
        int carry = (block[0] >> SHIFTBITS) & 0x1;
        for (int i = 0; i < AES_BLOCK_SIZE - 1; i++) {
            block[i] = (byte) ((block[i] << 1) | ((block[i + 1] >> SHIFTBITS) & 0x1));
        }
        block[AES_BLOCK_SIZE - 1] = (byte) ((block[AES_BLOCK_SIZE - 1] << 1) ^ (carry * 0x87));
    }

    private static void s3(byte[] block) {
        int carry = (block[0] >> SHIFTBITS) & 0x1;
        for (int i = 0; i < AES_BLOCK_SIZE - 1; i++) {
            block[i] ^= (block[i] << 1) | ((block[i + 1] >> SHIFTBITS) & 0x1);
        }
        block[AES_BLOCK_SIZE - 1] ^= ((block[AES_BLOCK_SIZE - 1] << 1) ^ (carry * 0x87));
    }

    private static void zero(byte[] block) {
        Arrays.fill(block, (byte) 0);
    }


    private void ocbEncrypt(final byte[] plain, byte[] encrypted, int len, byte[] nonce, byte[] tag) throws BadPaddingException, IllegalBlockSizeException, ShortBufferException {
        byte[] checksum = new byte[AES_BLOCK_SIZE];
        byte[] tmp = new byte[AES_BLOCK_SIZE];

        byte[] delta = encryptCipher.doFinal(nonce);
        zero(checksum);

        int offset = 0;
        while (len > AES_BLOCK_SIZE) {
            byte[] buffer = new byte[AES_BLOCK_SIZE];
            s2(delta);
            System.arraycopy(plain, offset, buffer, 0, AES_BLOCK_SIZE);
            xor(checksum, checksum, buffer);
            xor(tmp, delta, buffer);

            encryptCipher.doFinal(tmp, 0, AES_BLOCK_SIZE, tmp);

            xor(buffer, delta, tmp);
            System.arraycopy(buffer, 0, encrypted, offset, AES_BLOCK_SIZE);
            len -= AES_BLOCK_SIZE;
            offset += AES_BLOCK_SIZE;
        }

        s2(delta);
        zero(tmp);
        long num = len * 8;
        tmp[AES_BLOCK_SIZE - 2] = (byte) ((num >> 8) & 0xFF);
        tmp[AES_BLOCK_SIZE - 1] = (byte) (num & 0xFF);
        xor(tmp, tmp, delta);

        final byte[] pad = encryptCipher.doFinal(tmp);

        System.arraycopy(plain, offset, tmp, 0, len);
        System.arraycopy(pad, len, tmp, len, AES_BLOCK_SIZE - len);
        xor(checksum, checksum, tmp);
        xor(tmp, pad, tmp);

        System.arraycopy(tmp, 0, encrypted, offset, len);
        s3(delta);
        xor(tmp, delta, checksum);
        encryptCipher.doFinal(tmp, 0, AES_BLOCK_SIZE, tag);
    }

    private void ocbDecrypt(byte[] encrypted, byte[] plain, int len, byte[] nonce, byte[] tag) throws BadPaddingException, IllegalBlockSizeException, ShortBufferException {
        byte[] checksum = new byte[AES_BLOCK_SIZE];
        byte[] tmp = new byte[AES_BLOCK_SIZE];

        byte[] delta = encryptCipher.doFinal(nonce);

        int offset = 0;
        while (len > AES_BLOCK_SIZE) {
            byte[] buffer = new byte[AES_BLOCK_SIZE];
            s2(delta);
            System.arraycopy(encrypted, offset, buffer, 0, AES_BLOCK_SIZE);

            xor(tmp, delta, buffer);
            decryptCipher.doFinal(tmp, 0, AES_BLOCK_SIZE, tmp);

            xor(buffer, delta, tmp);
            System.arraycopy(buffer, 0, plain, offset, AES_BLOCK_SIZE);

            xor(checksum, checksum, buffer);
            len -= AES_BLOCK_SIZE;
            offset += AES_BLOCK_SIZE;
        }

        s2(delta);
        zero(tmp);

        long num = len * 8;
        tmp[AES_BLOCK_SIZE - 2] = (byte) ((num >> 8) & 0xFF);
        tmp[AES_BLOCK_SIZE - 1] = (byte) (num & 0xFF);
        xor(tmp, tmp, delta);

        byte[] pad = encryptCipher.doFinal(tmp);
        zero(tmp);
        System.arraycopy(encrypted, offset, tmp, 0, len);

        xor(tmp, tmp, pad);
        xor(checksum, checksum, tmp);

        System.arraycopy(tmp, 0, plain, offset, len);

        s3(delta);
        xor(tmp, delta, checksum);

        encryptCipher.doFinal(tmp, 0, AES_BLOCK_SIZE, tag);
    }
}
