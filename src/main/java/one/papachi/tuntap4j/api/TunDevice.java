package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public interface TunDevice extends NetworkDevice, TunnelAddressConfigurable {

    static List<TunDevice> getAvailableDevices() {
        return null;
    }

    InetAddress getLocalAddress() throws IOException;

    InetAddress getRemoteAddress() throws IOException;

    void setLocalAddress(InetAddress localAddress) throws IOException;

    void setRemoteAddress(InetAddress remoteAddress) throws IOException;

}
