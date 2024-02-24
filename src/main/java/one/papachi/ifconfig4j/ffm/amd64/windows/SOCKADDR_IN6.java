package one.papachi.ifconfig4j.ffm.amd64.windows;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class SOCKADDR_IN6 {

    final MemorySegment pointer;

    public SOCKADDR_IN6(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int sin6_family() {
        return pointer.get(JAVA_SHORT, 0);
    }

    public int sin6_port() {
        return pointer.get(JAVA_SHORT, 2);
    }

    public int sin6_flowinfo() {
        return pointer.get(JAVA_INT, 4);
    }

    public byte[] sin6_addr() {
        byte[] sin6_addr = new byte[16];
        pointer.asByteBuffer().get(8, sin6_addr);
        return sin6_addr;
    }

    public int sin6_scope_id() {
        return pointer.get(JAVA_INT, 24);
    }

    public void setSin6Family(int sin6_family) {
        pointer.set(JAVA_SHORT, 0, (short) sin6_family);
    }

    public void setSin6Port(int sin6_port) {
        pointer.set(JAVA_INT, 2, (short) sin6_port);
    }

    public void setSin6Flowinfo(int sin6_flowinfo) {
        pointer.set(JAVA_INT, 4, (short) sin6_flowinfo);
    }

    public void setSin6Addr(byte[] sin6_addr) {
        pointer.asByteBuffer().put(8, sin6_addr);
    }

    public void setSin6Scope(int sin6_scope_id) {
        pointer.set(JAVA_INT, 24, sin6_scope_id);
    }

}
