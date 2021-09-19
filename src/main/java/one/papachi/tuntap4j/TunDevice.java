package one.papachi.tuntap4j;

public class TunDevice extends NetworkDevice {

    public TunDevice(String deviceName) {
        super(deviceName, Type.TUN);
    }
}
