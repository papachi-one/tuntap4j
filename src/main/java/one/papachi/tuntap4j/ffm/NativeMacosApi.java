package one.papachi.tuntap4j.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

public class NativeMacosApi {

    protected Arena arena;
    protected Linker linker;
    protected SymbolLookup lookup;
    protected MethodHandle error;
    protected MethodHandle strerror;
    protected MethodHandle fcntl;
    protected MethodHandle open;
    protected MethodHandle socket;
    protected MethodHandle ioctl;
    protected MethodHandle connect;
    protected MethodHandle getsockopt;
    protected MethodHandle read;
    protected MethodHandle write;
    protected MethodHandle close;
    protected MethodHandle sysctl;
    protected StructLayout in_addr;
    protected StructLayout ifr_addr;
    protected StructLayout ifreq;

    NativeMacosApi() {
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

    public static void main(String[] args) {
        NativeMacosApi api = new NativeMacosApi();
    }

}
