package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.util.List;

public interface NicAddressConfigurable {

    List<NicAddress> getNetworkAddresses() throws IOException;

    void addNetworkAddress(NicAddress nicAddress) throws IOException;

    void removeNetworkAddress(NicAddress nicAddress) throws IOException;

    byte[] getMacAddress() throws IOException;

    void setMacAddress(byte[] macAddress) throws IOException;

}
