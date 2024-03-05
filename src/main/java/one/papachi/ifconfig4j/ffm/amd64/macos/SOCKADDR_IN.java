package one.papachi.ifconfig4j.ffm.amd64.macos;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class SOCKADDR_IN {

    final MemorySegment pointer;

    public SOCKADDR_IN(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int sin_len() {
        return pointer.get(JAVA_BYTE, 0) & 0xFF;
    }

    public int sin_family() {
        return pointer.get(JAVA_BYTE, 1) & 0xFF;
    }

    public int sin_port() {
        return pointer.get(JAVA_SHORT, 2) & 0xFFFF;
    }

    public byte[] sin_addr() {
        byte[] sin_addr = new byte[4];
        pointer.asByteBuffer().get(4, sin_addr);
        return sin_addr;
    }

    public void sin_len(int value) {
        pointer.set(JAVA_BYTE, 0, (byte) value);
    }

    public void sin_family(int value) {
        pointer.set(JAVA_SHORT, 1, (byte) value);
    }

    public void sin_port(int value) {
        pointer.set(JAVA_SHORT, 2, (short) value);
    }

    public void sin_addr(byte[] value) {
        pointer.asByteBuffer().put(4, value);
    }

}
