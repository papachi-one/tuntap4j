package one.papachi.tuntap4j.api;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

public interface NetworkDevice {

    public static List<TunDevice> getAvailableTunDevices() throws IOException {
        return TunDevice.getAvailableDevices();
    }

    public static List<TapDevice> getAvailableTapDevices() throws IOException {
        return TapDevice.getAvailableDevices();
    }

    boolean isOpen() throws IOException;

    void open() throws IOException;

    int read(ByteBuffer dst) throws IOException;

    int write(ByteBuffer src) throws IOException;

    void close() throws IOException;

    boolean isUp() throws IOException;

    void setUp(boolean isUp) throws IOException;

    int getMtu() throws IOException;

    void setMtu(int mtu) throws IOException;

}
