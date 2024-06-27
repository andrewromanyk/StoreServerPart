package ua.edu.ukma;

import org.junit.jupiter.api.Test;

class CRC16Test {

    @Test
    void CRC() {
        assert 10560 == CRC16.CRC(new byte[]{0, 0, 0, 99});
        assert 27792 == CRC16.CRC(new byte[]{1, 1, 1, 1});
        assert 9781 == CRC16.CRC(new byte[]{10, -10, 79, -89, 15, 18, -99});
        assert 0 == CRC16.CRC(new byte[]{0});
    }
}