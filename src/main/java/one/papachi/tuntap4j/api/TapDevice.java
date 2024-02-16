package one.papachi.tuntap4j.api;

import java.util.List;

public interface TapDevice extends NetworkDevice, NicAddressConfigurable {

    static List<TapDevice> getAvailableDevices() {
        return null;
    }

}
