package one.papachi.ifconfig4j.ffm.amd64.windows;

import one.papachi.ifconfig4j.api.NetIfConfig;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class SOCKADDR_INET {

    final MemorySegment pointer;

    public SOCKADDR_INET(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public SOCKADDR_IN Ipv4() {
        return new SOCKADDR_IN(pointer.asSlice(0, 16));
    }

    public SOCKADDR_IN6 Ipv6() {
        return new SOCKADDR_IN6(pointer.asSlice(0, 28));
    }

    public int si_family() {
        return pointer.get(JAVA_SHORT, 0);
    }

    public void setSiFamily(int si_family) {
        pointer.set(JAVA_SHORT, 0, (short) si_family);
    }

}
