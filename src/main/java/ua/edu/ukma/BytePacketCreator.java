package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.util.Arrays;

public class BytePacketCreator {

    private static long PackageId = 0;

    public static byte[] packetToBytePacket(Packet pkt, Key key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ByteBuffer buffer = ByteBuffer.allocate(18 + pkt.len());
        buffer.put((byte) 0x13)
                .put(pkt.src())
                .putLong(pkt.pktId())
                .putInt(pkt.len())
                .putShort(pkt.crc16())
                .putInt(pkt.message().type())
                .putInt(pkt.message().userId())
                .put(Encryption.encrypt(pkt.message().getByteMessage(), key))
                .putShort(pkt.crc16Message());
        return buffer.array();

    }

/*    public static byte[] toBigEndian(long num, int length){
        byte[] endian = new byte[length];
        int i = length-1;
        while (i >= 0 && num != 0) {
            endian[i] = (byte) (num % 256);
            num = (long) (num / 256.0);
            --i;
        }
        return endian;
    }*/

    public static byte[] messageToBytePacket(byte[] message, byte clientId, int command, int userId, Key key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] encryptedMessage = Encryption.encrypt(message, key);
        ByteBuffer buffer = ByteBuffer.allocate(26+encryptedMessage.length);
        buffer.put((byte) 0x13)
                .put(clientId)
                .putLong(PackageId++)
                .putInt(8+encryptedMessage.length);

        byte[] header = Arrays.copyOfRange(buffer.duplicate().rewind().array(), 0, 14);
        buffer.putShort(CRC16.CRC(header))
                .putInt(command)
                .putInt(userId)
                .put(encryptedMessage);

        byte[] byteMessage = Arrays.copyOfRange(buffer.duplicate().rewind().array(), 16, 24+encryptedMessage.length);
        buffer.putShort(CRC16.CRC(byteMessage));

        return buffer.array();
    }
}
