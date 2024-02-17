package one.papachi.tuntap4j.api;

import java.util.List;

public interface TapDevice extends NetworkDevice, TapAddressable {

    static List<TapDevice> getAvailableDevices() {
        return null;
    }

    byte[] getMacAddress();

    void setMacAddress(byte[] macAddress);
}
