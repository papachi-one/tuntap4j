package one.papachi.ifconfig4j.ffm.amd64.macos;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class RT_MSGHDR {

    final MemorySegment pointer;

    public RT_MSGHDR(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int rtm_msglen() {
        return pointer.get(JAVA_SHORT, 0) & 0xFFFF;
    }

    public void rtm_msglen(int value) {
        pointer.set(JAVA_SHORT, 0, (short) value);
    }

    public int rtm_version() {
        return pointer.get(JAVA_BYTE, 2) & 0xFF;
    }

    public void rtm_version(int value) {
        pointer.set(JAVA_BYTE, 2, (byte) value);
    }

    public int rtm_type() {
        return pointer.get(JAVA_BYTE, 3) & 0xFF;
    }

    public void rtm_type(int value) {
        pointer.set(JAVA_BYTE, 3, (byte) value);
    }

    public int rtm_index() {
        return pointer.get(JAVA_SHORT, 4) & 0xFFFF;
    }

    public void rtm_index(int value) {
        pointer.set(JAVA_SHORT, 4, (short) value);
    }

    public int rtm_flags() {
        return pointer.get(JAVA_INT, 8);
    }

    public void rtm_flags(int value) {
        pointer.set(JAVA_INT, 8, value);
    }

    public int rtm_addrs() {
        return pointer.get(JAVA_INT, 12);
    }

    public void rtm_addrs(int value) {
        pointer.set(JAVA_INT, 12, value);
    }

    public int rtm_pid() {
        return pointer.get(JAVA_INT, 16);
    }

    public void rtm_pid(int value) {
        pointer.set(JAVA_INT, 16, value);
    }

    public int rtm_seq() {
        return pointer.get(JAVA_INT, 20);
    }

    public void rtm_seq(int value) {
        pointer.set(JAVA_INT, 20, value);
    }

    public int rtm_errno() {
        return pointer.get(JAVA_INT, 24);
    }

    public void rtm_errno(int value) {
        pointer.set(JAVA_INT, 24, value);
    }

    public int rtm_use() {
        return pointer.get(JAVA_INT, 28);
    }

    public void rtm_use(int value) {
        pointer.set(JAVA_INT, 28, value);
    }

    public int rtm_inits() {
        return pointer.get(JAVA_INT, 32);
    }

    public void rtm_inits(int value) {
        pointer.set(JAVA_INT, 32, value);
    }

    public int rtm_rmx_locks() {
        return pointer.get(JAVA_INT, 36);
    }

    public void rtm_rmx_locks(int value) {
        pointer.set(JAVA_INT, 36, value);
    }

    public int rtm_rmx_mtu() {
        return pointer.get(JAVA_INT, 40);
    }

    public void rtm_rmx_mtu(int value) {
        pointer.set(JAVA_INT, 40, value);
    }

    public int rtm_rmx_hopcount() {
        return pointer.get(JAVA_INT, 44);
    }

    public void rtm_rmx_hopcount(int value) {
        pointer.set(JAVA_INT, 44, value);
    }

    public int rtm_rmx_expire() {
        return pointer.get(JAVA_INT, 48);
    }

    public void rtm_rmx_expire(int value) {
        pointer.set(JAVA_INT, 48, value);
    }

    public int rtm_rmx_recvpipe() {
        return pointer.get(JAVA_INT, 52);
    }

    public void rtm_rmx_recvpipe(int value) {
        pointer.set(JAVA_INT, 52, value);
    }

    public int rtm_rmx_sendpipe() {
        return pointer.get(JAVA_INT, 56);
    }

    public void rtm_rmx_sendpipe(int value) {
        pointer.set(JAVA_INT, 56, value);
    }

    public int rtm_rmx_ssthresh() {
        return pointer.get(JAVA_INT, 60);
    }

    public void rtm_rmx_ssthresh(int value) {
        pointer.set(JAVA_INT, 60, value);
    }

    public int rtm_rmx_rtt() {
        return pointer.get(JAVA_INT, 64);
    }

    public void rtm_rmx_rtt(int value) {
        pointer.set(JAVA_INT, 64, value);
    }

    public int rtm_rmx_rttvar() {
        return pointer.get(JAVA_INT, 68);
    }

    public void rtm_rmx_rttvar(int value) {
        pointer.set(JAVA_INT, 68, value);
    }

    public int rtm_rmx_pksent() {
        return pointer.get(JAVA_INT, 72);
    }

    public void rtm_rmx_pksent(int value) {
        pointer.set(JAVA_INT, 72, value);
    }

    public int rtm_rmx_filler0() {
        return pointer.get(JAVA_INT, 76);
    }

    public void rtm_rmx_filler0(int value) {
        pointer.set(JAVA_INT, 76, value);
    }

    public int rtm_rmx_filler1() {
        return pointer.get(JAVA_INT, 80);
    }

    public void rtm_rmx_filler1(int value) {
        pointer.set(JAVA_INT, 80, value);
    }

    public int rtm_rmx_filler2() {
        return pointer.get(JAVA_INT, 84);
    }

    public void rtm_rmx_filler2(int value) {
        pointer.set(JAVA_INT, 84, value);
    }

    public int rtm_rmx_filler3() {
        return pointer.get(JAVA_INT, 88);
    }

    public void rtm_rmx_filler3(int value) {
        pointer.set(JAVA_INT, 88, value);
    }

}
