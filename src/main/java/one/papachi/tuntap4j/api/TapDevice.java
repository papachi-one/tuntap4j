package one.papachi.tuntap4j.api;

import java.util.List;

public interface TapDevice extends NetworkDevice {

    static List<TapDevice> getAvailableDevices() {
        return null;
    }

}
