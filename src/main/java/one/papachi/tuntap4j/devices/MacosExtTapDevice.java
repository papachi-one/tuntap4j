package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.api.TapDevice;
import one.papachi.tuntap4j.ffm.NativeMacosApi;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MacosExtTapDevice extends AbstractMacosNetworkDevice implements TapDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "tap1";
        NativeMacosApi api = new NativeMacosApi();
        MacosExtTapDevice tap = new MacosExtTapDevice(api, deviceName);
        tap.open();
        tap.addInetAddress(InetAddress.ofLiteral("10.0.0.1"), 8);
        tap.addInetAddress(InetAddress.ofLiteral("2001:da8:ecd1::1"), 64);
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
            if ((handle = (int) api.open.invokeExact(pDevicePath, NativeMacosApi.O_RDWR)) < 0)
                api.getAndThrowException("open");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public byte[] getMacAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMacAddress(byte[] macAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addInetAddress(InetAddress address, int prefixLength) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.doAddress(arena, deviceName, address, null, prefixLength, false);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void removeInetAddress(InetAddress address, int prefixLength) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.doAddress(arena, deviceName, address, null, prefixLength, false);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }

    }

}
