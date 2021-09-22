# tuntap4j
Java library for interfacing with TUN/TAP devices.

## Supported platforms

### Linux
Linux provides out of the box support for TUN and TAP devices. All you have to do is to provide desired device name.

### Windows
On Windows we can currently create TAP devices using OpenVPNs virtual network adapter (Tap Windows Adapter V9).<br>
Provisioning of virtual network adapter is out of scope of this library. Network adapter has to be already created beforehand.

### macOS
On macOS there is native support of TUN devices (utun).<br>
Alternatively for TUN devices, we support system/kernel extension.

For TAP devices, a system/kernel extension has to be installed and loaded in order to use it.<br>
This provides us with /dev/tap0 up to /dev/tap15 (total of 16 TAP devices).

## Sample code
```java
import one.papachi.tuntap4j.TunDevice;
import java.io.IOException;

class App {

    public static void main(String[] args) throws IOException {
        TunDevice tunDevice = new TunDevice("tun0");
        tunDevice.open();
        System.out.println(tunDevice.isOpen());
    }

}
```
```java
import one.papachi.tuntap4j.TapDeviceDevice;
import java.io.IOException;

class App {

    public static void main(String[] args) throws IOException {
        TapDevice tapDevice = new TapDevice("tap0");
        tapDevice.open();
        System.out.println(tapDevice.isOpen());
    }

}
```
