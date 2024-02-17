package one.papachi.tuntap4j.ffm;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

public class NativeMacosApi {

    public static int prefixLen(InetAddress mask) {
        int prefixLen = 0;
        ByteBuffer buffer = ByteBuffer.wrap(mask.getAddress());
        while (buffer.hasRemaining()) {
            prefixLen += Integer.bitCount(buffer.getInt());
        }
        return prefixLen;
    }

    public static byte[] mask(int prefixLen) {
        byte[] mask = new byte[prefixLen > 32 ? 16 : 4];
        for (int i = 0; i < prefixLen; i++) {
            int bytePosition = i / 8;
            int bitPosition = i % 8;
            mask[bytePosition] |= (byte) (1 << (7 - bitPosition));
        }
        return mask;
    }

    public static byte[] broadcast(byte[] address, byte[] mask) {
        byte[] broadcast = new byte[address.length];
        for (int i = 0; i < broadcast.length; i++) {
            broadcast[i] = (byte) (address[i] | ~mask[i]);
        }
        return broadcast;
    }

    public static final int F_GETFD = 1;

    public static final int AF_INET = 2;
    public static final int AF_INET6 = 30;
    public static final int AF_SYSTEM = 32;

    public static final int SOCK_DGRAM = 2;

    public static final int SYSPROTO_CONTROL = 2;
    public static final String UTUN_CONTROL_NAME = "com.apple.net.utun_control";

    public static final int CTLIOCGINFO = (int) 3227799043L;

    public static final int AF_SYS_CONTROL = 2;

    public static final int UTUN_OPT_IFNAME = 2;

    public static final int IFF_UP = 1;

    public static final int O_RDWR = 2;

    public static final int SIOCGIFFLAGS = (int) 3223349521L;
    public static final int SIOCSIFFLAGS = (int) 2149607696L;

    public static final int SIOCGIFMTU = (int) 3223349555L;
    public static final int SIOCSIFMTU = (int) 2149607732L;

    public static final int SIOCAIFADDR = (int) -2143262438L;
    public static final int SIOCDIFADDR = (int) 2149607705L;

    public static final int SIOCAIFADDR_IN6 = (int) 2155899162L;
    public static final int SIOCDIFADDR_IN6 = (int) 2166384921L;

    public Arena arena;
    public Linker linker;
    public SymbolLookup lookup;
    public MethodHandle error;
    public MethodHandle strerror;
    public MethodHandle fcntl;
    public MethodHandle open;
    public MethodHandle socket;
    public MethodHandle ioctl;
    public MethodHandle ioctl1;
    public MethodHandle connect;
    public MethodHandle getsockopt;
    public MethodHandle read;
    public MethodHandle write;
    public MethodHandle close;
    public MethodHandle sysctl;
    public StructLayout in_addr;
    public StructLayout ifr_addr;
    public StructLayout ifreq;

    public NativeMacosApi() {
        arena = Arena.ofShared();
        linker = Linker.nativeLinker();
        lookup = linker.defaultLookup();

        error = linker.downcallHandle(lookup.find("__error").orElseThrow(),
                FunctionDescriptor.of(
                        ADDRESS.withName("return")
                ));

        strerror = linker.downcallHandle(lookup.find("strerror").orElseThrow(),
                FunctionDescriptor.of(
                        ADDRESS.withName("return"),
                        JAVA_INT.withName("errnum")
                ));

        fcntl = linker.downcallHandle(lookup.find("fcntl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fildes"),
                        JAVA_INT.withName("cmd")
                ));

        open = linker.downcallHandle(lookup.find("open").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("path"),
                        JAVA_INT.withName("flags")
                ));

        socket = linker.downcallHandle(lookup.find("socket").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("domain"),
                        JAVA_INT.withName("type"),
                        JAVA_INT.withName("protocol")
                ));

        ioctl = linker.downcallHandle(lookup.find("ioctl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd"),
                        JAVA_INT.withName("request"),
                        ADDRESS.withName("param")
                ));

        ioctl1 = linker.downcallHandle(lookup.find("ioctl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd"),
                        JAVA_INT.withName("request"),
                        ADDRESS.withName("param"),
                        JAVA_INT.withName("paramLen")
                ));

        connect = linker.downcallHandle(lookup.find("connect").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("sockfd"),
                        ADDRESS.withName("addr"),
                        JAVA_INT.withName("sizeof(addr)")// TODO
                ));

        getsockopt = linker.downcallHandle(lookup.find("getsockopt").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("sockfd"),
                        JAVA_INT.withName("level"),
                        JAVA_INT.withName("optname"),
                        ADDRESS.withName("optval"),
                        ADDRESS.withName("optlen")
                ));

        read = linker.downcallHandle(lookup.find("read").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd"),
                        ADDRESS.withName("buf"),
                        JAVA_INT.withName("count")
                ));

        write = linker.downcallHandle(lookup.find("write").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("fd"),
                        ADDRESS.withName("buf"),
                        JAVA_INT.withName("count")
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

        in_addr = structLayout(
                JAVA_INT.withName("s_addr")
        ).withName("sin_addr");

        ifr_addr = structLayout(
                JAVA_BYTE.withName("sin_len"),
                JAVA_BYTE.withName("sin_family"),
                JAVA_SHORT.withName("sin_port"),
                in_addr,
                sequenceLayout(8, JAVA_BYTE).withName("sin_zero")
        ).withName("ifr_addr");

        ifreq = structLayout(
                sequenceLayout(16, JAVA_BYTE).withName("ifr_name"),
                unionLayout(
                        JAVA_SHORT.withName("ifru_flags"),
                        JAVA_INT.withName("ifru_mtu"),
                        ifr_addr
                ).withName("if_ifru")
        ).withName("ifreq");

    }

    public boolean isOpen(int handle) throws IOException {
        try {
            return (int) this.fcntl.invokeExact(handle, F_GETFD) != -1;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int read(int handle, ByteBuffer dst) throws IOException {
        try {
            MemorySegment memorySegment = MemorySegment.ofBuffer(dst);
            int read = (int) this.read.invokeExact(handle, memorySegment, dst.remaining());
            dst.position(dst.position() + read);
            return read;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int write(int handle, ByteBuffer src) throws IOException {
        try {
            MemorySegment memorySegment = MemorySegment.ofBuffer(src);
            int written = (int) this.read.invokeExact(handle, memorySegment, src.remaining());
            src.position(src.position() + written);
            return written;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void close(int handle) {
        // TODO
    }

    public boolean isUp(Arena arena, String ifName) throws Throwable {
        MemorySegment ifr = getIfr(arena, ifName);
        socketIoctlClose(AF_INET, 0, SIOCGIFFLAGS, ifr);
        return (ifr.get(ValueLayout.JAVA_SHORT, 16) & IFF_UP) != 0;
    }

    public void setUp(Arena arena, String ifName, boolean isUp) throws Throwable {
        MemorySegment ifr = getIfr(arena, ifName);
        socketIoctlClose(AF_INET, 0, SIOCGIFFLAGS, ifr);
        short flags = ifr.get(ValueLayout.JAVA_SHORT, 16);
        flags = (short) (isUp ? flags | IFF_UP : flags & ~IFF_UP);
        ifr.set(ValueLayout.JAVA_SHORT, 16, flags);
        socketIoctlClose(AF_INET, 0, SIOCSIFFLAGS, ifr);
    }

    public int getMtu(Arena arena, String ifName) throws Throwable {
            MemorySegment ifr = getIfr(arena, ifName);
            socketIoctlClose(AF_INET, 0, SIOCGIFMTU, ifr);
            return ifr.get(ValueLayout.JAVA_INT, 16);
    }

    public void setMtu(Arena arena, String ifName, int mtu) throws Throwable {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, ifName);
            ifr.set(ValueLayout.JAVA_INT, 16, mtu);
            socketIoctlClose(AF_INET, 0, SIOCSIFMTU, ifr);
    }

    public MemorySegment getIfr(Arena arena, String ifName) {
        MemorySegment ifr = arena.allocate(32);
        ifr.setString(0, ifName);
        return ifr;
    }

    public void doAddress(Arena arena, String ifName, InetAddress localAddress, InetAddress remoteAddress, int prefixLength, boolean isDelete) throws Throwable {
        byte[] ifAddr = localAddress.getAddress();
        byte[] mask = mask(prefixLength);
        byte[] broadDstAddr = remoteAddress != null ? remoteAddress.getAddress() : (ifAddr.length == 4 ? broadcast(ifAddr, mask) : null);
        MemorySegment data = ifAddr.length == 4 ? getIfAliasReq(arena, ifName, ifAddr, broadDstAddr, mask) : getIn6AliasReq(arena, ifName, ifAddr, broadDstAddr, mask);
        socketIoctlClose(ifAddr.length == 4 ? AF_INET : AF_INET6, 0, ifAddr.length == 4 ?  (isDelete ? SIOCDIFADDR : SIOCAIFADDR) : (isDelete ? SIOCDIFADDR_IN6 : SIOCAIFADDR_IN6), data);
    }

    public MemorySegment getIfAliasReq(Arena arena, String ifName, byte[] ifAddr, byte[] broadAddr, byte[] maskAddr) throws Throwable {
        MemorySegment ifAliasReq = arena.allocate(64);

        ifAliasReq.setString(0, ifName);

        ifAliasReq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 16);
        ifAliasReq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET);
        ifAliasReq.asByteBuffer().put(20, ifAddr);

        ifAliasReq.set(ValueLayout.JAVA_BYTE, 32L, (byte) 16);
        ifAliasReq.set(ValueLayout.JAVA_BYTE, 33L, (byte) AF_INET);
        ifAliasReq.asByteBuffer().put(36, broadAddr);

        ifAliasReq.set(ValueLayout.JAVA_BYTE, 48L, (byte) 16);
        ifAliasReq.set(ValueLayout.JAVA_BYTE, 49L, (byte) AF_INET);
        ifAliasReq.asByteBuffer().put(52, maskAddr);

        return ifAliasReq;
    }

    public MemorySegment getIn6AliasReq(Arena arena, String ifName, byte[] ifAddr, byte[] dstAddr, byte[] maskAddr) throws Throwable {
        MemorySegment in6_aliasreq = arena.allocate(128);

        in6_aliasreq.setString(0, ifName);

        in6_aliasreq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 28);
        in6_aliasreq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET6);
        in6_aliasreq.asByteBuffer().put(24, ifAddr);

        if (dstAddr != null) {
            in6_aliasreq.set(ValueLayout.JAVA_BYTE, 44L, (byte) 28);
            in6_aliasreq.set(ValueLayout.JAVA_BYTE, 45L, (byte) AF_INET6);
            in6_aliasreq.asByteBuffer().put(52, dstAddr);
        }

        in6_aliasreq.set(ValueLayout.JAVA_BYTE, 72L, (byte) 28);
        in6_aliasreq.set(ValueLayout.JAVA_BYTE, 73L, (byte) AF_INET6);
        in6_aliasreq.asByteBuffer().put(80, maskAddr);

        in6_aliasreq.set(ValueLayout.JAVA_INT, 120L, 0xFFFFFF);
        in6_aliasreq.set(ValueLayout.JAVA_INT, 124L, 0xFFFFFF);

        return in6_aliasreq;
    }

    public int socket(int AF, int PROTOCOL) throws Throwable {
        int handle;
        if ((handle = (int) this.socket.invokeExact(AF, SOCK_DGRAM, PROTOCOL)) < 0)
            getAndThrowException("socket");
        return handle;
    }

    public void ioctl(int handle, int command, MemorySegment data) throws Throwable {
        if ((int) this.ioctl1.invokeExact(handle, command, data, (int) data.byteSize()) < 0)
            getAndThrowException("ioctl");
    }

    public void socketIoctlClose(int AF, int PROTOCOL, int command, MemorySegment data) throws Throwable {
        int handle = socket(AF, PROTOCOL);
        ioctl(handle, command, data);
        close(handle);
    }

    public void getAndThrowException(String function) throws Throwable {
        MemorySegment p_error = (MemorySegment) this.error.invokeExact();
        p_error = p_error.reinterpret(4);
        int error = p_error.get(ValueLayout.JAVA_INT, 0);
        MemorySegment strerror = (MemorySegment) this.strerror.invokeExact(error);
        strerror = strerror.reinterpret(1000);
        String string = strerror.getString(0);
        throw new IOException("Failed to call " + function + ": " + error + " " + string);
    }

}
