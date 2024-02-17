package one.papachi.tuntap4j;

import java.nio.ByteBuffer;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i < 32; i++) {
            System.out.println(i == prefixLen(mask(i)));
        }
    }

    static byte[] mask(int prefixLen) {
        byte[] mask = new byte[4];
        for (int i = 0; i < prefixLen; i++) {
            int bytePosition = i / 8;
            int bitPosition = i % 8;
            mask[bytePosition] |= (1 << (7 - bitPosition));
        }
        return mask;
    }

    static int prefixLen(byte[] mask) {
        int prefixLen = 0;
        ByteBuffer buffer = ByteBuffer.wrap(mask);
        while (buffer.hasRemaining()) {
            prefixLen += Integer.bitCount(buffer.getInt());
        }
        return prefixLen;
    }

}
