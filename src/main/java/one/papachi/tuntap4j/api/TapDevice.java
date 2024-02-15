package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface TapDevice extends NetworkDevice {

    static List<TapDevice> getAvailableDevices() {
        return null;
    }

    Inet4Address getHostAddress() throws IOException;

    Inet4Address getNetworkMask() throws IOException;

    int getNetworkCIDR() throws IOException;

    void setHostAddress(Inet4Address localAddress) throws IOException;

    void setNetworkMask(Inet4Address networkMask) throws IOException;

    void setNetworkCIDR(int networkCIDR) throws IOException;

    byte[] getMACAddress();

    void setMACAddress(byte[] macAddress);

}
