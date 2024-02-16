package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.NicAddress;
import one.papachi.tuntap4j.api.TapDevice;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MacosExtTapDevice extends AbstractMacosNetworkDevice implements TapDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "tap1";
        NativeMacosApi api = new NativeMacosApi();
        MacosExtTapDevice tap = new MacosExtTapDevice(api, deviceName);
        tap.open();
        tap.addNetworkAddress(new NicAddress(Inet4Address.ofLiteral("10.0.0.1"), 16));
        tap.addNetworkAddress(new NicAddress(Inet4Address.ofLiteral("10.1.0.1"), 16));
        tap.addNetworkAddress(new NicAddress(Inet4Address.ofLiteral("10.2.0.1"), 16));
        tap.addNetworkAddress(new NicAddress(Inet6Address.ofLiteral("2001:da8:ecd1::1"), 64));
        tap.addNetworkAddress(new NicAddress(Inet6Address.ofLiteral("2001:da8:ecd1::2"), 64));
        tap.addNetworkAddress(new NicAddress(Inet6Address.ofLiteral("2001:da8:ecd1::3"), 64));
        tap.setUp(true);
        ByteBuffer dst = ByteBuffer.allocateDirect(1514);
        while (tap.read(dst.clear()) != -1) {
            dst.flip();
            System.out.println(dst);
        }
    }

    public MacosExtTapDevice(NativeMacosApi api, String deviceName) {
        this.api = api;
        this.deviceName = deviceName;
    }

    @Override
    public void open() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            String devicePath = "/dev/" + deviceName;
            MemorySegment pDevicePath = arena.allocateFrom(devicePath);
            if ((handle = (int) api.open.invokeExact(pDevicePath, O_RDWR)) < 0)
                getAndThrowException("open");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public List<NicAddress> getNetworkAddresses() {
        return List.of();
    }

    @Override
    public void addNetworkAddress(NicAddress nicAddress) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            InetAddress inetAddress = nicAddress.ipAddress();
            byte[] address = nicAddress.ipAddress().getAddress();
            if (inetAddress instanceof Inet4Address) {
                byte[] broadcast = nicAddress.broadcast();
                byte[] mask = nicAddress.mask();

                MemorySegment ifaliasreq = arena.allocate(16 + 16 + 16 + 16);
                ifaliasreq.setString(0, deviceName);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 18L, (short) 0);
                ifaliasreq.asByteBuffer().put(20, address);

                ifaliasreq.set(ValueLayout.JAVA_BYTE, 32L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 33L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 34L, (short) 0);
                ifaliasreq.asByteBuffer().put(36, broadcast);

                ifaliasreq.set(ValueLayout.JAVA_BYTE, 48L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 49L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 50L, (short) 0);
                ifaliasreq.asByteBuffer().put(52, mask);

                int handle;
                if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                    getAndThrowException("socket");
                if ((int) api.ioctl1.invokeExact(handle, SIOCAIFADDR, ifaliasreq, (int) ifaliasreq.byteSize()) < 0)
                    getAndThrowException("ioctl");
            } else if (inetAddress instanceof Inet6Address) {
                int prefixLength = nicAddress.prefixLength();
                byte[] mask = nicAddress.mask();

                System.out.println(mask.length);
                System.out.println(address.length);

                MemorySegment in6_aliasreq = arena.allocate(128);
                in6_aliasreq.setString(0, deviceName);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 28);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET6);
                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 18L, (short) 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 20L, 0);
                in6_aliasreq.asByteBuffer().put(24, address);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 40L, 0);

//                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 44L, (byte) 28);
//                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 45L, (byte) AF_INET6);
//                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 46L, (short) 0);
//                in6_aliasreq.set(ValueLayout.JAVA_INT, 48L, 0);
//                in6_aliasreq.asByteBuffer().put(52, address);
//                in6_aliasreq.set(ValueLayout.JAVA_INT, 68L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 72L, (byte) 28);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 73L, (byte) AF_INET6);
                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 74L, (short) 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 76L, 0);
                in6_aliasreq.asByteBuffer().put(80, mask);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 96L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_INT, 100L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_INT, 104L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 108L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 112L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 116L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 120L, 0xFFFFFF);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 124L, 0xFFFFFF);


                int handle;
                if ((handle = (int) api.socket.invokeExact(AF_INET6, SOCK_DGRAM, 0)) < 0)
                    getAndThrowException("socket");
                if ((int) api.ioctl1.invokeExact(handle, (int) 2155899162L, in6_aliasreq, (int) in6_aliasreq.byteSize()) < 0)
                    getAndThrowException("ioctl");

            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

//#define SIOCDIFADDR     _IOW('i', 25, struct ifreq)     /* delete IF addr */
//#define SIOCAIFADDR     _IOW('i', 26, struct ifaliasreq)/* add/chg IF alias */
//#define SIOCGIFMTU      _IOWR('i', 51, struct ifreq)    /* get IF mtu */
//#define SIOCSIFMTU       _IOW('i', 52, struct ifreq)    /* set IF mtu */
//#define SIOCSIFLLADDR   _IOW('i', 60, struct ifreq)     /* set link level addr */
//#define SIOCSIFDSTADDR   _IOW('i', 14, struct ifreq)    /* set p-p address */
//#define SIOCGIFDSTADDR  _IOWR('i', 34, struct ifreq)    /* get p-p address */

//#define SIOCDIFADDR_IN6         _IOW('i', 25, struct in6_ifreq)
//#define SIOCAIFADDR_IN6         _IOW('i', 26, struct in6_aliasreq)
//#define SIOCSIFDSTADDR_IN6      _IOW('i', 14, struct in6_ifreq) ? SIOCSxxx ioctls should be unused (see comments in in6.c)
//#define SIOCGIFDSTADDR_IN6      _IOWR('i', 34, struct in6_ifreq)

//    typedef __uint32_t      in_addr_t;

//    struct in_addr {
//        in_addr_t s_addr;
//    };

//    struct sockaddr_in {
//        __uint8_t       sin_len;
//        sa_family_t     sin_family;
//        in_port_t       sin_port;
//        struct  in_addr sin_addr;
//        char            sin_zero[8];
//    };

//    struct ifaliasreq {
//        char    ifra_name[IFNAMSIZ];            /* if name, e.g. "en0" */
//#if __has_ptrcheck
//        struct  sockaddr_in ifra_addr;
//        struct  sockaddr_in ifra_broadaddr;
//        struct  sockaddr_in ifra_mask;
//#else
//        struct  sockaddr ifra_addr;
//        struct  sockaddr ifra_broadaddr;
//        struct  sockaddr ifra_mask;
//#endif /* __has_ptrcheck */
//    };

//    struct  ifreq {
//        char    ifr_name[IFNAMSIZ];             /* if name, e.g. "en0" */
//        union {
//            struct  sockaddr ifru_addr;
//            struct  sockaddr ifru_dstaddr;
//            struct  sockaddr ifru_broadaddr;
//            short   ifru_flags;
//            int     ifru_metric;
//            int     ifru_mtu;
//            int     ifru_phys;
//            int     ifru_media;
//            int     ifru_intval;
//            caddr_t ifru_data;
//            struct  ifdevmtu ifru_devmtu;
//            struct  ifkpi   ifru_kpi;
//            u_int32_t ifru_wake_flags;
//            u_int32_t ifru_route_refcnt;
//            int     ifru_cap[2];
//            u_int32_t ifru_functional_type;
//#define IFRTYPE_FUNCTIONAL_UNKNOWN              0
//#define IFRTYPE_FUNCTIONAL_LOOPBACK             1
//#define IFRTYPE_FUNCTIONAL_WIRED                2
//#define IFRTYPE_FUNCTIONAL_WIFI_INFRA           3
//#define IFRTYPE_FUNCTIONAL_WIFI_AWDL            4
//#define IFRTYPE_FUNCTIONAL_CELLULAR             5
//#define IFRTYPE_FUNCTIONAL_INTCOPROC            6
//#define IFRTYPE_FUNCTIONAL_COMPANIONLINK        7
//#define IFRTYPE_FUNCTIONAL_MANAGEMENT           8
//#define IFRTYPE_FUNCTIONAL_LAST                 8
//        } ifr_ifru;
//#define ifr_addr        ifr_ifru.ifru_addr      /* address */
//#define ifr_dstaddr     ifr_ifru.ifru_dstaddr   /* other end of p-to-p link */
//#define ifr_broadaddr   ifr_ifru.ifru_broadaddr /* broadcast address */
//#ifdef __APPLE__
//#define ifr_flags       ifr_ifru.ifru_flags     /* flags */
//#else
//#define ifr_flags       ifr_ifru.ifru_flags[0]  /* flags */
//#define ifr_prevflags   ifr_ifru.ifru_flags[1]  /* flags */
//#endif /* __APPLE__ */
//#define ifr_metric      ifr_ifru.ifru_metric    /* metric */
//#define ifr_mtu         ifr_ifru.ifru_mtu       /* mtu */
//#define ifr_phys        ifr_ifru.ifru_phys      /* physical wire */
//#define ifr_media       ifr_ifru.ifru_media     /* physical media */
//#define ifr_data        ifr_ifru.ifru_data      /* for use by interface */
//#define ifr_devmtu      ifr_ifru.ifru_devmtu
//#define ifr_intval      ifr_ifru.ifru_intval    /* integer value */
//#define ifr_kpi         ifr_ifru.ifru_kpi
//#define ifr_wake_flags  ifr_ifru.ifru_wake_flags /* wake capabilities */
//#define ifr_route_refcnt ifr_ifru.ifru_route_refcnt /* route references count */
//#define ifr_reqcap      ifr_ifru.ifru_cap[0]    /* requested capabilities */
//#define ifr_curcap      ifr_ifru.ifru_cap[1]    /* current capabilities */
//    };


//    typedef struct in6_addr {
//        union {
//            __uint8_t   __u6_addr8[16];
//            __uint16_t  __u6_addr16[8];
//            __uint32_t  __u6_addr32[4];
//        } __u6_addr;                    /* 128-bit IP6 address */
//    } in6_addr_t;


//    struct sockaddr_in6 {
//        __uint8_t       sin6_len;       /* length of this struct(sa_family_t) */
//        sa_family_t     sin6_family;    /* AF_INET6 (sa_family_t) */
//        in_port_t       sin6_port;      /* Transport layer port # (in_port_t) */
//        __uint32_t      sin6_flowinfo;  /* IP6 flow information */
//        struct in6_addr sin6_addr;      /* IP6 address */
//        __uint32_t      sin6_scope_id;  /* scope zone index */
//    };

//    struct in6_aliasreq {
//        char    ifra_name[IFNAMSIZ];
//        struct  sockaddr_in6 ifra_addr;
//        struct  sockaddr_in6 ifra_dstaddr;
//        struct  sockaddr_in6 ifra_prefixmask;
//        int     ifra_flags;
//        struct in6_addrlifetime ifra_lifetime;
//    };

//    struct in6_ifreq {
//        char    ifr_name[IFNAMSIZ];
//        union {
//            struct  sockaddr_in6 ifru_addr;
//            struct  sockaddr_in6 ifru_dstaddr;
//            int     ifru_flags;
//            int     ifru_flags6;
//            int     ifru_metric;
//            int     ifru_intval;
//            caddr_t ifru_data;
//            struct in6_addrlifetime ifru_lifetime;
//            struct in6_ifstat ifru_stat;
//            struct icmp6_ifstat ifru_icmp6stat;
//            u_int32_t ifru_scope_id[SCOPE6_ID_MAX];
//        } ifr_ifru;
//    };

    @Override
    public void removeNetworkAddress(NicAddress nicAddress) {

    }

    @Override
    public byte[] getMacAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMacAddress(byte[] macAddress) {
        throw new UnsupportedOperationException();
    }

}
