package one.papachi.ifconfig4j.ffm.amd64.windows;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class SOCKADDR_IN {

    final MemorySegment pointer;

    public SOCKADDR_IN(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int sin_family() {
        return pointer.get(JAVA_SHORT, 0);
    }

    public int sin_port() {
        return pointer.get(JAVA_SHORT, 2);
    }

    public byte[] sin_addr() {
        byte[] sin_addr = new byte[4];
        pointer.asByteBuffer().get(4, sin_addr);
        return sin_addr;
    }

    public void sin_family(int sin_family) {
        pointer.set(JAVA_SHORT, 0, (short) sin_family);
    }

    public void sin_port(int sin_port) {
        pointer.set(JAVA_SHORT, 2, (short) sin_port);
    }

    public void sin_addr(byte[] sin_addr) {
        pointer.asByteBuffer().put(4, sin_addr);
    }

}
