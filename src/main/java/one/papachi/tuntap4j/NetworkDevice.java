package one.papachi.tuntap4j;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

public abstract class NetworkDevice {

    private static final String temporaryLibraryPath = System.getProperty("java.io.tmpdir");

    static {
        String library = Utils.getOperatingSystemFamily().getLibraryName();
        File file = Paths.get(temporaryLibraryPath, library).toFile();
        try (InputStream is = NetworkDevice.class.getResourceAsStream(library); OutputStream os = new FileOutputStream(file)) {
            is.transferTo(os);
        } catch (Exception e) {
        }
        System.load(file.getAbsolutePath());
    }

    enum Type {
        TUN, TAP
    }

    protected final String deviceName;

    protected final Type type;

    protected long deviceHandle = -1;

    protected NetworkDevice(String deviceName, Type type) {
        this.deviceName = deviceName;
        this.type = type;
    }

    public boolean isOpen() throws IOException {
        return isOpen(deviceName, deviceHandle);
    }

    public void open() throws IOException {
        deviceHandle = open(deviceName, type == Type.TAP);
    }

    public int read(ByteBuffer dst) throws IOException {
        return read(deviceName, deviceHandle, dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return write(deviceName, deviceHandle, src);
    }

    public void close() throws IOException {
        close(deviceName, deviceHandle);
    }

    public void setStatus(boolean isUp) throws IOException {
        setStatus(deviceName, deviceHandle, isUp);
    }

    public String getIPAddress() throws IOException {
        return getIPAddress(deviceName, deviceHandle);

    }

    public void setIPAddress(String address, String mask) throws IOException {
        setIPAddress(deviceName, deviceHandle, address, mask);
    }

    public int getMTU() throws IOException {
        return getMTU(deviceName, deviceHandle);
    }

    public void setMTU(int mtu) throws IOException {
        setMTU(deviceName, deviceHandle, mtu);
    }

    public byte[] getMACAddress() throws IOException {
        if (type == Type.TUN)
            throw new UnsupportedOperationException("TUN device does not have MAC address");
        return getMACAddress(deviceName, deviceHandle);
    }

    public void setMACAddress(byte[] macAddress) throws IOException {
        if (type == Type.TUN)
            throw new UnsupportedOperationException("TUN device does not have MAC address");
        setMACAddress(deviceName, deviceHandle, macAddress);
    }

    protected static native boolean isOpen(String deviceName, long deviceHandle);

    protected static native long open(String deviceName, boolean isTAP) throws IOException;

    protected static native int read(String deviceName, long deviceHandle, ByteBuffer dst) throws IOException;

    protected static native int write(String deviceName, long deviceHandle, ByteBuffer src) throws IOException;

    protected static native void close(String deviceName, long deviceHandle) throws IOException;

    protected static native void setStatus(String deviceName, long deviceHandle, boolean isUp) throws IOException;

    protected static native String getIPAddress(String deviceName, long deviceHandle) throws IOException;

    protected static native void setIPAddress(String deviceName, long deviceHandle, String address, String mask) throws IOException;

    protected static native int getMTU(String deviceName, long deviceHandle) throws IOException;

    protected static native void setMTU(String deviceName, long deviceHandle, int mtu) throws IOException;

    protected static native byte[] getMACAddress(String deviceName, long deviceHandle) throws IOException;

    protected static native void setMACAddress(String deviceName, long deviceHandle, byte[] macAddress) throws IOException;

    protected static native List<String> getAvailableTunDevices() throws IOException;

    protected static native List<String> getAvailableTapDevices() throws IOException;

}
