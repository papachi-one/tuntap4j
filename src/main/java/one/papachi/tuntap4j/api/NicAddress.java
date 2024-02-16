package one.papachi.tuntap4j.api;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public record NicAddress(InetAddress ipAddress, int prefixLength) {

    public NicAddress(InetAddress ipAddress, InetAddress mask) {
        this(ipAddress, prefixLen(mask));
    }

    public byte[] mask() {
        return mask(prefixLength);
    }

    public byte[] broadcast() {
        int address = ByteBuffer.wrap(ipAddress.getAddress()).getInt();
        int mask = ByteBuffer.wrap(mask()).getInt();
        int broadcast = address | ~mask;
        return ByteBuffer.allocate(4).putInt(broadcast).array();
    }

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


}
