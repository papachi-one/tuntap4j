package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.api.TapDevice;
import one.papachi.tuntap4j.ffm.NativeWindowsApi;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public abstract class TapV9Adapter implements TapDevice {

//    static final int TAP_WIN_IOCTL_GET_MAC = 2228228;
//    static final int TAP_WIN_IOCTL_GET_VERSION = 2228232;
    static final int TAP_WIN_IOCTL_GET_MTU = 2228236;
//    static final int TAP_WIN_IOCTL_GET_INFO = 2228240;
//    static final int TAP_WIN_IOCTL_CONFIG_POINT_TO_POINT = 2228244;
    static final int TAP_WIN_IOCTL_SET_MEDIA_STATUS = 2228248;
//    static final int TAP_WIN_IOCTL_CONFIG_DHCP_MASQ = 2228252;
//    static final int TAP_WIN_IOCTL_GET_LOG_LINE = 2228256;
//    static final int TAP_WIN_IOCTL_CONFIG_DHCP_SET_OPT = 2228260;
//    static final int TAP_WIN_IOCTL_CONFIG_TUN = 2228264;

    static final String ADAPTER_KEY = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4D36E972-E325-11CE-BFC1-08002BE10318}";
//    static final String NETWORK_CONNECTIONS_KEY = "SYSTEM\\CurrentControlSet\\Control\\Network\\{4D36E972-E325-11CE-BFC1-08002BE10318}";

    static final String USERMODEDEVICEDIR = "\\\\.\\Global\\";
//    static final String SYSDEVICEDIR = "\\Device\\";
//    static final String USERDEVICEDIR = "\\DosDevices\\Global\\";
    static final String TAP_WIN_SUFFIX = ".tap";

    public static void main(String[] args) throws Throwable {
        String netCfgInstanceId = "{584C682A-C1FC-45BB-AC18-F1E15841F499}";
        String deviceName = USERMODEDEVICEDIR + netCfgInstanceId + TAP_WIN_SUFFIX;
        NativeWindowsApi api = null;
//        TapV9Adapter tap = new TapV9Adapter(api, deviceName);
        TapV9Adapter tap = null;
        tap.open();
        System.out.println("Registry MTU: " + tap.getMtuRegistry());
        System.out.println("   IOCTL MTU: " + tap.getMtu());
        tap.setUp(true);
        ByteBuffer dst = ByteBuffer.allocateDirect(1514);
        while (tap.read(dst.clear()) != -1) {
            dst.flip();
            System.out.println(dst);
        }
    }

    private final NativeWindowsApi api;

    private final String deviceName;

    private MemorySegment handle = MemorySegment.NULL;

    private boolean isUp;

    TapV9Adapter(NativeWindowsApi api, String deviceName) {
        this.api = api;
        this.deviceName = deviceName;
    }

    @Override
    public boolean isOpen() throws IOException {
        if (handle == MemorySegment.NULL)
            return false;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpdwFlags = arena.allocateFrom(JAVA_INT, 0);
            int result = api.GetHandleInformation(handle, lpdwFlags);
            if (result == 0) {
                int error = api.GetLastError();
                if (error != 0) {
                    throwErrorMessage(error);
                    throw new IOException("IO operation failed with error code: " + error + ".");
                }
                throw new IOException("IO operation (DeviceIoControl) failed.");
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
        return true;
    }

    @Override
    public void open() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpFileName = arena.allocateFrom(deviceName);
            int dwDesiredAccess = 0x80000000 | 0x40000000;// GENERIC_READ | GENERIC_WRITE
            int dwShareMode = 0;
            MemorySegment lpSecurityAttributes = MemorySegment.NULL;
            int dwCreationDisposition = 3;// OPEN_EXISTING
            int dwFlagsAndAttributes = 0x00000004;// FILE_ATTRIBUTE_SYSTEM
            MemorySegment hTemplateFile = MemorySegment.NULL;
            int handle = api.CreateFileA(lpFileName, dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
            if (handle == -1) {
                int error = api.GetLastError();
                if (error != 0) {
                    throwErrorMessage(error);
                    throw new IOException("IO operation (CreateFileA) failed with error code: " + error + ".");
                }
                throw new IOException("IO operation (CreateFileA) failed.");
            }
            this.handle = MemorySegment.ofAddress(handle);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
    }

    private void throwErrorMessage() throws IOException {
        try {
            int error = api.GetLastError();
            if (error != 0) {
                throwErrorMessage(error);
                throw new IOException("Native operation failed with error code: " + error + ".");
            }
            throw new IOException("Native operation failed.");
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    private void throwErrorMessage(int error) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment message = arena.allocate(10000L);
            int res = api.FormatMessageA(0x1000, MemorySegment.NULL, error, 0, message, (int) message.byteSize(), MemorySegment.NULL);
            if (res != 0) {
                String messageString = message.getString(0);
                throw new IOException(messageString);
            }
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("ByteBuffer dst is read only.");
        }
        if (!dst.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer dst is not direct.");
        }
        if (!isUp()) {
            throw new IllegalStateException("Network device is not in \"UP\" state.");
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpBuffer = MemorySegment.ofBuffer(dst);
            int nNumberOfBytesToRead = dst.remaining();
            MemorySegment lpNumberOfBytesRead = arena.allocateFrom(JAVA_INT, 0);
            MemorySegment lpOverlapped = MemorySegment.NULL;
            int result = api.ReadFile(handle, lpBuffer, nNumberOfBytesToRead, lpNumberOfBytesRead, lpOverlapped);
            if (result == 0) {
                int error = api.GetLastError();
                if (error != 0) {
                    MemorySegment message = arena.allocate(10000);
                    int res = api.FormatMessageA(0x1000, MemorySegment.NULL, error, 0, message, (int) message.byteSize(), MemorySegment.NULL);
                    if (res != 0) {
                        String messageString = message.getString(0);
                        throw new IOException(messageString);
                    }
                    throw new IOException("IO operation (ReadFile) failed with error code: " + error + ".");
                }
                throw new IOException("IO operation (ReadFile) failed.");
            }
            int read = lpNumberOfBytesRead.get(JAVA_INT, 0);
            dst.position(dst.position() + read);
            return read;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!src.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer src is not direct.");
        }
        if (!isUp()) {
            throw new IllegalStateException("Network device is not in \"UP\" state.");
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpBuffer = MemorySegment.ofBuffer(src);
            int nNumberOfBytesToWrite = src.remaining();
            MemorySegment lpNumberOfBytesWritten = arena.allocateFrom(JAVA_INT, 0);
            MemorySegment lpOverlapped = MemorySegment.NULL;
            int result = api.WriteFile(handle, lpBuffer, nNumberOfBytesToWrite, lpNumberOfBytesWritten, lpOverlapped);
            if (result == 0) {
                int error = (int) api.GetLastError();
                if (error != 0) {
                    MemorySegment message = arena.allocate(10000);
                    int res = (int) api.FormatMessageA(0x1000, MemorySegment.NULL, error, 0, message, (int) message.byteSize(), MemorySegment.NULL);
                    if (res != 0) {
                        String messageString = message.getString(0);
                        throw new IOException(messageString);
                    }
                    throw new IOException("IO operation (WriteFile) failed with error code: " + error + ".");
                }
                throw new IOException("IO operation (WriteFile) failed with error.");
            }
            int written = (int) lpNumberOfBytesWritten.get(JAVA_INT, 0);
            src.position(src.position() + written);
            return written;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            api.CloseHandle(handle);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        } finally {
            handle = MemorySegment.NULL;
            isUp = false;
        }
    }

    public boolean isUp() throws IOException {
        return isOpen() && isUp;
    }

    public void setUp(boolean isUp) throws IOException {
        if (!isOpen()) {
            throw new IllegalStateException("Network device is not opened.");
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpInBuffer = arena.allocateFrom(JAVA_INT, isUp ? 1 : 0);
            int nInOutBufferSize = (int) lpInBuffer.byteSize();
            int nOutBufferSize = nInOutBufferSize;
            MemorySegment lpBytesReturned = arena.allocateFrom(JAVA_INT, 0);
            MemorySegment lpOverlapped = MemorySegment.NULL;
            int result = api.DeviceIoControl(handle, TAP_WIN_IOCTL_SET_MEDIA_STATUS, lpInBuffer, nInOutBufferSize, lpInBuffer, nOutBufferSize, lpBytesReturned, lpOverlapped);
            if (result == 0) {
                throwErrorMessage();
            }
            this.isUp = true;
        } catch (IOException e) {
            this.isUp = false;
            throw e;
        } catch (Throwable e) {
            this.isUp = false;
            throw new IOException("IO operation failed.", e);
        }
    }

    public InetAddress getInetAddress() throws IOException {
        return null;
    }

    public void setInetAddress(InetAddress inetAddress) throws IOException {

    }

    public int getMtuRegistry() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment adapterKey = arena.allocateFrom(ADAPTER_KEY);
            MemorySegment hKey = arena.allocateFrom(JAVA_INT, 0);
            if (api.RegOpenKeyA(MemorySegment.ofAddress(0x80000002L), adapterKey, hKey) != 0) {
                throwErrorMessage();
            }
            hKey = MemorySegment.ofAddress(hKey.get(JAVA_INT, 0));
            MemorySegment lpcSubKeys = arena.allocateFrom(JAVA_INT, 0);
            if (api.RegQueryInfoKeyA(hKey, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, lpcSubKeys, arena.allocateFrom(JAVA_INT, 0),
                    MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL,
                    MemorySegment.NULL) != 0) {
                int error = api.GetLastError();
                if (error != 0) {
                    throwErrorMessage(error);
                    throw new IOException("Native function (RegQueryInfoKeyA) failed with error code: " + error + ".");
                }
                throw new IOException("Native function (RegQueryInfoKeyA) failed.");
            }
            MemorySegment name = arena.allocate(255);
            for (int dwIndex = 0; dwIndex < lpcSubKeys.get(JAVA_INT, 0); dwIndex++) {
                if (api.RegEnumKeyA(hKey, dwIndex, name, (int) name.byteSize()) != 0) {
                    throwErrorMessage();
                }
                MemorySegment pvDataNetCfgInstanceId = arena.allocate(255);
                if (api.RegGetValueA(hKey, name, arena.allocateFrom("NetCfgInstanceId"),
                        0x00000002 | 0x20000000, arena.allocateFrom(JAVA_INT, 0), pvDataNetCfgInstanceId,
                        arena.allocateFrom(JAVA_INT, (int) pvDataNetCfgInstanceId.byteSize())) != 0) {
                    continue;
                }
                if ("{584C682A-C1FC-45BB-AC18-F1E15841F499}".equals(pvDataNetCfgInstanceId.getString(0))) {
                    MemorySegment valueMTU = arena.allocate(255);
                    if (api.RegGetValueA(hKey, name, arena.allocateFrom("MTU"),
                            0x00000002 | 0x20000000, arena.allocateFrom(JAVA_INT, 0), valueMTU,
                            arena.allocateFrom(JAVA_INT, (int) valueMTU.byteSize())) != 0) {
                        continue;
                    }
                    return Integer.parseInt(valueMTU.getString(0));
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
        return -1;
    }

    public int getMtu() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lpInBuffer = MemorySegment.NULL;
            int nInBufferSize = (int) lpInBuffer.byteSize();
            MemorySegment lpOutBuffer = arena.allocateFrom(JAVA_INT, 0);
            int nOutBufferSize = (int) lpOutBuffer.byteSize();
            MemorySegment lpBytesReturned = arena.allocateFrom(JAVA_INT, 0);
            MemorySegment lpOverlapped = MemorySegment.NULL;
            int result = api.DeviceIoControl(handle, TAP_WIN_IOCTL_GET_MTU, lpInBuffer, nInBufferSize, lpOutBuffer, nOutBufferSize, lpBytesReturned, lpOverlapped);
            if (result == 0) {
                throwErrorMessage();
            }
            return lpOutBuffer.get(JAVA_INT, 0);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("IO operation failed.", e);
        }
    }

    public void setMtu(int mtu) throws IOException {

    }



}
