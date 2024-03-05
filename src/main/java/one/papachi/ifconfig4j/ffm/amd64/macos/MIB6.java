package one.papachi.ifconfig4j.ffm.amd64.macos;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class MIB6 {

    private final MemorySegment pointer;

    public MIB6(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int int0() {
        return pointer.get(JAVA_INT, 0);
    }

    public void int0(int value) {
        pointer.set(JAVA_INT, 0, value);
    }

    public int int1() {
        return pointer.get(JAVA_INT, 4);
    }

    public void int1(int value) {
        pointer.set(JAVA_INT, 4, value);
    }

    public int int2() {
        return pointer.get(JAVA_INT, 8);
    }

    public void int2(int value) {
        pointer.set(JAVA_INT, 8, value);
    }
    public int int3() {
        return pointer.get(JAVA_INT, 12);
    }

    public void int3(int value) {
        pointer.set(JAVA_INT, 12, value);
    }
    public int int4() {
        return pointer.get(JAVA_INT, 16);
    }

    public void int4(int value) {
        pointer.set(JAVA_INT, 16, value);
    }
    public int int5() {
        return pointer.get(JAVA_INT, 20);
    }

    public void int5(int value) {
        pointer.set(JAVA_INT, 20, value);
    }

}
