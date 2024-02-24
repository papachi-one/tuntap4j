package one.papachi.ifconfig4j.api;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.net.InetAddress;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

public interface NetIfConfig {

    record NetIf(String name) {
    }

    record NetIfAddress(InetAddress address, int prefixLength) {
    }

    void getNetIfList();

    void getNetIfAddressList();

    void addNetIfAddress(NetIf netIf, NetIfAddress netIfAddress);

    void removeNetIfAddress(NetIf netIf, NetIfAddress netIfAddress);

    record NetRoute() {
    }

    void getNetRoutes();

    void addNetRoute();

    void removeNetRoute();


    class WindowsNetApi {
        Arena arena = Arena.ofAuto();
        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = linker.defaultLookup()
                .or(SymbolLookup.libraryLookup("kernel32", arena))
                .or(SymbolLookup.libraryLookup("combase", arena))
                .or(SymbolLookup.libraryLookup("advapi32", arena))
                .or(SymbolLookup.libraryLookup("iphlpapi", arena));

        MethodHandle GetIpInterfaceTable;
        MethodHandle GetIpInterfaceEntry;
        StructLayout MIB_IPINTERFACE_ROW;//168
        StructLayout MIB_IPINTERFACE_TABLE;


        MethodHandle GetUnicastIpAddressTable;
        MethodHandle GetUnicastIpAddressEntry;
        MethodHandle CreateUnicastIpAddressEntry;
        MethodHandle DeleteUnicastIpAddressEntry;
        StructLayout MIB_UNICASTIPADDRESS_ROW;//80
        StructLayout MIB_UNICASTIPADDRESS_TABLE;

        MethodHandle GetIpForwardTable2;
        MethodHandle GetIpForwardEntry2;
        MethodHandle CreateIpForwardEntry2;
        MethodHandle DeleteIpForwardEntry2;
        StructLayout MIB_IPFORWARD_ROW2;//104
        StructLayout MIB_IPFORWARD_TABLE2;

        ValueLayout BOOLEAN = JAVA_BYTE;
        ValueLayout UCHAR = JAVA_BYTE;
        ValueLayout UINT8 = JAVA_BYTE;
        ValueLayout ULONG = JAVA_INT;
        ValueLayout ULONG64 = JAVA_LONG;
        ValueLayout LARGE_INTEGER = JAVA_LONG;

        ValueLayout ADDRESS_FAMILY = JAVA_SHORT;
        ValueLayout NET_LUID = JAVA_LONG;
        ValueLayout NET_IFINDEX = JAVA_INT;

        ValueLayout SCOPE_ID = JAVA_INT;

        ValueLayout NL_ROUTER_DISCOVERY_BEHAVIOR = JAVA_INT;
        ValueLayout NL_LINK_LOCAL_ADDRESS_BEHAVIOR = JAVA_INT;
        ValueLayout NL_INTERFACE_OFFLOAD_ROD = JAVA_BYTE;
        ValueLayout NL_PREFIX_ORIGIN = JAVA_INT;
        ValueLayout NL_SUFFIX_ORIGIN = JAVA_INT;
        ValueLayout NL_DAD_STATE = JAVA_INT;
        ValueLayout NL_ROUTE_PROTOCOL = JAVA_INT;
        ValueLayout NL_ROUTE_ORIGIN = JAVA_INT;

        StructLayout SOCKADDR_IN;

        StructLayout SOCKADDR_IN6;

        UnionLayout SOCKADDR_INET;

        StructLayout IP_ADDRESS_PREFIX;

        {
            GetIpInterfaceTable = linker.downcallHandle(lookup.find("GetIpInterfaceTable").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS));
            GetIpInterfaceEntry = linker.downcallHandle(lookup.find("GetIpInterfaceEntry").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            GetUnicastIpAddressTable = linker.downcallHandle(lookup.find("GetUnicastIpAddressTable").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS));
            GetUnicastIpAddressEntry = linker.downcallHandle(lookup.find("GetUnicastIpAddressEntry").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            CreateUnicastIpAddressEntry = linker.downcallHandle(lookup.find("CreateUnicastIpAddressEntry").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            DeleteUnicastIpAddressEntry = linker.downcallHandle(lookup.find("DeleteUnicastIpAddressEntry").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            GetIpForwardTable2 = linker.downcallHandle(lookup.find("GetIpForwardTable2").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS));
            GetIpForwardEntry2 = linker.downcallHandle(lookup.find("GetIpForwardEntry2").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            CreateIpForwardEntry2 = linker.downcallHandle(lookup.find("CreateIpForwardEntry2").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));
            DeleteIpForwardEntry2 = linker.downcallHandle(lookup.find("DeleteIpForwardEntry2").orElseThrow(),
                    FunctionDescriptor.of(JAVA_INT, ADDRESS));


        }

    }

    public static void main(String[] args) {
        WindowsNetApi api = new WindowsNetApi();
        System.out.println(api.arena.allocate(api.SOCKADDR_IN).byteSize());
        System.out.println(api.arena.allocate(api.SOCKADDR_IN6).byteSize());
        System.out.println(api.arena.allocate(api.SOCKADDR_INET).byteSize());
        System.out.println(api.arena.allocate(api.IP_ADDRESS_PREFIX).byteSize());
        System.out.println(api.arena.allocate(api.MIB_IPINTERFACE_ROW).byteSize());
        System.out.println(api.arena.allocate(api.MIB_UNICASTIPADDRESS_ROW).byteSize());
        System.out.println(api.arena.allocate(api.MIB_IPFORWARD_ROW2).byteSize());
        System.out.println(api.arena.allocate(api.MIB_IPINTERFACE_TABLE).byteSize());
        System.out.println(api.arena.allocate(api.MIB_UNICASTIPADDRESS_TABLE).byteSize());
        System.out.println(api.arena.allocate(api.MIB_IPFORWARD_TABLE2).byteSize());
    }

    class SOCKADDR_IN {

        final MemorySegment pointer;

        SOCKADDR_IN(MemorySegment pointer) {
            this.pointer = pointer;
        }

        int sin_family() {
            return pointer.get(JAVA_SHORT, 0);
        }

        int sin_port() {
            return pointer.get(JAVA_SHORT, 2);
        }

        int sin_addr() {
            return pointer.get(JAVA_INT, 4);
        }

    }

    class SOCKADDR_IN6 {

        final MemorySegment pointer;

        SOCKADDR_IN6(MemorySegment pointer) {
            this.pointer = pointer;
        }

        int sin6_family() {
            return pointer.get(JAVA_SHORT, 0);
        }

        int sin6_port() {
            return pointer.get(JAVA_SHORT, 2);
        }

        int sin6_flowinfo() {
            return pointer.get(JAVA_INT, 4);
        }

        byte[] sin6_addr() {
            byte[] sin6_addr = new byte[16];
            sin6_addr[0] = pointer.get(JAVA_BYTE, 8);
            sin6_addr[1] = pointer.get(JAVA_BYTE, 8 + 1);
            sin6_addr[2] = pointer.get(JAVA_BYTE, 8 + 2);
            sin6_addr[3] = pointer.get(JAVA_BYTE, 8 + 3);
            sin6_addr[4] = pointer.get(JAVA_BYTE, 8 + 4);
            sin6_addr[5] = pointer.get(JAVA_BYTE, 8 + 5);
            sin6_addr[6] = pointer.get(JAVA_BYTE, 8 + 6);
            sin6_addr[7] = pointer.get(JAVA_BYTE, 8 + 7);
            sin6_addr[8] = pointer.get(JAVA_BYTE, 8 + 8);
            sin6_addr[9] = pointer.get(JAVA_BYTE, 8 + 9);
            sin6_addr[10] = pointer.get(JAVA_BYTE, 8 + 10);
            sin6_addr[11] = pointer.get(JAVA_BYTE, 8 + 11);
            sin6_addr[12] = pointer.get(JAVA_BYTE, 8 + 12);
            sin6_addr[13] = pointer.get(JAVA_BYTE, 8 + 13);
            sin6_addr[14] = pointer.get(JAVA_BYTE, 8 + 14);
            sin6_addr[15] = pointer.get(JAVA_BYTE, 8 + 15);
            return sin6_addr;
        }

        int sin6_scope_id() {
            return pointer.get(JAVA_INT, 24);
        }

    }

    class SOCKADDR_INET {

        final MemorySegment pointer;

        SOCKADDR_INET(MemorySegment pointer) {
            this.pointer = pointer;
        }

        SOCKADDR_IN Ipv4() {
            return new SOCKADDR_IN(pointer.asSlice(0, 16));
        }

        SOCKADDR_IN6 Ipv6() {
            return new SOCKADDR_IN6(pointer.asSlice(0, 28));
        }

        int si_family() {
            return pointer.get(JAVA_SHORT, 0);
        }

    }

    class IP_ADDRESS_PREFIX {

        final MemorySegment pointer;

        IP_ADDRESS_PREFIX(MemorySegment pointer) {
            this.pointer = pointer;
        }

        SOCKADDR_INET Prefix() {
            return new SOCKADDR_INET(pointer.asSlice(0, 28));
        }

        int PrefixLength() {
            return pointer.get(JAVA_BYTE, 28) & 0xFF;
        }

    }

    class MIB_IPINTERFACE_ROW {

        final MemorySegment pointer;

        MIB_IPINTERFACE_ROW(MemorySegment pointer) {
            this.pointer = pointer;
        }

        int Family() {
            return pointer.get(JAVA_SHORT, 0);
        }

        long InterfaceLuid() {
            return pointer.get(JAVA_LONG, 8);
        }

        int InterfaceIndex() {
            return pointer.get(JAVA_INT, 16);
        }

        int MaxReassemblySize() {
            return pointer.get(JAVA_INT, 20);
        }

        long InterfaceIdentifier() {
            return pointer.get(JAVA_LONG, 24);
        }

        int MinRouterAdvertisementInterval() {
            return pointer.get(JAVA_INT, 32);
        }

        int MaxRouterAdvertisementInterval() {
            return pointer.get(JAVA_INT, 36);
        }

        int AdvertisingEnabled() {
            return pointer.get(JAVA_BYTE, 40);
        }

        int ForwardingEnabled() {
            return pointer.get(JAVA_BYTE, 41);
        }

        int WeakHostSend() {
            return pointer.get(JAVA_BYTE, 42);
        }

        int WeakHostReceive() {
            return pointer.get(JAVA_BYTE, 43);
        }

        int UseAutomaticMetric() {
            return pointer.get(JAVA_BYTE, 44);
        }

        int UseNeighborUnreachabilityDetection() {
            return pointer.get(JAVA_BYTE, 45);
        }

        int ManagedAddressConfigurationSupported() {
            return pointer.get(JAVA_BYTE, 46);
        }

        int OtherStatefulConfigurationSupported() {
            return pointer.get(JAVA_BYTE, 47);
        }

        int AdvertiseDefaultRoute() {
            return pointer.get(JAVA_BYTE, 48);
        }

        int RouterDiscoveryBehavior() {
            return pointer.get(JAVA_INT, 52);
        }

        int DadTransmits() {
            return pointer.get(JAVA_INT, 56);
        }

        int BaseReachableTime() {
            return pointer.get(JAVA_INT, 60);
        }

        int RetransmitTime() {
            return pointer.get(JAVA_INT, 64);
        }

        int PathMtuDiscoveryTimeout() {
            return pointer.get(JAVA_INT, 68);
        }

        int LinkLocalAddressBehavior() {
            return pointer.get(JAVA_INT, 72);
        }

        int LinkLocalAddressTimeout() {
            return pointer.get(JAVA_INT, 76);
        }

        int ZoneIndices() {
            return pointer.get(JAVA_INT, 80);
        }

        int SitePrefixLength() {
            return pointer.get(JAVA_INT, 144);
        }

        int Metric() {
            return pointer.get(JAVA_INT, 148);
        }

        int NlMtu() {
            return pointer.get(JAVA_INT, 152);
        }

        int Connected() {
            return pointer.get(JAVA_BYTE, 156);
        }

        int SupportsWakeUpPatterns() {
            return pointer.get(JAVA_BYTE, 157);
        }

        int SupportsNeighborDiscovery() {
            return pointer.get(JAVA_BYTE, 158);
        }

        int SupportsRouterDiscovery() {
            return pointer.get(JAVA_BYTE, 159);
        }

        int ReachableTime() {
            return pointer.get(JAVA_INT, 160);
        }

        int TransmitOffload() {
            return pointer.get(JAVA_BYTE, 164);
        }

        int ReceiveOffload() {
            return pointer.get(JAVA_BYTE, 165);
        }

        int DisableDefaultRoutes() {
            return pointer.get(JAVA_BYTE, 166);
        }

    }

    class MIB_UNICASTIPADDRESS_ROW {

        final MemorySegment pointer;

        MIB_UNICASTIPADDRESS_ROW(MemorySegment pointer) {
            this.pointer = pointer;
        }

        SOCKADDR_INET Address() {
            return new SOCKADDR_INET(pointer.asSlice(0, 28));
        }

        long InterfaceLuid() {
            return pointer.get(JAVA_LONG, 32);
        }

        int InterfaceIndex() {
            return pointer.get(JAVA_INT, 40);
        }

        int PrefixOrigin() {
            return pointer.get(JAVA_INT, 44);
        }

        int SuffixOrigin() {
            return pointer.get(JAVA_INT, 48);
        }

        int ValidLifetime() {
            return pointer.get(JAVA_INT, 52);
        }

        int PreferredLifetime() {
            return pointer.get(JAVA_INT, 56);
        }

        int OnLinkPrefixLength() {
            return pointer.get(JAVA_BYTE, 60) & 0xFF;
        }

        int SkipAsSource() {
            return pointer.get(JAVA_BYTE, 61);
        }

        int DadState() {
            return pointer.get(JAVA_INT, 64);
        }

        int ScopeId() {
            return pointer.get(JAVA_INT, 68);
        }

        long CreationTimeStamp() {
            return pointer.get(JAVA_LONG, 72);
        }

    }

    class MIB_IPFORWARD_ROW2 {

        final MemorySegment pointer;

        MIB_IPFORWARD_ROW2(MemorySegment pointer) {
            this.pointer = pointer;
        }

        long InterfaceLuid() {
            return pointer.get(JAVA_LONG, 0);
        }

        int InterfaceIndex() {
            return pointer.get(JAVA_INT, 8);
        }

        IP_ADDRESS_PREFIX DestinationPrefix() {
            return new IP_ADDRESS_PREFIX(pointer.asSlice(12, 32));
        }

        SOCKADDR_INET NextHop() {
            return new SOCKADDR_INET(pointer.asSlice(44, 28));
        }

        int SitePrefixLength() {
            return pointer.get(JAVA_BYTE, 72) & 0xFF;
        }

        int ValidLifetime() {
            return pointer.get(JAVA_INT, 76);
        }

        int PreferredLifetime() {
            return pointer.get(JAVA_INT, 80);
        }

        int Metric() {
            return pointer.get(JAVA_INT, 84);
        }

        int Protocol() {
            return pointer.get(JAVA_INT, 88);
        }

        int Loopback() {
            return pointer.get(JAVA_BYTE, 92);
        }

        int AutoconfigureAddress() {
            return pointer.get(JAVA_BYTE, 93);
        }

        int Publish() {
            return pointer.get(JAVA_BYTE, 94);
        }

        int Immortal() {
            return pointer.get(JAVA_BYTE, 95);
        }

        int Age() {
            return pointer.get(JAVA_INT, 96);
        }

        int Origin() {
            return pointer.get(JAVA_INT, 100);
        }

    }

}
