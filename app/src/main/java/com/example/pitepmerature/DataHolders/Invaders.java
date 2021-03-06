package com.example.pitepmerature.DataHolders;

public class Invaders {

    public static final byte[] ALIEN_1_FRAME_1 = new byte[]{
            (byte) 0b00011000,
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b11011011,
            (byte) 0b11111111,
            (byte) 0b00100100,
            (byte) 0b01011010,
            (byte) 0b10100101
    };

    public static final byte[] ALIEN_1_FRAME_2 = new byte[]{
            (byte) 0b00011000,
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b11011011,
            (byte) 0b11111111,
            (byte) 0b00100100,
            (byte) 0b01011010,
            (byte) 0b01000010
    };

    public static final byte[] ALIEN_2_FRAME_1 = new byte[]{
            (byte) 0b00000000,
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b11011011,
            (byte) 0b11011011,
            (byte) 0b01111110,
            (byte) 0b00100100,
            (byte) 0b11000011
    };

    public static final byte[] ALIEN_2_FRAME_2 = new byte[]{
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b11011011,
            (byte) 0b11011011,
            (byte) 0b01111110,
            (byte) 0b00100100,
            (byte) 0b00100100,
            (byte) 0b00100100
    };

    public static final byte[] ALIEN_3_FRAME_1 = new byte[]{
            (byte) 0b00100100,
            (byte) 0b00100100,
            (byte) 0b01111110,
            (byte) 0b11011011,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b10100101,
            (byte) 0b00100100
    };

    public static final byte[] ALIEN_3_FRAME_2 = new byte[]{
            (byte) 0b00100100,
            (byte) 0b10100101,
            (byte) 0b11111111,
            (byte) 0b11011011,
            (byte) 0b11111111,
            (byte) 0b01111110,
            (byte) 0b00100100,
            (byte) 0b01000010
    };

    public static final byte[] ALIEN_4_FRAME_1 = new byte[]{
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b00110011,
            (byte) 0b01111110,
            (byte) 0b00111100,
            (byte) 0b00000000,
            (byte) 0b00001000,
            (byte) 0b00000000
    };

    public static final byte[] ALIEN_4_FRAME_2 = new byte[]{
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b10011001,
            (byte) 0b01111110,
            (byte) 0b00111100,
            (byte) 0b00000000,
            (byte) 0b00001000,
            (byte) 0b00001000
    };

    public static final byte[] ALIEN_4_FRAME_3 = new byte[]{
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b11001100,
            (byte) 0b01111110,
            (byte) 0b00111100,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00001000
    };

    public static final byte[] ALIEN_4_FRAME_4 = new byte[]{
            (byte) 0b00111100,
            (byte) 0b01111110,
            (byte) 0b01100110,
            (byte) 0b01111110,
            (byte) 0b00111100,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000
    };

    public static final byte[][] FRAMES = new byte[][]{
            ALIEN_1_FRAME_1, ALIEN_1_FRAME_1, ALIEN_1_FRAME_2, ALIEN_1_FRAME_2, ALIEN_1_FRAME_1, ALIEN_1_FRAME_1, ALIEN_1_FRAME_2, ALIEN_1_FRAME_2,
            ALIEN_2_FRAME_1, ALIEN_2_FRAME_1, ALIEN_2_FRAME_2, ALIEN_2_FRAME_2, ALIEN_2_FRAME_1, ALIEN_2_FRAME_1, ALIEN_2_FRAME_2, ALIEN_2_FRAME_2,
            ALIEN_3_FRAME_1, ALIEN_3_FRAME_1, ALIEN_3_FRAME_2, ALIEN_3_FRAME_2, ALIEN_3_FRAME_1, ALIEN_3_FRAME_1, ALIEN_3_FRAME_2, ALIEN_3_FRAME_2,
            ALIEN_4_FRAME_1, ALIEN_4_FRAME_2, ALIEN_4_FRAME_3, ALIEN_4_FRAME_4, ALIEN_4_FRAME_1, ALIEN_4_FRAME_2, ALIEN_4_FRAME_3, ALIEN_4_FRAME_4
    };
}
