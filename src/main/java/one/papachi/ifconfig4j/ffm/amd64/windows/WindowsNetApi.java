package one.papachi.ifconfig4j.ffm.amd64.windows;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.net.InetAddress;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

public class WindowsNetApi {

    public static final Arena arena;
    public static final Linker linker;
    public static final SymbolLookup lookup;

    public static final MethodHandle GetIpInterfaceTable;
    public static final MethodHandle GetIpInterfaceEntry;
    public static final MethodHandle GetUnicastIpAddressTable;
    public static final MethodHandle GetUnicastIpAddressEntry;
    public static final MethodHandle CreateUnicastIpAddressEntry;
    public static final MethodHandle DeleteUnicastIpAddressEntry;
    public static final MethodHandle GetIpForwardTable2;
    public static final MethodHandle GetIpForwardEntry2;
    public static final MethodHandle CreateIpForwardEntry2;
    public static final MethodHandle DeleteIpForwardEntry2;

    public static final ValueLayout BOOLEAN = JAVA_BYTE;
    public static final ValueLayout UCHAR = JAVA_BYTE;
    public static final ValueLayout UINT8 = JAVA_BYTE;
    public static final ValueLayout ULONG = JAVA_INT;
    public static final ValueLayout ULONG64 = JAVA_LONG;
    public static final ValueLayout LARGE_INTEGER = JAVA_LONG;
    public static final ValueLayout ADDRESS_FAMILY = JAVA_SHORT;
    public static final ValueLayout NET_LUID = JAVA_LONG;
    public static final ValueLayout NET_IFINDEX = JAVA_INT;
    public static final ValueLayout SCOPE_ID = JAVA_INT;
    public static final ValueLayout NL_ROUTER_DISCOVERY_BEHAVIOR = JAVA_INT;
    public static final ValueLayout NL_LINK_LOCAL_ADDRESS_BEHAVIOR = JAVA_INT;
    public static final ValueLayout NL_INTERFACE_OFFLOAD_ROD = JAVA_BYTE;
    public static final ValueLayout NL_PREFIX_ORIGIN = JAVA_INT;
    public static final ValueLayout NL_SUFFIX_ORIGIN = JAVA_INT;
    public static final ValueLayout NL_DAD_STATE = JAVA_INT;
    public static final ValueLayout NL_ROUTE_PROTOCOL = JAVA_INT;
    public static final ValueLayout NL_ROUTE_ORIGIN = JAVA_INT;

    public static final StructLayout SOCKADDR_IN;
    public static final StructLayout SOCKADDR_IN6;
    public static final UnionLayout SOCKADDR_INET;
    public static final StructLayout IP_ADDRESS_PREFIX;

    public static final StructLayout MIB_IPINTERFACE_ROW;
    public static final StructLayout MIB_IPINTERFACE_TABLE;
    public static final StructLayout MIB_UNICASTIPADDRESS_ROW;
    public static final StructLayout MIB_UNICASTIPADDRESS_TABLE;
    public static final StructLayout MIB_IPFORWARD_ROW2;
    public static final StructLayout MIB_IPFORWARD_TABLE2;

    static {
        arena = Arena.ofAuto();
        linker = Linker.nativeLinker();
        lookup = linker.defaultLookup().or(SymbolLookup.libraryLookup("iphlpapi", arena));
    }

    static {
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

    static {
        SOCKADDR_IN = structLayout(
                JAVA_SHORT.withName("sin_family"),
                JAVA_SHORT.withName("sin_port"),
                JAVA_INT.withName("sin_addr"),
                sequenceLayout(8, JAVA_BYTE).withName("sin_zero")
        );
        SOCKADDR_IN6 = structLayout(
                JAVA_SHORT.withName("sin6_family"),
                JAVA_SHORT.withName("sin6_port"),
                JAVA_INT.withName("sin6_flowinfo"),
                sequenceLayout(16, JAVA_BYTE).withName("sin6_addr"),
                JAVA_INT.withName("sin6_scope_id")
        );
        SOCKADDR_INET = unionLayout(
                SOCKADDR_IN.withName("Ipv4"),
                SOCKADDR_IN6.withName("Ipv6"),
                ADDRESS_FAMILY.withName("si_family")
        );
        IP_ADDRESS_PREFIX = structLayout(
                SOCKADDR_INET.withName("Prefix"),
                UINT8.withName("PrefixLength"),
                paddingLayout(3)
        );
        MIB_IPINTERFACE_ROW = structLayout(
                // Key Structure;
                ADDRESS_FAMILY.withName("Family"),
                paddingLayout(6),
                NET_LUID.withName("InterfaceLuid"),
                NET_IFINDEX.withName("InterfaceIndex"),

                // Fields currently not exposed.
                ULONG.withName("MaxReassemblySize"),
                ULONG64.withName("InterfaceIdentifier"),
                ULONG.withName("MinRouterAdvertisementInterval"),
                ULONG.withName("MaxRouterAdvertisementInterval"),

                // Fileds currently exposed.
                BOOLEAN.withName("AdvertisingEnabled"),
                BOOLEAN.withName("ForwardingEnabled"),
                BOOLEAN.withName("WeakHostSend"),
                BOOLEAN.withName("WeakHostReceive"),
                BOOLEAN.withName("UseAutomaticMetric"),
                BOOLEAN.withName("UseNeighborUnreachabilityDetection"),
                BOOLEAN.withName("ManagedAddressConfigurationSupported"),
                BOOLEAN.withName("OtherStatefulConfigurationSupported"),
                BOOLEAN.withName("AdvertiseDefaultRoute"),
                paddingLayout(3),

                NL_ROUTER_DISCOVERY_BEHAVIOR.withName("RouterDiscoveryBehavior"),
                ULONG.withName("DadTransmits"),// DupAddrDetectTransmits in RFC 4862.
                ULONG.withName("BaseReachableTime"),
                ULONG.withName("RetransmitTime"),
                ULONG.withName("PathMtuDiscoveryTimeout"),// Path MTU discovery timeout (in ms).

                NL_LINK_LOCAL_ADDRESS_BEHAVIOR.withName("LinkLocalAddressBehavior"),
                ULONG.withName("LinkLocalAddressTimeout"),// In ms.
                sequenceLayout(16, ULONG).withName("ZoneIndices"),// Zone part of a SCOPE_ID.
                ULONG.withName("SitePrefixLength"),
                ULONG.withName("Metric"),
                ULONG.withName("NlMtu"),

                // Read Only fields.
                BOOLEAN.withName("Connected"),
                BOOLEAN.withName("SupportsWakeUpPatterns"),
                BOOLEAN.withName("SupportsNeighborDiscovery"),
                BOOLEAN.withName("SupportsRouterDiscovery"),

                ULONG.withName("ReachableTime"),

                NL_INTERFACE_OFFLOAD_ROD.withName("TransmitOffload"),
                NL_INTERFACE_OFFLOAD_ROD.withName("ReceiveOffload"),

                // Disables using default route on the interface. This flag
                // can be used by VPN clients to restrict Split tunnelling.
                BOOLEAN.withName("DisableDefaultRoutes"),
                paddingLayout(1)
        );
        MIB_IPINTERFACE_TABLE = structLayout(
                ULONG.withName("NumEntries"),
                paddingLayout(4),
                sequenceLayout(1, MIB_IPINTERFACE_ROW).withName("Table")
        );
        MIB_UNICASTIPADDRESS_ROW = structLayout(
                // Key Structure.
                SOCKADDR_INET.withName("Address"),
                paddingLayout(4),
                NET_LUID.withName("InterfaceLuid"),
                NET_IFINDEX.withName("InterfaceIndex"),

                // Read-Write Fileds.
                NL_PREFIX_ORIGIN.withName("PrefixOrigin"),
                NL_SUFFIX_ORIGIN.withName("SuffixOrigin"),
                ULONG.withName("ValidLifetime"),
                ULONG.withName("PreferredLifetime"),
                UINT8.withName("OnLinkPrefixLength"),
                BOOLEAN.withName("SkipAsSource"),
                paddingLayout(2),

                // Read-Only Fields.
                NL_DAD_STATE.withName("DadState"),
                SCOPE_ID.withName("ScopeId"),
                LARGE_INTEGER.withName("CreationTimeStamp")
        );
        MIB_UNICASTIPADDRESS_TABLE = structLayout(
                ULONG.withName("NumEntries"),
                paddingLayout(4),
                sequenceLayout(1, MIB_UNICASTIPADDRESS_ROW).withName("Table")
        );
        MIB_IPFORWARD_ROW2 = structLayout(
                // Key Structure.
                NET_LUID.withName("InterfaceLuid"),
                NET_IFINDEX.withName("InterfaceIndex"),
                IP_ADDRESS_PREFIX.withName("DestinationPrefix"),
                SOCKADDR_INET.withName("NextHop"),

                // Read-Write Fields.
                UCHAR.withName("SitePrefixLength"),
                paddingLayout(3),
                ULONG.withName("ValidLifetime"),
                ULONG.withName("PreferredLifetime"),
                ULONG.withName("Metric"),
                NL_ROUTE_PROTOCOL.withName("Protocol"),

                BOOLEAN.withName("Loopback"),
                BOOLEAN.withName("AutoconfigureAddress"),
                BOOLEAN.withName("Publish"),
                BOOLEAN.withName("Immortal"),

                // Read-Only Fields.
                ULONG.withName("Age"),
                NL_ROUTE_ORIGIN.withName("Origin")
        );
        MIB_IPFORWARD_TABLE2 = structLayout(
                ULONG.withName("NumEntries"),
                paddingLayout(4),
                sequenceLayout(1, MIB_IPFORWARD_ROW2).withName("Table")
        );

    }

    public static void main(String[] args) throws Throwable {
        if (false) {
            MemorySegment table = arena.allocateFrom(ADDRESS, MemorySegment.NULL);
            int returnValue = (int) GetIpInterfaceTable.invokeExact(0, table);
            table = table.get(ADDRESS, 0).reinterpret(4);
            int NumOfEntries = table.get(JAVA_INT, 0);
            System.out.println(NumOfEntries);
            table = table.reinterpret(8 + (NumOfEntries * 168L));
            table = table.asSlice(8);
            for (int i = 0; i < NumOfEntries; i++) {
                one.papachi.ifconfig4j.ffm.amd64.windows.MIB_IPINTERFACE_ROW row = new MIB_IPINTERFACE_ROW(table.asSlice(i * 168L, 168));
                System.out.println("InterfaceIndex = " + row.InterfaceIndex());
            }
        }
        if (true) {
            MemorySegment pointer = arena.allocate(MIB_UNICASTIPADDRESS_ROW);
            one.papachi.ifconfig4j.ffm.amd64.windows.MIB_UNICASTIPADDRESS_ROW row = new MIB_UNICASTIPADDRESS_ROW(pointer);
            row.InterfaceIndex(11);
            row.Address().Ipv4().sin_family(2);
            row.Address().Ipv4().sin_addr(InetAddress.ofLiteral("10.0.0.2").getAddress());
            row.PreferredLifetime(Integer.MAX_VALUE);
            row.ValidLifetime(Integer.MAX_VALUE);
            row.OnLinkPrefixLength(16);
            int returnValue = (int) CreateUnicastIpAddressEntry.invokeExact(pointer);
            System.out.println(returnValue);
        }
        {
            MemorySegment table = arena.allocateFrom(ADDRESS, MemorySegment.NULL);
            int returnValue = (int) GetUnicastIpAddressTable.invokeExact(0, table);
            table = table.get(ADDRESS, 0).reinterpret(4);
            int NumOfEntries = table.get(JAVA_INT, 0);
            table = table.reinterpret(8 + (NumOfEntries * 80L));
            table = table.asSlice(8);
            for (int i = 0; i < NumOfEntries; i++) {
                one.papachi.ifconfig4j.ffm.amd64.windows.MIB_UNICASTIPADDRESS_ROW row = new MIB_UNICASTIPADDRESS_ROW(table.asSlice(i * 80L, 80));
                if (row.InterfaceIndex() != 11)
                    continue;
                int sin_family = row.Address().Ipv4().sin_family();
                InetAddress inetAddress = InetAddress.getByAddress(sin_family == 2 ? row.Address().Ipv4().sin_addr() : row.Address().Ipv6().sin6_addr());
                System.out.println("InterfaceIndex = " + row.InterfaceIndex() + ": InetAddress = " + inetAddress.getHostAddress() + "/" + row.OnLinkPrefixLength());
                if (inetAddress.getHostAddress().startsWith("169.254.")) {
//                    returnValue = (int) DeleteUnicastIpAddressEntry.invokeExact(table.asSlice(i * 80L, 80));
//                    System.out.println(returnValue);
                }
            }
        }
        // HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters\Interfaces\<guid>
        // IPAutoconfigurationEnabled 0/1 (DWORD) EnableDHCP 0/1 (DWORD) DisableDhcpOnConnect 0/1 (DWORD)
        System.out.println();
        if (false) {
            MemorySegment table = arena.allocateFrom(ADDRESS, MemorySegment.NULL);
            int returnValue = (int) GetIpForwardTable2.invokeExact(0, table);
            table = table.get(ADDRESS, 0).reinterpret(4);
            int NumOfEntries = table.get(JAVA_INT, 0);
            table = table.reinterpret(8 + (NumOfEntries * 104L));
            table = table.asSlice(8);
            for (int i = 0; i < NumOfEntries; i++) {
                one.papachi.ifconfig4j.ffm.amd64.windows.MIB_IPFORWARD_ROW2 row = new MIB_IPFORWARD_ROW2(table.asSlice(i * 104L, 104));
                int sin_family = row.DestinationPrefix().Prefix().Ipv4().sin_family();
                InetAddress destination = InetAddress.getByAddress(sin_family == 2 ? row.DestinationPrefix().Prefix().Ipv4().sin_addr() : row.DestinationPrefix().Prefix().Ipv6().sin6_addr());
                InetAddress nextHop = InetAddress.getByAddress(sin_family == 2 ? row.NextHop().Ipv4().sin_addr() : row.NextHop().Ipv6().sin6_addr());
                System.out.println("Route = " + destination.getHostAddress() + "/" + row.DestinationPrefix().PrefixLength() + " -> " + nextHop.getHostAddress() + " on InterfaceIndex = " + row.InterfaceIndex());
            }
        }
    }

}




