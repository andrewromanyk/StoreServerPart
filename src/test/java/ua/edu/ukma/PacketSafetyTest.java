package ua.edu.ukma;

import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;

public class PacketSafetyTest {

    private void testTemplate(byte[] message, byte clientid, int command, int userId, Key KEY) {
        try {
            byte[] bytePkt = BytePacketCreator.messageToBytePacket(message, clientid, command, userId, KEY);
            Packet pkt = new Packet(bytePkt, KEY);
            assert Arrays.equals(BytePacketCreator.packetToBytePacket(pkt, KEY), bytePkt);
        }
        catch (Exception e) {
            assert false;
        }
    }

    @Test
    void PacketSafetyTest1() {
        byte[] message = {1, 2, 3, 4};
        byte clientid = 28;
        int command = 99;
        int userId = 435;

        testTemplate(message, clientid, command, userId, ua.edu.ukma.Key.KEY);
    }

    @Test
    void PacketSafetyTest2() {
        byte[] message = {1, 1, 1, 127, -13, 12, 0, 14, 15, -78};
        byte clientid = 100;
        int command = 6;
        int userId = 78;

        testTemplate(message, clientid, command, userId, ua.edu.ukma.Key.KEY);
    }

}
