package one.papachi.ifconfig4j.ffm.amd64.windows;

import one.papachi.ifconfig4j.api.NetIfConfig;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class IP_ADDRESS_PREFIX {

    final MemorySegment pointer;

    public IP_ADDRESS_PREFIX(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public SOCKADDR_INET Prefix() {
        return new SOCKADDR_INET(pointer.asSlice(0, 28));
    }

    public int PrefixLength() {
        return pointer.get(JAVA_BYTE, 28) & 0xFF;
    }

}
