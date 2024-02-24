package one.papachi.ifconfig4j.ffm.amd64.windows;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

public class MIB_IPINTERFACE_ROW {

    final MemorySegment pointer;

    public MIB_IPINTERFACE_ROW(MemorySegment pointer) {
        this.pointer = pointer;
    }

    public int Family() {
        return pointer.get(JAVA_SHORT, 0);
    }

    public long InterfaceLuid() {
        return pointer.get(JAVA_LONG, 8);
    }

    public int InterfaceIndex() {
        return pointer.get(JAVA_INT, 16);
    }

    public int MaxReassemblySize() {
        return pointer.get(JAVA_INT, 20);
    }

    public long InterfaceIdentifier() {
        return pointer.get(JAVA_LONG, 24);
    }

    public int MinRouterAdvertisementInterval() {
        return pointer.get(JAVA_INT, 32);
    }

    public int MaxRouterAdvertisementInterval() {
        return pointer.get(JAVA_INT, 36);
    }

    public int AdvertisingEnabled() {
        return pointer.get(JAVA_BYTE, 40);
    }

    public int ForwardingEnabled() {
        return pointer.get(JAVA_BYTE, 41);
    }

    public int WeakHostSend() {
        return pointer.get(JAVA_BYTE, 42);
    }

    public int WeakHostReceive() {
        return pointer.get(JAVA_BYTE, 43);
    }

    public int UseAutomaticMetric() {
        return pointer.get(JAVA_BYTE, 44);
    }

    public int UseNeighborUnreachabilityDetection() {
        return pointer.get(JAVA_BYTE, 45);
    }

    public int ManagedAddressConfigurationSupported() {
        return pointer.get(JAVA_BYTE, 46);
    }

    public int OtherStatefulConfigurationSupported() {
        return pointer.get(JAVA_BYTE, 47);
    }

    public int AdvertiseDefaultRoute() {
        return pointer.get(JAVA_BYTE, 48);
    }

    public int RouterDiscoveryBehavior() {
        return pointer.get(JAVA_INT, 52);
    }

    public int DadTransmits() {
        return pointer.get(JAVA_INT, 56);
    }

    public int BaseReachableTime() {
        return pointer.get(JAVA_INT, 60);
    }

    public int RetransmitTime() {
        return pointer.get(JAVA_INT, 64);
    }

    public int PathMtuDiscoveryTimeout() {
        return pointer.get(JAVA_INT, 68);
    }

    public int LinkLocalAddressBehavior() {
        return pointer.get(JAVA_INT, 72);
    }

    public int LinkLocalAddressTimeout() {
        return pointer.get(JAVA_INT, 76);
    }

    public int ZoneIndices() {
        return pointer.get(JAVA_INT, 80);
    }

    public int SitePrefixLength() {
        return pointer.get(JAVA_INT, 144);
    }

    public int Metric() {
        return pointer.get(JAVA_INT, 148);
    }

    public int NlMtu() {
        return pointer.get(JAVA_INT, 152);
    }

    public int Connected() {
        return pointer.get(JAVA_BYTE, 156);
    }

    public int SupportsWakeUpPatterns() {
        return pointer.get(JAVA_BYTE, 157);
    }

    public int SupportsNeighborDiscovery() {
        return pointer.get(JAVA_BYTE, 158);
    }

    public int SupportsRouterDiscovery() {
        return pointer.get(JAVA_BYTE, 159);
    }

    public int ReachableTime() {
        return pointer.get(JAVA_INT, 160);
    }

    public int TransmitOffload() {
        return pointer.get(JAVA_BYTE, 164);
    }

    public int ReceiveOffload() {
        return pointer.get(JAVA_BYTE, 165);
    }

    public int DisableDefaultRoutes() {
        return pointer.get(JAVA_BYTE, 166);
    }

}
