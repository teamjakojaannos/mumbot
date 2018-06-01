package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.IAudioFrame;
import jakojaannos.mumbot.client.util.VarInt;

import java.nio.ByteBuffer;

public class UdpAudioPacket implements IAudioFrame {
    private byte header;
    private VarInt sessionId;
    private VarInt sequenceNumber;
    private Payload payload;
    private float[] positionInfo;

    private boolean endOfTransmission;

    public UdpAudioPacket() {
        header = (byte) 0x80;
        sessionId = null;
        sequenceNumber = null;
        payload = new Payload(0, new byte[0]);
        positionInfo = new float[3];
    }

    @Override
    public int getSize() {
        return (int) payload.header.getValue();
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber.getValue();
    }

    @Override
    public boolean isEndOfTransmission() {
        return endOfTransmission;
    }

    @Override
    public byte[] getData() {
        return payload.data;
    }


    public UdpAudioPacket(byte header, VarInt sessionId, VarInt sequenceNumber, Payload payload, float[] positionInfo, boolean endOfTransmission) {
        if (positionInfo.length != 3) {
            throw new IllegalArgumentException("Position info length must be exactly 3");
        }

        this.header = header;
        this.sessionId = sessionId;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.positionInfo = positionInfo;
        this.endOfTransmission = endOfTransmission;
    }

    public void serialize(ByteBuffer buffer) {
        buffer.put(header);
        // We do not serialize session id as outgoing packets do not need it
        //buffer.put(sessionId.toBytes());
        buffer.put(sequenceNumber.toBytes());

        byte[] payloadHeader = payload.header.toBytes();
        if (endOfTransmission) payloadHeader[0] |= 0x20;

        buffer.putFloat(positionInfo[0]);
        buffer.putFloat(positionInfo[1]);
        buffer.putFloat(positionInfo[2]);
    }

    public void deserialize(ByteBuffer buffer) {
        header = buffer.get();
        sessionId = VarInt.decode(buffer);
        sequenceNumber = VarInt.decode(buffer);

        VarInt payloadHeader = VarInt.decode(buffer);
        endOfTransmission = (payloadHeader.toBytes()[0] & 0x20) != 0;

        positionInfo[0] = buffer.getFloat();
        positionInfo[1] = buffer.getFloat();
        positionInfo[2] = buffer.getFloat();
    }

    /**
     * For packaging Opus audio frames
     */
    public static class Payload {
        private VarInt header;
        private byte[] data;

        public Payload(VarInt header, byte[] data) {
            this.header = header;
            this.data = data;
        }

        public Payload(int length, byte[] data) {
            this(VarInt.encode(verify(length)), data);
        }

        private static long verify(long length) {
            if (length > 0x1FFF) {
                throw new IllegalArgumentException("Audio packet Opus frame cannot be longer than 8191 bytes!");
            }
            return length;
        }
    }
}
