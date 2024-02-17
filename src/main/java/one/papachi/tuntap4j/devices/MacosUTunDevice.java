package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.ffm.NativeMacosApi;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MacosUTunDevice extends AbstractMacosTunDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "utun5";
        NativeMacosApi api = new NativeMacosApi();
        MacosUTunDevice tun = new MacosUTunDevice(api, deviceName);
        tun.open();
        tun.addInetAddresses(InetAddress.ofLiteral("10.0.0.1"), InetAddress.ofLiteral("10.0.0.2"), 32);
        tun.addInetAddresses(InetAddress.ofLiteral("2001:da8:ecd1::1"), InetAddress.ofLiteral("2001:da8:ecd1::2"), 128);
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
            MemorySegment info = arena.allocate(100);
            info.setString(4, NativeMacosApi.UTUN_CONTROL_NAME);

            handle = api.socket(NativeMacosApi.AF_SYSTEM, NativeMacosApi.SYSPROTO_CONTROL);
            api.ioctl(handle, NativeMacosApi.CTLIOCGINFO, info);

            int developerPrivateNumber = Integer.parseInt(deviceName.replaceAll("([A-Za-z])+", "")) + 1;

            MemorySegment addr = arena.allocate(32);
            addr.set(ValueLayout.JAVA_BYTE, 0, (byte) addr.byteSize());
            addr.set(ValueLayout.JAVA_BYTE, 1, (byte) NativeMacosApi.AF_SYSTEM);
            addr.set(ValueLayout.JAVA_SHORT, 2, (short) NativeMacosApi.AF_SYS_CONTROL);
            addr.set(ValueLayout.JAVA_INT, 4, info.get(ValueLayout.JAVA_INT, 0));
            addr.set(ValueLayout.JAVA_INT, 8, developerPrivateNumber);

            if ((int) api.connect.invokeExact(this.handle, addr, (int) addr.byteSize()) < 0)
                api.getAndThrowException("connect");

            MemorySegment utunNameLen = arena.allocate(ValueLayout.JAVA_INT);
            utunNameLen.set(ValueLayout.JAVA_INT, 0, 255);
            MemorySegment utunName = arena.allocate(255);

            if ((int) api.getsockopt.invokeExact(this.handle, NativeMacosApi.SYSPROTO_CONTROL, NativeMacosApi.UTUN_OPT_IFNAME, utunName, utunNameLen) < 0)
                api.getAndThrowException("getsockopt");
            this.deviceName = utunName.getString(0);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

}
