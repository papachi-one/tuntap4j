package one.papachi.tuntap4j;

public class TunTap4j {

    public static void main(String[] args) throws Exception {
        TapDevice tapDevice = new TapDevice("{A6215D55-1B39-4C4E-B56E-250AB857A90A}");
        tapDevice.open();
        System.out.println(tapDevice.isOpen());
        tapDevice.setStatus(true);
        System.out.println(tapDevice.getMTU());
        System.out.println(tapDevice.getMACAddress());
        tapDevice.setIPAddress("10.0.0.1", "255.0.0.0");
        System.out.println(tapDevice.getIPAddress());
        Thread.sleep(10000);
    }

}
