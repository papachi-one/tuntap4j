package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public interface TunDevice extends NetworkDevice {

    static List<TunDevice> getAvailableDevices() {
        return null;
    }

    Inet4Address getLocalAddress() throws IOException;

    Inet4Address getRemoteAddress() throws IOException;

    void setLocalAddress(Inet4Address localAddress) throws IOException;

    void setRemoteAddress(Inet4Address remoteAddress) throws IOException;

}
