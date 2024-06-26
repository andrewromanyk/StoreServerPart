package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;
import static ua.edu.ukma.Key.KEY;
//import static java.lang.Math.pow;

public class Packet {
    private static long pktidLast = 0;

    private byte bMagic;
    private byte bSrc;
    private long bPktId;
    private int wLen;
    private short wCrc16;
    private Message bMsq;
    private short wCrc16Mes;

    public Packet(byte src, int command, byte[] message, int userid) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        bMagic = 0x13;
        bSrc = src;
        bPktId = pktidLast++;
        byte[] encrypted_message = Encryption.encrypt(message, KEY);
        wLen = encrypted_message.length+8;
        ByteBuffer buf = ByteBuffer.allocate(14);
        buf.put(bMagic);
        buf.put(bSrc);
        buf.putLong(bPktId);
        buf.putInt(wLen);
        wCrc16 = CRC16.CRC(buf.array());
        bMsq = new Message(command, userid, message);
        ByteBuffer buf2 = ByteBuffer.allocate(wLen);
        buf2.putInt(command);
        buf2.putInt(userid);
        buf2.put(encrypted_message);
        wCrc16Mes = CRC16.CRC(buf2.array());
    }

    public Packet(byte[] packet, Key KEY) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        bMagic = buffer.get();
        if (bMagic != 0x13)
            throw new Exception("Invalid magic number.");

        bSrc = buffer.get();
        bPktId = buffer.getLong();
        wLen = buffer.getInt();
        wCrc16 = buffer.getShort();
        if (wCrc16 != CRC16.CRC(Arrays.copyOfRange(packet, 0, 14)))
            throw new Exception("CRC of the header doesn't match.");

        bMsq = new Message(buffer.getInt(), buffer.getInt(), Encryption.decrypt(Arrays.copyOfRange(packet, 24, 16+wLen), KEY));

        buffer.position(16+wLen);

        wCrc16Mes = buffer.getShort();
        if (wCrc16Mes != CRC16.CRC(Arrays.copyOfRange(packet, 16, 16+wLen)))
            throw new Exception("CRC of the message doesn't match.");
    }

    public byte src() { return bSrc; }
    public long pktId() { return bPktId; }
    public int len() { return wLen; }
    public short crc16() { return wCrc16; }
    public Message message() { return bMsq; }
    public short crc16Message() { return wCrc16Mes; }

    public void setwLen(int wLen) {
        this.wLen = wLen;
    }


    public byte[] toByteArray() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        ByteBuffer buffer = ByteBuffer.allocate(18+wLen);
        buffer.put(bMagic);
        buffer.put(bSrc);
        buffer.putLong(bPktId);
        buffer.putInt(wLen);
        buffer.putShort(wCrc16);
//        System.out.println(Arrays.toString(message().toByteArray()));
//        System.out.println("actual message size:" + message().toByteArray().length);
//        System.out.println("overall packet size:" + (message().toByteArray().length + 18));
//        System.out.println("buffer length:"+buffer.array().length);
//        System.out.println("wLen:" + wLen);
        buffer.put(message().toByteArray());
        buffer.putShort(wCrc16Mes);
        return buffer.array();
    }
/*    public static long fromBigEndian(byte[] array){
        long result = 0;
        int length = array.length;
        for (int i = 0; i < length; i++){
            long toadd = array[i] & 0xFF;
            if (array[i] < 0) toadd -= 1;
            result += (long) (toadd * pow(256, length - i - 1));
        }
        return result;
    }*/
    public static void main(String[] args) throws Exception {
        byte[] byte_key =
                {0, -15, -9, 127,
                        65, -77, 1, 123,
                        -36, 12, -32, 1,
                        44, -15, 15, 99};
        Key KEY = new SecretKeySpec(byte_key, "AES");

        byte[] pkt = {
                0x13,
                28,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 24,
                -123, 61,
                0, 0, 0, 99,
                0, 0, 1, (byte) 179,
                -33, 83, -74, -18, -92, -30, 39, 86, -25, -57, 126, -95, -13, 68, 41, 15,
                -76, 113
        };
        Packet packet = new Packet(pkt, KEY);
        System.out.println(packet);
        packet.message().setbyteMessage(new byte[]{1, 1, 1});
        System.out.println(packet);
    }
}
