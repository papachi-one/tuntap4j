package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.InetAddress;

public interface TunAddresssable {

    void addInetAddresses(InetAddress localAddress, InetAddress remoteAddress, int prefixLength) throws IOException;

    void removeInetAddresses(InetAddress localAddress, InetAddress remoteAddress, int prefixLength) throws IOException;

}
