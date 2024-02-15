package one.papachi.tuntap4j.ffm;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class AbstractMacosNetworkDevice {

    protected static final int F_GETFD = 1;
    protected static final int PF_SYSTEM = 32;
    protected static final int SOCK_DGRAM = 2;
    protected static final int SYSPROTO_CONTROL = 2;
    protected static final String UTUN_CONTROL_NAME = "com.apple.net.utun_control";
    protected static final int CTLIOCGINFO = (int) 3227799043L;
    protected static final int AF_SYSTEM = 32;
    protected static final int AF_INET = 2;
    protected static final int AF_SYS_CONTROL = 2;
    protected static final int UTUN_OPT_IFNAME = 2;
    protected static final int SIOCGIFFLAGS = (int) 3223349521L;
    protected static final int SIOCSIFFLAGS = (int) 2149607696L;
    protected static final int IFF_UP = 1;
    protected static final int SIOCGIFADDR = (int) 3223349537L;
    protected static final int SIOCSIFADDR = (int) 2149607692L;
    protected static final int SIOCGIFDSTADDR = (int) 3223349538L;
    protected static final int SIOCSIFDSTADDR = (int) 2149607694L;
    protected static final int SIOCGIFNETMASK = (int) 3223349541L;
    protected static final int SIOCSIFNETMASK = (int) 2149607702L;
    protected static final int SIOCGIFMTU = (int) 3223349555L;
    protected static final int SIOCSIFMTU = (int) 2149607732L;
    protected static final int O_RDWR = 2;


    protected NativeMacosApi api;

    protected String deviceName;

    protected int handle;

    public boolean isOpen() throws IOException {
        try {
            return (int) api.fcntl.invokeExact(handle, F_GETFD) != -1;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int read(ByteBuffer dst) throws IOException {
        try {
            MemorySegment memorySegment = MemorySegment.ofBuffer(dst);
            int read = (int) api.read.invokeExact(handle, memorySegment, dst.remaining());
            dst.position(dst.position() + read);
            return read;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int write(ByteBuffer src) throws IOException {
        try {
            MemorySegment memorySegment = MemorySegment.ofBuffer(src);
            int written = (int) api.read.invokeExact(handle, memorySegment, src.remaining());
            src.position(src.position() + written);
            return written;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void close() throws IOException {
        // TODO
    }

    public boolean isUp() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) < 0)
                getAndThrowException("ioctl");
            return (ifr.get(ValueLayout.JAVA_SHORT, 16) & IFF_UP) != 0;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void setUp(boolean isUp) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) < 0)
                getAndThrowException("ioctl");
            short flags = ifr.get(ValueLayout.JAVA_SHORT, 16);
            flags = (short) (isUp ? flags | IFF_UP : flags & ~IFF_UP);
            ifr.set(ValueLayout.JAVA_SHORT, 16, flags);
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFFLAGS, ifr) < 0)
                getAndThrowException("ioctl");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    protected Inet4Address getAddress(int ioctlOperation) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            ifr.set(ValueLayout.JAVA_BYTE, 17, (byte) 2);
            if ((int) api.ioctl.invokeExact(handle, ioctlOperation, ifr) < 0)
                getAndThrowException("ioctl");
            int address = ifr.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20);
            return (Inet4Address) InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(address).array());
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    protected void setAddress(Inet4Address address, int ioctlOperation) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            ifr.set(ValueLayout.JAVA_BYTE, 17, (byte) 2);
            ifr.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20, ByteBuffer.wrap(address.getAddress()).getInt());
            if ((int) api.ioctl.invokeExact(handle, ioctlOperation, ifr) < 0)
                getAndThrowException("ioctl");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    void set(Inet4Address address, Inet4Address mask) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            ifr.set(ValueLayout.JAVA_BYTE, 17, (byte) 2);
            ifr.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20, ByteBuffer.wrap(address.getAddress()).getInt());
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFADDR, ifr) < 0)
                getAndThrowException("ioctl");
            ifr.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20, ByteBuffer.wrap(mask.getAddress()).getInt());
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFNETMASK, ifr) < 0)
                getAndThrowException("ioctl");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int getMtu() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFMTU, ifr) < 0)
                getAndThrowException("ioctl");
            return ifr.get(ValueLayout.JAVA_INT, 16);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void setMtu(int mtu) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            ifr.set(ValueLayout.JAVA_INT, 16, mtu);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                getAndThrowException("socket");
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFMTU, ifr) < 0)
                getAndThrowException("ioctl");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }


    protected void getAndThrowException(String function) throws Throwable {
        MemorySegment p_error = (MemorySegment) api.error.invokeExact();
        p_error = p_error.reinterpret(4);
        int error = p_error.get(ValueLayout.JAVA_INT, 0);
        MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
        strerror = strerror.reinterpret(1000);
        String string = strerror.getString(0);
        throw new IOException("Failed to call " + function + ": " + error + " " + string);
    }


}
