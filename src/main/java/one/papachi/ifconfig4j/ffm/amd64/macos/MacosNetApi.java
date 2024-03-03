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
        addRoute(InetAddress.ofLiteral("10.0.0.2"), InetAddress.ofLiteral("255.255.0.0"), InetAddress.ofLiteral("192.168.68.68"));
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

    public static final int RTA_DST = 0x1;     /* destination sockaddr present */
    public static final int RTA_GATEWAY = 0x2;    /* gateway sockaddr present */
    public static final int RTA_NETMASK = 0x4;    /* netmask sockaddr present */
    public static final int RTA_GENMASK = 0x8;    /* cloning mask sockaddr present */
    public static final int RTA_IFP = 0x10;   /* interface name sockaddr present */
    public static final int RTA_IFA = 0x20;  /* interface addr sockaddr present */
    public static final int RTA_AUTHOR = 0x40; /* sockaddr for author of redirect */
    public static final int RTA_BRD = 0x80; /* for NEWADDR, broadcast or p-p dest addr */


    static void getRoutes() throws Throwable {
//#define RTM_ADD	       0x1    /* Add Route */
//#define RTM_DELETE       0x2    /* Delete	Route */
//#define RTM_CHANGE       0x3    /* Change	Metrics, Flags,	or Gateway */
//#define RTM_GET	       0x4    /* Report	Information */
        // sysctl(mib, 6, NULL, &needed, NULL, 0)
        // buf = (char *)malloc(needed)
        // sysctl(mib, 6, buf, &needed, NULL, 0)

        // s = socket(PF_ROUTE, SOCK_RAW, 0);

        MemorySegment mib = arena.allocate(JAVA_INT, 6);
        mib.set(JAVA_INT, 0, CTL_NET);
        mib.set(JAVA_INT, 4, PF_ROUTE);
        mib.set(JAVA_INT, 8, 0);
        mib.set(JAVA_INT, 12, 0);
        mib.set(JAVA_INT, 16, NET_RT_DUMP);
        mib.set(JAVA_INT, 20, 0);

        MemorySegment needed = arena.allocateFrom(JAVA_INT, 6);

        int returnValue = (int) sysctl.invokeExact(mib, 6, MemorySegment.NULL, needed, MemorySegment.NULL, 0);

        MemorySegment buf = arena.allocate(needed.get(JAVA_INT, 0));
        returnValue = (int) sysctl.invokeExact(mib, 6, buf, needed, MemorySegment.NULL, 0);

        int rtm_msglen;

        for (MemorySegment rtm = buf; rtm.address() < buf.address() + needed.get(JAVA_INT, 0); rtm = rtm.asSlice(rtm_msglen)) {
            rtm_msglen = rtm.get(JAVA_SHORT, 0) & 0xFFFF;
            int rtm_version = rtm.get(JAVA_BYTE, 2) & 0xFF;
            int rtm_type = rtm.get(JAVA_BYTE, 3) & 0xFF;
            int rtm_index = rtm.get(JAVA_SHORT, 4) & 0xFFFF;
            int rtm_flags = rtm.get(JAVA_INT, 8);
            int rtm_addrs = rtm.get(JAVA_INT, 12);
            int rtm_pid = rtm.get(JAVA_INT, 16);
            int rtm_seq = rtm.get(JAVA_INT, 20);
            int rtm_errno = rtm.get(JAVA_INT, 24);
            int rtm_use = rtm.get(JAVA_INT, 28);
            int rtm_inits = rtm.get(JAVA_INT, 32);
            int rtm_rmx_locks = rtm.get(JAVA_INT, 36);
            int rtm_rmx_mtu = rtm.get(JAVA_INT, 40);
            int rtm_rmx_hopcount = rtm.get(JAVA_INT, 44);
            int rtm_rmx_expire = rtm.get(JAVA_INT, 48);
            int rtm_rmx_recvpipe = rtm.get(JAVA_INT, 52);
            int rtm_rmx_sendpipe = rtm.get(JAVA_INT, 56);
            int rtm_rmx_ssthresh = rtm.get(JAVA_INT, 60);
            int rtm_rmx_rtt = rtm.get(JAVA_INT, 64);
            int rtm_rmx_rttvar = rtm.get(JAVA_INT, 68);
            int rtm_rmx_pksent = rtm.get(JAVA_INT, 72);
            int rtm_rmx_filler0 = rtm.get(JAVA_INT, 76);
            int rtm_rmx_filler1 = rtm.get(JAVA_INT, 80);
            int rtm_rmx_filler2 = rtm.get(JAVA_INT, 84);
            int rtm_rmx_filler3 = rtm.get(JAVA_INT, 88);

            if ((rtm_flags & 0x2) == 0 || (rtm_flags & 0x4) != 0)
                continue;

            int len;

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

    static void addRoute(InetAddress dst, InetAddress mask, InetAddress gateway) throws Throwable {

        MemorySegment msg = arena.allocate(92 + 16 + 16/* + 16*/);
        msg.set(JAVA_SHORT, 0, (short) (92 + 16 + 16/* + 16*/));
        msg.set(JAVA_BYTE, 2, (byte) 5);
        msg.set(JAVA_BYTE, 3, (byte) 1);
        msg.set(JAVA_SHORT, 4, (short) 4);
        msg.set(JAVA_INT, 8, 0x1 | 0x2 | 0x4 | 0x800);
        msg.set(JAVA_INT, 12, 0x1 | 0x2/* | 0x4*/);
        msg.set(JAVA_INT, 16, (int) ProcessHandle.current().pid());

        MemorySegment pDst = msg.asSlice(92);
        pDst.set(JAVA_BYTE, 0, (byte) 16);
        pDst.set(JAVA_BYTE, 0, (byte) 2);
        pDst.asByteBuffer().put(4, dst.getAddress());
//        MemorySegment pMask = msg.asSlice(92 + 16 + 16);
//        pMask.set(JAVA_BYTE, 0, (byte) 16);
//        pMask.set(JAVA_BYTE, 0, (byte) 2);
//        pMask.asByteBuffer().put(4, mask.getAddress());
        MemorySegment pGateway = msg.asSlice(92 + 16);
        pGateway.set(JAVA_BYTE, 0, (byte) 16);
        pGateway.set(JAVA_BYTE, 0, (byte) 2);
        pGateway.asByteBuffer().put(4, gateway.getAddress());


        // s = socket(PF_ROUTE, SOCK_RAW, 0);

        int handle = (int) socket.invokeExact(PF_ROUTE, 3, 0);
        System.out.println(handle);
        int written = (int) write.invokeExact(handle, msg, (int) msg.byteSize());
        System.out.println(written);
        getAndThrowException("write");
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
