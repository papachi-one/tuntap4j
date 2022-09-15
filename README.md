# tuntap4j
Java library for interfacing with TUN/TAP devices.
## Supported platforms
### Linux
Linux provides out of the box support for TUN and TAP devices without any installation required.
#### Sample code
##### TUN device
```java
import one.papachi.tuntap4j.TunDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

class App {

    public static void main(String[] args) throws IOException {
        String deviceName = "tun0"; // it can be any name e.g. "mytun"
        TunDevice device = new TunDevice(deviceName); // initialize a TUN device
        device.open(); // open device for reading and writing
        device.setStatus(true); // set device status to "up"
        device.setIPAddress("10.0.0.0", "255.0.0.0"); // set IPv4 address and mask
        int mtu = device.getMTU(); // get MTU of the device
        ByteBuffer buffer = ByteBuffer.allocate(mtu); // allocate ByteBuffer to use in read operation (direct ByteBuffer is supported)
        while (true) {
            device.read(buffer.clear()); // read from the device into ByteBuffer
            buffer.flip(); // prepare ByteBuffer for reading
            // TODO process IP packet
        }
    }

}
```
##### TAP device
```java
import one.papachi.tuntap4j.TapDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

class App {

    public static void main(String[] args) throws IOException {
        String deviceName = "tap0"; // it can be any name e.g. "mytap"
        TapDevice device = new TapDevice(deviceName); // initialize a TAP device
        device.open(); // open device for reading and writing
        device.setStatus(true); // set device status to "up"
        device.setIPAddress("10.0.0.0", "255.0.0.0"); // set IPv4 address and mask
        int mtu = device.getMTU(); // get MTU of the device
        mtu += 14; // 14 bytes for Ethernet header
        ByteBuffer buffer = ByteBuffer.allocate(mtu); // allocate ByteBuffer to use in read operation (direct ByteBuffer is supported)
        while (true) {
            device.read(buffer.clear()); // read from the device into ByteBuffer
            buffer.flip(); // prepare ByteBuffer for reading
            // TODO process IP packet
        }
    }

}
```
### Windows
On Windows only TAP device is supported. There is no TUN device support (natively or by third-party).
TAP-Windows Adapter V9 is supported from OpenVPN package.
#### Installation
You can download the TAP-Windows Adapter from [here](https://build.openvpn.net/downloads/releases/) (latest version is 9.24.7).
You can choose an [installer](https://build.openvpn.net/downloads/releases/tap-windows-9.24.7-I601-Win10.exe) or manual installation from [zip file](https://build.openvpn.net/downloads/releases/tap-windows-9.24.7.zip).
Recommended option is to use an installer.
##### TAP device
```java
import one.papachi.tuntap4j.NetworkDevice;
import one.papachi.tuntap4j.TapDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

class App {

    public static void main(String[] args) throws IOException {
        List<String> availableTapDevices = NetworkDevice.getAvailableTapDevices(); // get list of TAP devices available to use
        if (availableTapDevices.isEmpty()) {
            System.out.println("No TAP device is available on the system to use.");
            return;
        }
        String deviceName = availableTapDevices.get(0); // get first available TAP device (e.g. {C9842FFE-AA5D-4AD1-82E0-6580767C25CA})
        TapDevice device = new TapDevice(deviceName); // initialize a TAP device
        device.open(); // open device for reading and writing
        device.setStatus(true); // set device status to "connected"
        device.setIPAddress("10.0.0.0", "255.0.0.0"); // set IPv4 address and mask
        int mtu = device.getMTU(); // get MTU of the device
        mtu += 14; // 14 bytes for Ethernet header
        ByteBuffer buffer = ByteBuffer.allocate(mtu); // allocate ByteBuffer to use in read operation (direct ByteBuffer is supported)
        while (true) {
            device.read(buffer.clear()); // read from the device into ByteBuffer
            buffer.flip(); // prepare ByteBuffer for reading
            // TODO process Ethernet frame
        }
    }

}
```
### macOS
macOS provides out of the box support for TUN (utun) devices.
Support for TAP devices requires installation of a system/kernel extension to be installed and loaded in order to use it.
#### Installation (for TAP device support)
TAP system/kernel extension can be downloaded from [here](https://github.com/Tunnelblick/Tunnelblick).
- Download repository as a [zip file](https://github.com/Tunnelblick/Tunnelblick/archive/refs/heads/master.zip).
- Extract "tap-notarized.kext" from "third_party" directory
- Rename "tap-notarized.kext" to "tap.kext"
- Copy "tap.kext" to "/Library/Extensions"
- Set owner to root:wheel `sudo chown -R root:wheel /Library/Extensions/tap.kext`
- Run sudo `kextload /Library/Extensions/tap.kext` (won't be successful on first time)
  - Open System Preferences -> Security & Privacy -> Allow
  - Restart macOS
  - Run the command again (this time it should work)

After installation and loading of the TAP system/kernel extension we have available 16 TAP devices (/dev/tap0 through /dev/tap15).
#### Sample code
##### TUN device
```java
import one.papachi.tuntap4j.NetworkDevice;
import one.papachi.tuntap4j.TunDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

class App {

    public static void main(String[] args) throws IOException {
        List<String> availableTunDevices = NetworkDevice.getAvailableTunDevices(); // get list of TUN devices available to use
        if (availableTunDevices.isEmpty()) {
            System.out.println("No TUN device is available on the system to use.");
            return;
        }
        String deviceName = availableTapDevices.get(0); // get first available TUN device (e.g. utun0)
        TunDevice device = new TunDevice(deviceName); // initialize a TUN device
        device.open(); // open device for reading and writing
        device.setStatus(true); // set device status to "up"
        device.setIPAddress("10.0.0.0", "255.0.0.0"); // set IPv4 address and mask
        int mtu = device.getMTU(); // get MTU of the device
        ByteBuffer buffer = ByteBuffer.allocate(mtu); // allocate ByteBuffer to use in read operation (direct ByteBuffer is supported)
        while (true) {
            device.read(buffer.clear()); // read from the device into ByteBuffer
            buffer.flip(); // prepare ByteBuffer for reading
            // TODO process IP packet
        }
    }

}
```
##### TAP device
```java
import one.papachi.tuntap4j.TapDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

class App {

    public static void main(String[] args) throws IOException {
        List<String> availableTapDevices = NetworkDevice.getAvailableTapDevices(); // get list of TAP devices available to use
        if (availableTapDevices.isEmpty()) {
            System.out.println("No TAP device is available on the system to use.");
            return;
        }
        String deviceName = availableTapDevices.get(0); // get first available TAP device (e.g. tap0)
        TapDevice device = new TapDevice(deviceName); // initialize a TAP device
        device.open(); // open device for reading and writing
        device.setStatus(true); // set device status to "up"
        device.setIPAddress("10.0.0.0", "255.0.0.0"); // set IPv4 address and mask
        int mtu = device.getMTU(); // get MTU of the device
        mtu += 14; // 14 bytes for Ethernet header
        ByteBuffer buffer = ByteBuffer.allocate(mtu); // allocate ByteBuffer to use in read operation (direct ByteBuffer is supported)
        while (true) {
            device.read(buffer.clear()); // read from the device into ByteBuffer
            buffer.flip(); // prepare ByteBuffer for reading
            // TODO process Ethernet frame
        }
    }

}
```