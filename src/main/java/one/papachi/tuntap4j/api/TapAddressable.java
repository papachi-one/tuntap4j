package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.InetAddress;

public interface TapAddressable {

    void addInetAddress(InetAddress address, int prefixLength) throws IOException;

    void removeInetAddress(InetAddress address, int prefixLength) throws IOException;

}
