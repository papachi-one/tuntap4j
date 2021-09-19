package one.papachi.tuntap4j;

public class TapDevice extends NetworkDevice {

    public TapDevice(String deviceName) {
        super(deviceName, Type.TAP);
    }

}
