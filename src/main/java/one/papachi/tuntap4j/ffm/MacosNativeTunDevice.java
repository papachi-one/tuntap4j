package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.TunDevice;

import java.io.IOException;
import java.lang.foreign.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MacosNativeTunDevice implements TunDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "utun5";
        NativeMacosApi api = new NativeMacosApi();
        MacosNativeTunDevice tun = new MacosNativeTunDevice(api, deviceName);
        tun.open();
        System.out.println(tun.isUp());
        Thread.sleep(1000);
        tun.setInetAddress(null);
        tun.getInetAddress();
        ByteBuffer dst = ByteBuffer.allocateDirect(1514);
        while (tun.read(dst.clear()) != -1) {
            dst.flip();
            System.out.println(dst);
        }
    }

    private final NativeMacosApi api;

    private String deviceName;

    private int handle;

    public MacosNativeTunDevice(NativeMacosApi api, String deviceName) {
        this.api = api;
        this.deviceName = deviceName;
    }

    private static final int F_GETFD = 1;
    private static final int PF_SYSTEM = 32;
    private static final int SOCK_DGRAM = 2;
    private static final int SYSPROTO_CONTROL = 2;
    private static final String UTUN_CONTROL_NAME = "com.apple.net.utun_control";
    private static final int CTLIOCGINFO = (int) 3227799043L;
    private static final int AF_SYSTEM = 32;
    private static final int AF_INET = 2;
    private static final int AF_SYS_CONTROL = 2;
    private static final int UTUN_OPT_IFNAME = 2;
    private static final int SIOCGIFFLAGS = (int) 3223349521L;
    private static final int SIOCSIFFLAGS = (int) 2149607696L;
    private static final short IFF_UP = 1;
    private static final int SIOCGIFADDR = (int) 3223349537L;
    private static final int SIOCSIFADDR = (int) 2149607692L;
    private static final int SIOCGIFNETMASK = (int) 3223349541L;
    private static final int SIOCSIFNETMASK = (int) 2149607702L;
    private static final int SIOCGIFMTU = (int) 3223349555L;
    private static final int SIOCSIFMTU = (int) 2149607732L;

    @Override
    public boolean isOpen() throws IOException {
        try {
            return (int) api.fcntl.invokeExact(handle, F_GETFD) != -1;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void open() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            if ((handle = (int) api.socket.invokeExact(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL)) == -1) {
                getAndThrowException("socket");
            }
            MemorySegment info = arena.allocate(100);
            info.setString(4, UTUN_CONTROL_NAME);
            if ((int) api.ioctl.invokeExact(handle, CTLIOCGINFO, info) == -1) {
                getAndThrowException("ioctl");
            }
            MemorySegment addr = arena.allocate(32);
            addr.set(ValueLayout.JAVA_BYTE, 0, (byte) addr.byteSize());
            addr.set(ValueLayout.JAVA_BYTE, 1, (byte) AF_SYSTEM);
            addr.set(ValueLayout.JAVA_SHORT, 2, (short) AF_SYS_CONTROL);
            addr.set(ValueLayout.JAVA_INT, 4, info.get(ValueLayout.JAVA_INT, 0));
            addr.set(ValueLayout.JAVA_INT, 8, 6);// developer private number utunX X+1
            if ((int) api.connect.invokeExact(handle, addr, (int) addr.byteSize()) == -1) {
                getAndThrowException("connect");
            }
            MemorySegment utunNameLen = arena.allocate(ValueLayout.JAVA_INT);
            utunNameLen.set(ValueLayout.JAVA_INT, 0, 255);
            MemorySegment utunName = arena.allocate(255);
            if ((int) api.getsockopt.invokeExact(handle, SYSPROTO_CONTROL, UTUN_OPT_IFNAME, utunName, utunNameLen) == -1) {
                getAndThrowException("getsockopt");
            }
            this.deviceName = utunName.getString(0);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
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

    @Override
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

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isUp() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            short flags = ifr.get(ValueLayout.JAVA_SHORT, 16);
            return (flags & IFF_UP) != 0;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void setUp(boolean isUp) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            short flags = ifr.get(ValueLayout.JAVA_SHORT, 16);
            flags = (short) (isUp ? flags | IFF_UP : flags & ~IFF_UP);
            ifr.set(ValueLayout.JAVA_SHORT, 16, flags);
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFFLAGS, ifr) == -1) {
                getAndThrowException("ioctl");
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public InetAddress getInetAddress() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            ifr.set(ValueLayout.JAVA_BYTE, 17, (byte) 2);
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFADDR, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            int address = ifr.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20);
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFNETMASK, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            int mask = ifr.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20);
            InetAddress ipAddress = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(address).array());
            InetAddress ipMask = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(mask).array());
            System.out.println(ipAddress);
            System.out.println(ipMask);
            return null;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void setInetAddress(InetAddress inetAddress) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            ifr.set(ValueLayout.JAVA_BYTE, 17, (byte) 2);
            ifr.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20, ByteBuffer.wrap(new byte[] {10, 0, 0, 1}).getInt());
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFADDR, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            ifr.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 20, ByteBuffer.wrap(new byte[] {(byte) 255, 0, 0, 0}).getInt());
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFNETMASK, ifr) == -1) {
                getAndThrowException("ioctl");
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public int getMtu() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFMTU, ifr) == -1) {
                getAndThrowException("ioctl");
            }
            int ifru_mtu = ifr.get(ValueLayout.JAVA_INT, 16);
            return ifru_mtu;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void setMtu(int mtu) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            ifr.set(ValueLayout.JAVA_INT, 16, mtu);
            int handle;
            if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) == -1) {
                getAndThrowException("socket");
            }
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFMTU, ifr) == -1) {
                getAndThrowException("ioctl");
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public byte[] getMacAddress() throws IOException {
        return new byte[0];
    }

    @Override
    public void setMacAddress(byte[] macAddress) throws IOException {

    }

    private void getAndThrowException(String function) throws Throwable {
        MemorySegment p_error = (MemorySegment) api.error.invokeExact();
        p_error = p_error.reinterpret(4);
        int error = p_error.get(ValueLayout.JAVA_INT, 0);
        MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
        strerror = strerror.reinterpret(1000);
        String string = strerror.getString(0);
        throw new IOException("Failed to call " + function + ": " + error + " " + string);
    }
}
