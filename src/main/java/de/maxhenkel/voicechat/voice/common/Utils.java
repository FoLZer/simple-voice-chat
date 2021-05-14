package de.maxhenkel.voicechat.voice.common;

public class Utils {
    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }
}
