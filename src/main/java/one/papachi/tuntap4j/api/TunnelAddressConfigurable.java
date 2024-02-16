package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.InetAddress;

public interface TunnelAddressConfigurable {

    void setTunnelAddresses(InetAddress localAddress, InetAddress remoteAddress) throws IOException;

    InetAddress getLocalAddress() throws IOException;

    void setLocalAddress(InetAddress address) throws IOException;

    InetAddress getRemoteAddress() throws IOException;

    void setRemoteAddress(InetAddress address) throws IOException;

}
