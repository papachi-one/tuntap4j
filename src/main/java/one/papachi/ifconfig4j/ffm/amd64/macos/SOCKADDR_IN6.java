package one.papachi.ifconfig4j.ffm.amd64.macos;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class SOCKADDR_IN6 {

    final MemorySegment pointer;

    public SOCKADDR_IN6(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int sin_len() {
        return pointer.get(JAVA_BYTE, 0) & 0xFF;
    }

    public int sin6_family() {
        return pointer.get(JAVA_BYTE, 1) & 0xFF;
    }

    public int sin6_port() {
        return pointer.get(JAVA_SHORT, 2) & 0xFFFF;
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

    public void sin_len(int value) {
        pointer.set(JAVA_BYTE, 0, (byte) value);
    }

    public void sin6_family(int value) {
        pointer.set(JAVA_BYTE, 1, (byte) value);
    }

    public void sin6_port(int value) {
        pointer.set(JAVA_INT, 2, (short) value);
    }

    public void sin6_flowinfo(int value) {
        pointer.set(JAVA_INT, 4, (short) value);
    }

    public void sin6_addr(byte[] value) {
        pointer.asByteBuffer().put(8, value);
    }

    public void sin6_scope_id(int value) {
        pointer.set(JAVA_INT, 24, value);
    }

}
