package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.TapDevice;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MacosExtTapDevice extends AbstractMacosNetworkDevice implements TapDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "tap1";
        NativeMacosApi api = new NativeMacosApi();
        MacosExtTapDevice tap = new MacosExtTapDevice(api, deviceName);
        tap.open();
        tap.setHostAddress(Inet4Address.ofLiteral("172.16.32.1"));
//        tap.setNetworkMask(Inet4Address.ofLiteral("255.255.255.0"));
        System.out.println(tap.isUp());
//        tap.setNetworkCIDR(24);
        System.out.println(tap.getHostAddress());
        System.out.println(tap.getNetworkMask());
        System.out.println(tap.getNetworkCIDR());
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
    public Inet4Address getHostAddress() throws IOException {
        return getAddress(SIOCGIFADDR);
    }

    @Override
    public void setHostAddress(Inet4Address hostAddress) throws IOException {
        setAddress(hostAddress, SIOCSIFADDR);
    }

    @Override
    public Inet4Address getNetworkMask() throws IOException {
        return getAddress(SIOCGIFNETMASK);
    }

    @Override
    public void setNetworkMask(Inet4Address networkMask) throws IOException {
        setAddress(networkMask, SIOCSIFNETMASK);
    }

    @Override
    public int getNetworkCIDR() throws IOException {
        return Integer.bitCount(ByteBuffer.wrap(getNetworkMask().getAddress()).getInt());
    }

    @Override
    public void setNetworkCIDR(int networkCIDR) throws IOException {
        int mask = 0;
        for (int i = 0; i < networkCIDR; i++)
            mask |= 1 << (32 - i);
        setNetworkMask((Inet4Address) InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(mask).array()));
    }

    @Override
    public byte[] getMACAddress() {
        return new byte[0];// TODO
    }

    @Override
    public void setMACAddress(byte[] macAddress) {
        // TODO
    }

}
