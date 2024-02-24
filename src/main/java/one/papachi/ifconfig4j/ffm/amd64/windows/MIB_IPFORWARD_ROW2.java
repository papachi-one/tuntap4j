package one.papachi.ifconfig4j.ffm.amd64.windows;

import one.papachi.ifconfig4j.api.NetIfConfig;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class MIB_IPFORWARD_ROW2 {

    final MemorySegment pointer;

    public MIB_IPFORWARD_ROW2(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public long InterfaceLuid() {
        return pointer.get(JAVA_LONG, 0);
    }

    public int InterfaceIndex() {
        return pointer.get(JAVA_INT, 8);
    }

    public IP_ADDRESS_PREFIX DestinationPrefix() {
        return new IP_ADDRESS_PREFIX(pointer.asSlice(12, 32));
    }

    public SOCKADDR_INET NextHop() {
        return new SOCKADDR_INET(pointer.asSlice(44, 28));
    }

    public int SitePrefixLength() {
        return pointer.get(JAVA_BYTE, 72) & 0xFF;
    }

    public int ValidLifetime() {
        return pointer.get(JAVA_INT, 76);
    }

    public int PreferredLifetime() {
        return pointer.get(JAVA_INT, 80);
    }

    public int Metric() {
        return pointer.get(JAVA_INT, 84);
    }

    public int Protocol() {
        return pointer.get(JAVA_INT, 88);
    }

    public int Loopback() {
        return pointer.get(JAVA_BYTE, 92);
    }

    public int AutoconfigureAddress() {
        return pointer.get(JAVA_BYTE, 93);
    }

    public int Publish() {
        return pointer.get(JAVA_BYTE, 94);
    }

    public int Immortal() {
        return pointer.get(JAVA_BYTE, 95);
    }

    public int Age() {
        return pointer.get(JAVA_INT, 96);
    }

    public int Origin() {
        return pointer.get(JAVA_INT, 100);
    }

}
