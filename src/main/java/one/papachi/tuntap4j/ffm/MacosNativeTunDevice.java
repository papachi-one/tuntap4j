package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.TunDevice;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MacosNativeTunDevice implements TunDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "utun5";
        NativeMacosApi api = new NativeMacosApi();
        MacosNativeTunDevice tun = new MacosNativeTunDevice(api, deviceName);
        tun.open();
        System.out.println(tun.isUp());
        System.out.println(tun.getMtu());
        tun.setMtu(1024);
        System.out.println(tun.getMtu());
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

    @Override
    public boolean isOpen() throws IOException {
        int F_GETFD = 1;
        try {
            return (int) api.fcntl.invokeExact(handle, F_GETFD) != -1;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void open() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            int PF_SYSTEM = 32;
            int SOCK_DGRAM = 2;
            int SYSPROTO_CONTROL = 2;
            String UTUN_CONTROL_NAME = "com.apple.net.utun_control";
            int CTLIOCGINFO = (int) 3227799043L;
            int AF_SYSTEM = 32;
            int AF_SYS_CONTROL = 2;
            if ((handle = (int) api.socket.invokeExact(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL)) == -1) {
                throw new IOException("Failed to open socket.");
            }
            StructLayout ctl_info = MemoryLayout.structLayout(
                    ValueLayout.JAVA_INT.withName("ctl_id"),
                    MemoryLayout.sequenceLayout(96, ValueLayout.JAVA_BYTE).withName("ctl_name")
            );
            MemorySegment info = arena.allocate(100);
            info.setString(4, UTUN_CONTROL_NAME);
            if ((int) api.ioctl.invokeExact(handle, CTLIOCGINFO, info) == -1) {
                throw new IOException("Failed to call ioctl.");
            }
            StructLayout sockaddr_ctl = MemoryLayout.structLayout(
                    ValueLayout.JAVA_BYTE.withName("sc_len"),
                    ValueLayout.JAVA_BYTE.withName("sc_family"),
                    ValueLayout.JAVA_SHORT.withName("ss_sysaddr"),
                    ValueLayout.JAVA_INT.withName("sc_id"),
                    ValueLayout.JAVA_INT.withName("sc_unit"),
                    ValueLayout.JAVA_INT.withName("sc_reserved[0]"),
                    ValueLayout.JAVA_INT.withName("sc_reserved[1]"),
                    ValueLayout.JAVA_INT.withName("sc_reserved[2]"),
                    ValueLayout.JAVA_INT.withName("sc_reserved[3]"),
                    ValueLayout.JAVA_INT.withName("sc_reserved[4]")
            );
            MemorySegment addr = arena.allocate(sockaddr_ctl);
            addr.set(ValueLayout.JAVA_BYTE, 0, (byte) addr.byteSize());
            addr.set(ValueLayout.JAVA_BYTE, 1, (byte) AF_SYSTEM);
            addr.set(ValueLayout.JAVA_SHORT, 2, (short) AF_SYS_CONTROL);
            addr.set(ValueLayout.JAVA_INT, 4, info.get(ValueLayout.JAVA_INT, 0));
            addr.set(ValueLayout.JAVA_INT, 8, 6);// developer private number utunX X+1
            ByteBuffer buffer = addr.asByteBuffer();
            while (buffer.hasRemaining()) {
                System.out.printf("%02x ", buffer.get());
            }
            System.out.println();
            if ((int) api.connect.invokeExact(handle, addr, (int) addr.byteSize()) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call connect. " + error + " " + string);
            }
            MemorySegment utunNameLen = arena.allocate(ValueLayout.JAVA_INT);
            utunNameLen.set(ValueLayout.JAVA_INT, 0, 255);
            MemorySegment utunName = arena.allocate(255);
            int UTUN_OPT_IFNAME = 2;
            if ((int) api.getsockopt.invokeExact(handle, SYSPROTO_CONTROL, UTUN_OPT_IFNAME, utunName, utunNameLen) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call getsockopt. " + error + " " + string);
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
            if ((handle = (int) api.socket.invokeExact(2, 2, 0)) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call socket. " + error + " " + string);
            }
            int SIOCGIFFLAGS = (int) 3223349521L;
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call ioctl. " + error + " " + string);
            }
            short IFF_UP = 1;
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
            if ((handle = (int) api.socket.invokeExact(2, 2, 0)) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call socket. " + error + " " + string);
            }
            int SIOCGIFFLAGS = (int) 3223349521L;
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFFLAGS, ifr) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call ioctl. " + error + " " + string);
            }
            short IFF_UP = 1;
            short flags = ifr.get(ValueLayout.JAVA_SHORT, 16);
            flags = (short) (isUp ? flags | IFF_UP : flags & ~IFF_UP);
            ifr.set(ValueLayout.JAVA_SHORT, 16, flags);
            int SIOCSIFFLAGS = (int) 2149607696L;
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFFLAGS, ifr) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call ioctl. " + error + " " + string);
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public InetAddress getInetAddress() throws IOException {
        return null;
    }

    @Override
    public void setInetAddress(InetAddress inetAddress) throws IOException {

    }

    @Override
    public int getMtu() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ifr = arena.allocate(32);
            ifr.setString(0, deviceName);
            int handle;
            if ((handle = (int) api.socket.invokeExact(2, 2, 0)) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call socket. " + error + " " + string);
            }
            int SIOCGIFMTU = (int) 3223349555L;
            if ((int) api.ioctl.invokeExact(handle, SIOCGIFMTU, ifr) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call ioctl. " + error + " " + string);
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
            if ((handle = (int) api.socket.invokeExact(2, 2, 0)) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call socket. " + error + " " + string);
            }
            int SIOCSIFMTU = (int) 2149607732L;
            if ((int) api.ioctl.invokeExact(handle, SIOCSIFMTU, ifr) == -1) {
                MemorySegment p_error = (MemorySegment) api.error.invokeExact();
                p_error = p_error.reinterpret(4);
                int error = p_error.get(ValueLayout.JAVA_INT, 0);
                MemorySegment strerror = (MemorySegment) api.strerror.invokeExact(error);
                strerror = strerror.reinterpret(1000);
                String string = strerror.getString(0);
                throw new IOException("Failed to call ioctl. " + error + " " + string);
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
}
