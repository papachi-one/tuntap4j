package one.papachi.tuntap4j;

import java.util.List;

public class TunTap4j {

    public static void main(String[] args) throws Throwable {
        List<String> availableTapDevices = NetworkDevice.getAvailableTapDevices();
        availableTapDevices.forEach(System.out::println);
        String deviceName = availableTapDevices.get(0);
        System.out.println(deviceName);
        TapDevice device = new TapDevice(deviceName);
        device.open();
        boolean isOpen = device.isOpen();
        System.out.println("isOpen = " + isOpen);
        if (!isOpen)
            return;
        device.setStatus(true);
        int mtu = device.getMTU();
        System.out.println("MTU = " + mtu);
        byte[] macAddress = device.getMACAddress();
        String macAddressString = String.format("%02X-%02X-%02X-%02X-%02X-%02X", macAddress[0], macAddress[1], macAddress[2], macAddress[3], macAddress[4], macAddress[5]);
        System.out.println("MAC = " + macAddressString);
        device.setIPAddress("10.0.0.1", "255.0.0.0");
        String ipAddress = device.getIPAddress();
        System.out.println("ipAddress = " + ipAddress);
        Thread.sleep(10000);
    }

}
