package one.papachi.ifconfig4j.ffm.amd64.macos;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.foreign.ValueLayout.*;

public class MacosNetApi {

    public static final int AF_INET = 2;
    public static final int AF_LINK = 18;
    public static final int AF_INET6 = 30;

    public static final int SOCK_DGRAM = 2;

    public static final int SIOCAIFADDR = (int) -2143262438L;
    public static final int SIOCDIFADDR = (int) 2149607705L;

    public static final int SIOCAIFADDR_IN6 = (int) 2155899162L;
    public static final int SIOCDIFADDR_IN6 = (int) 2166384921L;

    public static final Arena arena;
    public static final Linker linker;
    public static final SymbolLookup lookup;

    public static final MethodHandle error;
    public static final MethodHandle strerror;
    public static final MethodHandle getifaddrs;
    public static final MethodHandle freeifaddrs;
    public static final MethodHandle socket;
    public static final MethodHandle read;
    public static final MethodHandle write;
    public static final MethodHandle ioctl;
    public static final MethodHandle close;
    public static final MethodHandle sysctl;
    public static final MethodHandle if_indextoname;

    static {
        arena = Arena.ofAuto();
        linker = Linker.nativeLinker();
        lookup = linker.defaultLookup();
    }

    static {
        error = linker.downcallHandle(lookup.find("__error").orElseThrow(),
                FunctionDescriptor.of(
                        ADDRESS.withName("return")
                ));
        strerror = linker.downcallHandle(lookup.find("strerror").orElseThrow(),
                FunctionDescriptor.of(
                        ADDRESS.withName("return"),
                        JAVA_INT.withName("errnum")
                ));
        getifaddrs = linker.downcallHandle(lookup.find("getifaddrs").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("ifap")
                ));
        freeifaddrs = linker.downcallHandle(lookup.find("freeifaddrs").orElseThrow(),
                FunctionDescriptor.ofVoid(ADDRESS));
        socket = linker.downcallHandle(lookup.find("socket").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("domain"),
                        JAVA_INT.withName("type"),
                        JAVA_INT.withName("protocol")
                ));
        read = linker.downcallHandle(lookup.find("read").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT));
        write = linker.downcallHandle(lookup.find("write").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT));
        ioctl = linker.downcallHandle(lookup.find("ioctl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd"),
                        JAVA_INT.withName("request"),
                        ADDRESS.withName("param"),
                        JAVA_INT.withName("paramLen")
                ));
        close = linker.downcallHandle(lookup.find("close").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd")
                ));
        sysctl = linker.downcallHandle(lookup.find("sysctl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("name"),
                        JAVA_INT.withName("namelen"),
                        ADDRESS.withName("oldp"),
                        ADDRESS.withName("oldlenp"),
                        ADDRESS.withName("newp"),
                        JAVA_INT.withName("newlen")
                ));
        if_indextoname = linker.downcallHandle(lookup.find("if_indextoname").orElseThrow(),
                FunctionDescriptor.of(ADDRESS, JAVA_INT, ADDRESS));
    }

    public static void main(String[] args) throws Throwable {
//        getInetAddresses();
//        System.out.println("--");
//        getRoutes();
        addRoute(InetAddress.ofLiteral("10.0.0.1"), InetAddress.ofLiteral("255.255.255.0"), InetAddress.ofLiteral("192.168.68.1"));
    }

    static byte[] getAddr(MemorySegment sockAddr) {
        return getAddr(sockAddr, 0);
    }

    static byte[] getAddr(MemorySegment sockAddr, int default_sa_family) {
        if (sockAddr.address() == 0L)
            return null;
        int len = sockAddr.reinterpret(1).get(JAVA_BYTE, 0) & 0xFF;
        sockAddr = sockAddr.reinterpret(len);
        if (len < 2)
            return null;
        int sa_family = sockAddr.get(JAVA_BYTE, 1) & 0xFF;
        sa_family = sa_family == 255 ? default_sa_family : sa_family;
        ByteBuffer buffer = sockAddr.asByteBuffer();
        byte[] addr = null;
        switch (sa_family) {
            case AF_INET -> buffer.get(4, addr = new byte[4], 0, Math.max(Math.min(4, len - 4), 0));
            case AF_INET6 -> buffer.get(8, addr = new byte[16], 0, Math.max(Math.min(16, len - 8), 0));
        }
        return addr;
    }

    public static Map<String, List<InetAddress>> getInetAddresses() throws Throwable {
        MemorySegment pointer = arena.allocate(ADDRESS);
        int returnValue = (int) getifaddrs.invokeExact(pointer);
        // TODO handle returnValue and get error number with message
        pointer = pointer.get(ADDRESS, 0);
        for (MemorySegment next = pointer.reinterpret(56);
             next.address() != 0L;
             next = next.get(ADDRESS, 0).reinterpret(56)) {
            MemorySegment ifa_name = next.get(ADDRESS, 8).reinterpret(16);
            int ifa_flags = next.get(JAVA_INT, 16);
            MemorySegment ifa_addr = next.get(ADDRESS, 24);
            MemorySegment ifa_netmask = next.get(ADDRESS, 32);
            MemorySegment ifa_dstaddr = next.get(ADDRESS, 48);
            byte[] addr = getAddr(ifa_addr);
            byte[] netmask = getAddr(ifa_netmask);
            byte[] dstaddr = getAddr(ifa_dstaddr);

            if (addr == null)
                continue;

            System.out.println();
            System.out.println(ifa_name.getString(0));
            if (addr != null)
                System.out.println(InetAddress.getByAddress(addr));
            if (netmask != null)
                System.out.println(InetAddress.getByAddress(netmask));
            if (dstaddr != null)
                System.out.println(InetAddress.getByAddress(dstaddr));
        }
        freeifaddrs.invokeExact(pointer);
        return Collections.emptyMap();
    }

    static void addInetAddress() {
    }

    static void deleteInetAddress() {
    }

    public static final int CTL_NET = 4;
    public static final int PF_ROUTE = 17;
    public static final int NET_RT_DUMP = 1;

    public static final int RTM_ADD = 0x1;    /* Add Route */
    public static final int RTM_DELETE = 0x2;    /* Delete Route */
    public static final int RTM_CHANGE = 0x3;/* Change Metrics or flags */
    public static final int RTM_GET = 0x4;    /* Report Metrics */
    public static final int RTM_LOSING = 0x5;    /* Kernel Suspects Partitioning */
    public static final int RTM_REDIRECT = 0x6;    /* Told to use different route */
    public static final int RTM_MISS = 0x7;/* Lookup failed on this address */
    public static final int RTM_LOCK = 0x8;    /* fix specified metrics */
    public static final int RTM_OLDADD = 0x9;/* caused by SIOCADDRT */
    public static final int RTM_OLDDEL = 0xa;    /* caused by SIOCDELRT */
    public static final int RTM_RESOLVE = 0xb;/* req to resolve dst to LL addr */
    public static final int RTM_NEWADDR = 0xc;/* address being added to iface */
    public static final int RTM_DELADDR = 0xd;    /* address being removed from iface */
    public static final int RTM_IFINFO = 0xe;    /* iface going up/down etc. */
    public static final int RTM_NEWMADDR = 0xf;/* mcast group membership being added to if */
    public static final int RTM_DELMADDR = 0x10;/* mcast group membership being deleted */

    public static final int RTF_UP = 0x1;    /* route usable */
    public static final int RTF_GATEWAY = 0x2;        /* destination is a gateway */
    public static final int RTF_HOST = 0x4;    /* host entry (net otherwise) */
    public static final int RTF_REJECT = 0x8;    /* host or net unreachable */
    public static final int RTF_DYNAMIC = 0x10;    /* created dynamically (by redirect) */
    public static final int RTF_MODIFIED = 0x20;        /* modified dynamically (by redirect) */
    public static final int RTF_DONE = 0x40;    /* message confirmed */
    public static final int RTF_CLONING = 0x100;        /* generate new routes on use */
    public static final int RTF_XRESOLVE = 0x200;    /* external daemon resolves name */
    public static final int RTF_LLINFO = 0x400;    /* generated by link layer (e.g. ARP) */
    public static final int RTF_STATIC = 0x800;    /* manually added */
    public static final int RTF_BLACKHOLE = 0x1000;    /* just discard pkts (during updates) */
    public static final int RTF_PROTO2 = 0x4000;    /* protocol specific routing flag */
    public static final int RTF_PROTO1 = 0x8000;/* protocol specific routing flag */
    public static final int RTF_PRCLONING = 0x10000;    /* protocol requires cloning */
    public static final int RTF_WASCLONED = 0x20000;    /* route generated through cloning */
    public static final int RTF_PROTO3 = 0x40000;    /* protocol specific routing flag */
    public static final int RTF_PINNED = 0x100000;    /* future use */
    public static final int RTF_LOCAL = 0x200000;    /* route represents a local address */
    public static final int RTF_BROADCAST = 0x400000;    /* route represents a bcast address */
    public static final int RTF_MULTICAST = 0x800000;    /* route represents a mcast address */
    public static final int RTF_IFSCOPE = 0x1000000;

    public static final int RTA_DST = 0x1;/* destination sockaddr present */
    public static final int RTA_GATEWAY = 0x2;    /* gateway sockaddr present */
    public static final int RTA_NETMASK = 0x4;/* netmask sockaddr present */
    public static final int RTA_GENMASK = 0x8;    /* cloning mask sockaddr present */
    public static final int RTA_IFP = 0x10;    /* interface name sockaddr present */
    public static final int RTA_IFA = 0x20;    /* interface addr sockaddr present */
    public static final int RTA_AUTHOR = 0x40;    /* sockaddr for author of redirect */
    public static final int RTA_BRD = 0x80;    /* for NEWADDR, broadcast or p-p dest addr */

    static void getRoutes() throws Throwable {
//#define RTM_ADD	       0x1    /* Add Route */
//#define RTM_DELETE       0x2    /* Delete	Route */
//#define RTM_CHANGE       0x3    /* Change	Metrics, Flags,	or Gateway */
//#define RTM_GET	       0x4    /* Report	Information */
        // sysctl(mib, 6, NULL, &needed, NULL, 0)
        // buf = (char *)malloc(needed)
        // sysctl(mib, 6, buf, &needed, NULL, 0)

        // s = socket(PF_ROUTE, SOCK_RAW, 0);



        MemorySegment pMib = arena.allocate(JAVA_INT, 6);
        MIB6 mib = new MIB6(pMib);
        mib.int0(CTL_NET);
        mib.int1(PF_ROUTE);
        mib.int4(NET_RT_DUMP);

        MemorySegment needed = arena.allocateFrom(JAVA_INT, 6);

        int returnValue = (int) sysctl.invokeExact(pMib, 6, MemorySegment.NULL, needed, MemorySegment.NULL, 0);

        MemorySegment buf = arena.allocate(needed.get(JAVA_INT, 0));
        returnValue = (int) sysctl.invokeExact(mib, 6, buf, needed, MemorySegment.NULL, 0);

        int rtm_msglen;

        for (MemorySegment rtm = buf; rtm.address() < buf.address() + needed.get(JAVA_INT, 0); rtm = rtm.asSlice(rtm_msglen)) {
            RT_MSGHDR RTM = new RT_MSGHDR(rtm);
            rtm_msglen = RTM.rtm_msglen();
            int rtm_index = RTM.rtm_index();
            int rtm_flags = RTM.rtm_flags();
            int rtm_addrs = RTM.rtm_addrs();

            if ((rtm_flags & 0x2) == 0 || (rtm_flags & 0x4) != 0)
                continue;

            MemorySegment rtm_msg = rtm.asSlice(92);
            int offset = 0;
            int length = 0;
            int sa_family = 0;
            MemorySegment if_name = arena.allocate(16);
            MemorySegment retVal = (MemorySegment) if_indextoname.invokeExact(rtm_index, if_name);
            System.out.println();
            System.out.print(rtm_index);
            System.out.println(retVal.address() != 0 ? if_name.getString(0) : "");
            if ((rtm_addrs & RTA_DST) != 0) {
                MemorySegment rta_dst = rtm_msg.asSlice(offset);
                length = rta_dst.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = rta_dst.get(JAVA_BYTE, 1) & 0xFF;
                byte[] addr = getAddr(rta_dst, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("DST: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_GATEWAY) != 0) {
                MemorySegment rta_gateway = rtm_msg.asSlice(offset);
                length = rta_gateway.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_gateway.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_gateway, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("GATEWAY: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_NETMASK) != 0) {
                MemorySegment rta_netmask = rtm_msg.asSlice(offset);
                length = rta_netmask.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_netmask.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_netmask, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("NETMASK: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_GENMASK) != 0) {
                MemorySegment rta_genmask = rtm_msg.asSlice(offset);
                length = rta_genmask.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_genmask.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_genmask, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("GENMASK: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_IFP) != 0) {
                MemorySegment rta_ifp = rtm_msg.asSlice(offset);
                length = rta_ifp.get(JAVA_BYTE, 0) & 0xFF;
                System.out.println(rta_ifp.get(JAVA_BYTE, 1) & 0xFF);
                byte[] addr = getAddr(rta_ifp);
                offset += length;
                System.out.println("IFP: ?");
            }
            if ((rtm_addrs & RTA_IFA) != 0) {
                MemorySegment rta_ifa = rtm_msg.asSlice(offset);
                length = rta_ifa.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_ifa.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_ifa, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("IFA: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_AUTHOR) != 0) {
                MemorySegment rta_author = rtm_msg.asSlice(offset);
                length = rta_author.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_author.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_author, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("AUTHOR: " + InetAddress.getByAddress(addr));
            }
            if ((rtm_addrs & RTA_BRD) != 0) {
                MemorySegment rta_brd = rtm_msg.asSlice(offset);
                length = rta_brd.get(JAVA_BYTE, 0) & 0xFF;
                sa_family = sa_family == 0 ? rta_brd.get(JAVA_BYTE, 1) & 0xFF : sa_family;
                byte[] addr = getAddr(rta_brd, sa_family);
                offset += length;
                if (addr != null)
                    System.out.println("BRD: " + InetAddress.getByAddress(addr));
            }
        }

    }


    static void addRoute(InetAddress dst, InetAddress netmask, InetAddress gateway) throws Throwable {
        dst = InetAddress.ofLiteral("172.17.0.0");
        gateway = InetAddress.ofLiteral("192.168.68.54");
        netmask = InetAddress.ofLiteral("255.255.255.0");

        MemorySegment p = arena.allocate(92 + 16 + 16 + 16);
        RT_MSGHDR msg = new RT_MSGHDR(p);
        msg.rtm_msglen((int) p.byteSize());
        msg.rtm_version(5);
        msg.rtm_type(RTM_ADD);
        msg.rtm_flags(RTF_UP | RTF_GATEWAY | RTF_STATIC);
        msg.rtm_addrs(RTA_DST | RTA_GATEWAY | RTA_NETMASK);
        msg.rtm_seq(1);

        MemorySegment pDst = p.asSlice(92);
        pDst.set(JAVA_BYTE, 0, (byte) 16);
        pDst.set(JAVA_BYTE, 1, (byte) 2);
        pDst.asByteBuffer().put(4, dst.getAddress());
        MemorySegment pGateway = p.asSlice(92 + 16);
        pGateway.set(JAVA_BYTE, 0, (byte) 16);
        pGateway.set(JAVA_BYTE, 1, (byte) 2);
        pGateway.asByteBuffer().put(4, gateway.getAddress());
        MemorySegment pNetmask = p.asSlice(92 + 16 + 16);
        pNetmask.set(JAVA_BYTE, 0, (byte) 16);
        pNetmask.set(JAVA_BYTE, 1, (byte) 2);
        pNetmask.asByteBuffer().put(4, netmask.getAddress());

        int handle = (int) socket.invokeExact(PF_ROUTE, 3, 0);
        System.out.println(handle);

        int written = (int) write.invokeExact(handle, msg, (int) p.byteSize());
        System.out.println(written);
        getAndThrowException("write");

//        MemorySegment p = arena.allocate(1500);
//        int _read = (int) read.invokeExact(handle, p, 1500);
//        System.out.println(_read);
//
//        byte[] data = new byte[132];
//        p.asByteBuffer().get(data);
//        Files.write(Path.of("/Users/pc/Projects/tuntap4j/data.out"), data, StandardOpenOption.CREATE_NEW);


//        getAndThrowException("write");
    }

    public static void getAndThrowException(String function) throws Throwable {
        MemorySegment p_error = (MemorySegment) error.invokeExact();
        p_error = p_error.reinterpret(4);
        int error = p_error.get(ValueLayout.JAVA_INT, 0);
        MemorySegment sError = (MemorySegment) strerror.invokeExact(error);
        sError = sError.reinterpret(1000);
        String string = sError.getString(0);
        throw new IOException("Failed to call " + function + ": " + error + " " + string);
    }

    static void deleteRoute() {
    }

    static long round(long i, MemoryLayout layout) {
        return i > 0 ? 1L + ((i - 1L) | (layout.byteSize() - 1L)) : layout.byteSize();
    }

}
