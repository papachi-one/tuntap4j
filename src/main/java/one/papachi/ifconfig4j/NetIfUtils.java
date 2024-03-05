package one.papachi.ifconfig4j;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class NetIfUtils {

    public static int prefixLen(InetAddress mask) {
        int prefixLen = 0;
        ByteBuffer buffer = ByteBuffer.wrap(mask.getAddress());
        while (buffer.hasRemaining()) {
            prefixLen += Integer.bitCount(buffer.getInt());
        }
        return prefixLen;
    }

    public static byte[] mask(int prefixLen) {
        byte[] mask = new byte[prefixLen > 32 ? 16 : 4];
        for (int i = 0; i < prefixLen; i++) {
            int bytePosition = i / 8;
            int bitPosition = i % 8;
            mask[bytePosition] |= (byte) (1 << (7 - bitPosition));
        }
        return mask;
    }

    public static byte[] broadcast(byte[] address, byte[] mask) {
        byte[] broadcast = new byte[address.length];
        for (int i = 0; i < broadcast.length; i++) {
            broadcast[i] = (byte) (address[i] | ~mask[i]);
        }
        return broadcast;
    }

}
