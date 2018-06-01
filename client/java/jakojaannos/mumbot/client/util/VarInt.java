package jakojaannos.mumbot.client.util;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Variable length encoded 64-bit integer
 */
public final class VarInt {
    private final long value;
    private final byte[] data;

    public long getValue() {
        return value;
    }

    public byte[] toBytes() {
        return data;
    }

    private VarInt(byte[] data, long value) {
        this.data = data;
        this.value = value;
    }

    public static VarInt decode(ByteBuffer buffer) {
        int originalPosition = buffer.position();
        long i = 0;
        byte b = buffer.get();

        // 7-bit positive number
        if ((b & 0x80) == 0x00) {
            i = b & 0x7F;
        }
        // 14-bit positive number
        else if ((b & 0xC0) == 0x80) {
            i = (b & 0x3F) << 8 | buffer.get();
        } else if ((b & 0xF0) == 0xF0) {
            switch (b & 0xFC) {
                // 32-bit positive number
                case 0xF0:
                    i = buffer.get() << 24 | buffer.get() << 16 | buffer.get() << 8 | buffer.get();
                    break;
                // 64-bit positive number
                case 0xF4:
                    i = (long) buffer.get() << 56 | (long) buffer.get() << 48 | (long) buffer.get() << 40 | (long) buffer.get() << 32 | buffer.get() << 24 | buffer.get() << 16 | buffer.get() << 8 | buffer.get();
                    break;
                // Negative recursive VarInt
                case 0xF8:
                    i = decode(buffer).value;
                    i = ~i;
                    break;
                // Byte-inverted negative two-bit number
                case 0xFC:
                    i = b & 0x03;
                    i = ~i;
                    break;
                default:
                    throw new BufferUnderflowException();
            }
        }
        // 28-bit positive number
        else if ((b & 0xF0) == 0xE0) {
            i = (b & 0x0F) << 24 | buffer.get() << 16 | buffer.get() << 8 | buffer.get();
        }
        // 21-bit positive number
        else if ((b & 0xE0) == 0xC0) {
            i = (b & 0x1F) << 16 | buffer.get() << 8 | buffer.get();
        }

        int length = buffer.position() - originalPosition;
        byte[] data = new byte[length];
        System.arraycopy(buffer.array(), originalPosition, data, 0, length);
        return new VarInt(data, i);
    }

    public static VarInt encode(final long value) {
        long i = value;
        ByteBuffer buffer = ByteBuffer.allocate(8 + 2); // 8 byte long + 2 bytes for header(s)

        // Negative recursive VarInt or byte-inverted negative two-bit number
        if ((Long.compareUnsigned(value & 0x8000000000000000L, 0L) > 0) && Long.compareUnsigned(~value, 0x100000000L) < 0) {
            i = ~i;

            // -1 to -4
            if (i <= 0x3) {
                buffer.put((byte) (0xFC | i));

                buffer.flip();
                byte[] result = new byte[buffer.limit()];
                System.arraycopy(buffer.array(), 0, result, 0, result.length);
                return new VarInt(result, value);
            }
            // Negative recursive VarInt
            else {
                buffer.put((byte) 0xF8);
            }
        }

        // 7-bit positive number
        if (i < 0x80L) {
            buffer.put((byte) (i & 0x7F));
        }
        // 14-bit positive number
        else if (i < 0x4000L) {
            buffer.put((byte) ((i >> 8) & 0x80));
            buffer.put((byte) (i & 0xFF));
        }
        // 21-bit positive number
        else if (i < 0x200000L) {
            buffer.put((byte) ((i >> 16) | 0xC0));
            buffer.put((byte) ((i >> 8) & 0xFF));
            buffer.put((byte) (i & 0xFF));
        }
        // 28-bit positive number
        else if (i < 0x10000000L) {
            buffer.put((byte) ((i >> 24) | 0xE0));
            buffer.put((byte) ((i >> 16) & 0xFF));
            buffer.put((byte) ((i >> 8) & 0xFF));
            buffer.put((byte) (i & 0xFF));
        }
        // 32-bit positive number
        else if (i < 0x100000000L) {
            buffer.put((byte) 0xF0);
            buffer.putInt((int) i);
            //buffer.put((byte) ((i >> 24) & 0xFF));
            //buffer.put((byte) ((i >> 16) & 0xFF));
            //buffer.put((byte) ((i >> 16) & 0xFF));
            //buffer.put((byte) ((i >> 8) & 0xFF));
            //buffer.put((byte) (i & 0xFF));
        }
        // 64-bit number
        else {
            buffer.put((byte) 0xF4);
            buffer.putLong(i);
            //buffer.put((byte) ((i >> 56) & 0xFF));
            //buffer.put((byte) ((i >> 48) & 0xFF));
            //buffer.put((byte) ((i >> 40) & 0xFF));
            //buffer.put((byte) ((i >> 32) & 0xFF));
            //buffer.put((byte) ((i >> 24) & 0xFF));
            //buffer.put((byte) ((i >> 16) & 0xFF));
            //buffer.put((byte) ((i >> 16) & 0xFF));
            //buffer.put((byte) ((i >> 8) & 0xFF));
            //buffer.put((byte) (i & 0xFF));
        }

        buffer.flip();
        byte[] result = new byte[buffer.limit()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return new VarInt(result, value);
    }
}
