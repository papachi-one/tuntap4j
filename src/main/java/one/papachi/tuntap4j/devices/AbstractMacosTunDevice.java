package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.api.TunDevice;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.net.InetAddress;

public abstract class AbstractMacosTunDevice extends AbstractMacosNetworkDevice implements TunDevice {

    @Override
    public void addInetAddresses(InetAddress localAddress, InetAddress remoteAddress, int prefixLength) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.doAddress(arena, deviceName, localAddress, remoteAddress, prefixLength, false);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    @Override
    public void removeInetAddresses(InetAddress localAddress, InetAddress remoteAddress, int prefixLength) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.doAddress(arena, deviceName, localAddress, remoteAddress, prefixLength, true);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

}
