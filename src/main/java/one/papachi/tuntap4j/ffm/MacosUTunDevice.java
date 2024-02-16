package one.papachi.tuntap4j.ffm;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.ByteBuffer;

public class MacosUTunDevice extends AbstractMacosTunDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "utun5";
        NativeMacosApi api = new NativeMacosApi();
        MacosUTunDevice tun = new MacosUTunDevice(api, deviceName);
        tun.open();
        tun.setTunnelAddresses(Inet4Address.ofLiteral("10.0.0.1"), Inet4Address.ofLiteral("10.0.0.2"));
        tun.setTunnelAddresses(Inet4Address.ofLiteral("10.1.0.1"), Inet4Address.ofLiteral("10.1.0.2"));
        tun.setTunnelAddresses(Inet6Address.ofLiteral("2001:da8:ecd1::1"), Inet6Address.ofLiteral("2001:da8:ecd1::2"));
        tun.setTunnelAddresses(Inet6Address.ofLiteral("2001:da9:ecd1::1"), Inet6Address.ofLiteral("2001:da9:ecd1::2"));
        ByteBuffer dst = ByteBuffer.allocateDirect(1514);
        while (tun.read(dst.clear()) != -1) {
            dst.flip();
            System.out.println(dst);
        }
    }

    public MacosUTunDevice(NativeMacosApi api, String deviceName) {
        this.api = api;
        this.deviceName = deviceName;
    }

    @Override
    public void open() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            if ((handle = (int) api.socket.invokeExact(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL)) < 0)
                getAndThrowException("socket");
            MemorySegment info = arena.allocate(100);
            info.setString(4, UTUN_CONTROL_NAME);
            if ((int) api.ioctl.invokeExact(handle, CTLIOCGINFO, info) < 0)
                getAndThrowException("ioctl");
            MemorySegment addr = arena.allocate(32);
            addr.set(ValueLayout.JAVA_BYTE, 0, (byte) addr.byteSize());
            addr.set(ValueLayout.JAVA_BYTE, 1, (byte) AF_SYSTEM);
            addr.set(ValueLayout.JAVA_SHORT, 2, (short) AF_SYS_CONTROL);
            addr.set(ValueLayout.JAVA_INT, 4, info.get(ValueLayout.JAVA_INT, 0));
            addr.set(ValueLayout.JAVA_INT, 8, 6);// developer private number utunX X+1
            if ((int) api.connect.invokeExact(handle, addr, (int) addr.byteSize()) < 0)
                getAndThrowException("connect");
            MemorySegment utunNameLen = arena.allocate(ValueLayout.JAVA_INT);
            utunNameLen.set(ValueLayout.JAVA_INT, 0, 255);
            MemorySegment utunName = arena.allocate(255);
            if ((int) api.getsockopt.invokeExact(handle, SYSPROTO_CONTROL, UTUN_OPT_IFNAME, utunName, utunNameLen) < 0)
                getAndThrowException("getsockopt");
            this.deviceName = utunName.getString(0);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

}
