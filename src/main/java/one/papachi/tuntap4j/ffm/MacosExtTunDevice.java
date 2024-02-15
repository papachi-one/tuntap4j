package one.papachi.tuntap4j.ffm;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

public abstract class MacosExtTunDevice extends AbstractMacosTunDevice {

    public static void main(String[] args) throws Throwable {
        String deviceName = "tun0";
        NativeMacosApi api = new NativeMacosApi();
        MacosExtTunDevice tun = null;
        tun.open();
        System.out.println(tun.isUp());
        Thread.sleep(1000);
        // TODO
        ByteBuffer dst = ByteBuffer.allocateDirect(1514);
        while (tun.read(dst.clear()) != -1) {
            dst.flip();
            System.out.println(dst);
        }
    }

    public MacosExtTunDevice(NativeMacosApi api, String deviceName) {
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

}
