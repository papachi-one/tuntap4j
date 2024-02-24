package one.papachi.ifconfig4j.ffm.amd64.windows;

import one.papachi.ifconfig4j.api.NetIfConfig;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class MIB_UNICASTIPADDRESS_ROW {

    final MemorySegment pointer;

    public MIB_UNICASTIPADDRESS_ROW(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public SOCKADDR_INET getAddress() {
        return new SOCKADDR_INET(pointer.asSlice(0, 28));
    }

    public long getInterfaceLuid() {
        return pointer.get(JAVA_LONG, 32);
    }

    public int getInterfaceIndex() {
        return pointer.get(JAVA_INT, 40);
    }

    public int getPrefixOrigin() {
        return pointer.get(JAVA_INT, 44);
    }

    public int getSuffixOrigin() {
        return pointer.get(JAVA_INT, 48);
    }

    public int getValidLifetime() {
        return pointer.get(JAVA_INT, 52);
    }

    public int getPreferredLifetime() {
        return pointer.get(JAVA_INT, 56);
    }

    public int getOnLinkPrefixLength() {
        return pointer.get(JAVA_BYTE, 60) & 0xFF;
    }

    public int getSkipAsSource() {
        return pointer.get(JAVA_BYTE, 61);
    }

    public int getDadState() {
        return pointer.get(JAVA_INT, 64);
    }

    public int getScopeId() {
        return pointer.get(JAVA_INT, 68);
    }

    public long getCreationTimeStamp() {
        return pointer.get(JAVA_LONG, 72);
    }

    public void setInterfaceLuid(long interfaceLuid) {
        pointer.set(JAVA_LONG, 32, interfaceLuid);
    }

    public void setInterfaceIndex(int interfaceIndex) {
        pointer.set(JAVA_INT, 40, interfaceIndex);
    }

    public void setPrefixOrigin(int prefixOrigin) {
        pointer.set(JAVA_INT, 44, prefixOrigin);
    }

    public void setSuffixOrigin(int suffixOrigin) {
        pointer.set(JAVA_INT, 48, suffixOrigin);
    }

    public void setValidLifetime(int validLifetime) {
        pointer.set(JAVA_INT, 52, validLifetime);
    }

    public void setPreferredLifetime(int preferredLifetime) {
        pointer.set(JAVA_INT, 56, preferredLifetime);
    }

    public void setOnLinkPrefixLength(int onLinkPrefixLength) {
        pointer.set(JAVA_BYTE, 60, (byte) onLinkPrefixLength);
    }

    public void setSkipAsSource(int skipAsSource) {
        pointer.set(JAVA_BYTE, 61, (byte) skipAsSource);
    }

    public void setDadState(int dadState) {
        pointer.set(JAVA_INT, 64, dadState);
    }

    public void setScopeId(int scopeId) {
        pointer.set(JAVA_INT, 68, scopeId);
    }

    public void setCreationTimeStamp(long creationTimeStamp) {
        pointer.set(JAVA_LONG, 72, creationTimeStamp);
    }

}
