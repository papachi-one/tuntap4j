package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.TunDevice;

import java.io.IOException;
import java.net.Inet4Address;

public abstract class AbstractMacosTunDevice extends AbstractMacosNetworkDevice implements TunDevice {

    @Override
    public Inet4Address getLocalAddress() throws IOException {
        return getAddress(SIOCGIFADDR);
    }

    @Override
    public void setLocalAddress(Inet4Address localAddress) throws IOException {
        setAddress(localAddress, SIOCSIFADDR);
    }

    @Override
    public Inet4Address getRemoteAddress() throws IOException {
        return getAddress(SIOCGIFDSTADDR);
    }

    @Override
    public void setRemoteAddress(Inet4Address remoteAddress) throws IOException {
        setAddress(remoteAddress, SIOCSIFDSTADDR);
    }

}
