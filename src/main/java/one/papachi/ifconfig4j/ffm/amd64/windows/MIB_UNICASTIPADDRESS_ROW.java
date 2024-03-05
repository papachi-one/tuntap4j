package one.papachi.ifconfig4j.ffm.amd64.windows;

import one.papachi.ifconfig4j.api.NetIfConfig;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class MIB_UNICASTIPADDRESS_ROW {

    final MemorySegment pointer;

    public MIB_UNICASTIPADDRESS_ROW(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public SOCKADDR_INET Address() {
        return new SOCKADDR_INET(pointer.asSlice(0, 28));
    }

    public long InterfaceLuid() {
        return pointer.get(JAVA_LONG, 32);
    }

    public int InterfaceIndex() {
        return pointer.get(JAVA_INT, 40);
    }

    public int PrefixOrigin() {
        return pointer.get(JAVA_INT, 44);
    }

    public int SuffixOrigin() {
        return pointer.get(JAVA_INT, 48);
    }

    public int ValidLifetime() {
        return pointer.get(JAVA_INT, 52);
    }

    public int PreferredLifetime() {
        return pointer.get(JAVA_INT, 56);
    }

    public int OnLinkPrefixLength() {
        return pointer.get(JAVA_BYTE, 60) & 0xFF;
    }

    public int SkipAsSource() {
        return pointer.get(JAVA_BYTE, 61);
    }

    public int DadState() {
        return pointer.get(JAVA_INT, 64);
    }

    public int ScopeId() {
        return pointer.get(JAVA_INT, 68);
    }

    public long CreationTimeStamp() {
        return pointer.get(JAVA_LONG, 72);
    }

    public void InterfaceLuid(long interfaceLuid) {
        pointer.set(JAVA_LONG, 32, interfaceLuid);
    }

    public void InterfaceIndex(int interfaceIndex) {
        pointer.set(JAVA_INT, 40, interfaceIndex);
    }

    public void PrefixOrigin(int prefixOrigin) {
        pointer.set(JAVA_INT, 44, prefixOrigin);
    }

    public void SuffixOrigin(int suffixOrigin) {
        pointer.set(JAVA_INT, 48, suffixOrigin);
    }

    public void ValidLifetime(int validLifetime) {
        pointer.set(JAVA_INT, 52, validLifetime);
    }

    public void PreferredLifetime(int preferredLifetime) {
        pointer.set(JAVA_INT, 56, preferredLifetime);
    }

    public void OnLinkPrefixLength(int onLinkPrefixLength) {
        pointer.set(JAVA_BYTE, 60, (byte) onLinkPrefixLength);
    }

    public void SkipAsSource(int skipAsSource) {
        pointer.set(JAVA_BYTE, 61, (byte) skipAsSource);
    }

    public void DadState(int dadState) {
        pointer.set(JAVA_INT, 64, dadState);
    }

    public void ScopeId(int scopeId) {
        pointer.set(JAVA_INT, 68, scopeId);
    }

    public void CreationTimeStamp(long creationTimeStamp) {
        pointer.set(JAVA_LONG, 72, creationTimeStamp);
    }

}
