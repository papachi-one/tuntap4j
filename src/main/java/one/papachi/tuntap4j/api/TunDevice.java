package one.papachi.tuntap4j.api;

import java.util.List;

public interface TunDevice extends NetworkDevice {

    static List<TunDevice> getAvailableDevices() {
        return null;
    }

}
