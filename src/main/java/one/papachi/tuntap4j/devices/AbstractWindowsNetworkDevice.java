package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.ffm.NativeWindowsApi;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class AbstractWindowsNetworkDevice {

    protected NativeWindowsApi api;

    public boolean isOpen(Arena arena, MemorySegment handle) throws Throwable {
        if (handle == MemorySegment.NULL)
            return false;
        MemorySegment lpdwFlags = arena.allocateFrom(JAVA_INT, 0);
        int result = api.GetHandleInformation(handle, lpdwFlags);
        return result != 0;
    }

    public MemorySegment open(Arena arena, String ifName) throws Throwable {
        MemorySegment lpFileName = arena.allocateFrom(ifName);
        int dwDesiredAccess = 0x80000000 | 0x40000000;// GENERIC_READ | GENERIC_WRITE
        int dwShareMode = 0;
        MemorySegment lpSecurityAttributes = MemorySegment.NULL;
        int dwCreationDisposition = 3;// OPEN_EXISTING
        int dwFlagsAndAttributes = 0x00000004;// FILE_ATTRIBUTE_SYSTEM
        MemorySegment hTemplateFile = MemorySegment.NULL;
        int handle = api.CreateFileA(lpFileName, dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
        if (handle == -1) {
            throwErrorMessage(arena, "CreateFileA");
        }
        return MemorySegment.ofAddress(handle);
    }

    public int read(Arena arena, MemorySegment handle, ByteBuffer dst) throws Throwable {
        MemorySegment lpBuffer = MemorySegment.ofBuffer(dst);
        int nNumberOfBytesToRead = dst.remaining();
        MemorySegment lpNumberOfBytesRead = arena.allocateFrom(JAVA_INT, 0);
        MemorySegment lpOverlapped = MemorySegment.NULL;
        int result = api.ReadFile(handle, lpBuffer, nNumberOfBytesToRead, lpNumberOfBytesRead, lpOverlapped);
        if (result == 0) {
            throwErrorMessage(arena, "ReadFile");
        }
        int read = lpNumberOfBytesRead.get(JAVA_INT, 0);
        dst.position(dst.position() + read);
        return read;
    }

    public int write(Arena arena, MemorySegment handle, ByteBuffer src) throws Throwable {
        MemorySegment lpBuffer = MemorySegment.ofBuffer(src);
        int nNumberOfBytesToWrite = src.remaining();
        MemorySegment lpNumberOfBytesWritten = arena.allocateFrom(JAVA_INT, 0);
        MemorySegment lpOverlapped = MemorySegment.NULL;
        int result = api.WriteFile(handle, lpBuffer, nNumberOfBytesToWrite, lpNumberOfBytesWritten, lpOverlapped);
        if (result == 0) {
            throwErrorMessage(arena, "WriteFile");
        }
        int written = lpNumberOfBytesWritten.get(JAVA_INT, 0);
        src.position(src.position() + written);
        return written;
    }

    public void close(Arena arena, MemorySegment handle) throws Throwable {
        int result = api.CloseHandle(handle);
        if (result == 0) {
            throwErrorMessage(arena, "CloseHandle");
        }
    }

    public void addIPAddress(Arena arena, int ifIndex, InetAddress address, InetAddress mask) throws Throwable {
        int Address = ByteBuffer.wrap(address.getAddress()).getInt();
        int IpMask = ByteBuffer.wrap(mask.getAddress()).getInt();
        MemorySegment NTEContext = arena.allocateFrom(JAVA_INT, 0);
        MemorySegment NTEInstance = arena.allocateFrom(JAVA_INT, 0);
        int result = (int) api.AddIPAddress.invokeExact(Address, IpMask, ifIndex, NTEContext, NTEInstance);
        if (result == 0) {
            throwErrorMessage(arena, "AddIPAddress");
        }
    }

    public void deleteIPAddress(Arena arena, int NTEContext) throws Throwable {
        int result = (int) api.DeleteIPAddress.invokeExact(NTEContext);
        if (result == 0) {
            throwErrorMessage(arena, "AddIPAddress");
        }
    }

    private void throwErrorMessage(Arena arena, String function) throws Throwable {
        int error = api.GetLastError();
        if (error != 0) {
            MemorySegment message = arena.allocate(10000L);
            int res = api.FormatMessageA(0x1000, MemorySegment.NULL, error, 0, message, (int) message.byteSize(), MemorySegment.NULL);
            if (res != 0) {
                String string = message.getString(0);
                throw new IOException("Failed to call " + function + ": " + error + " " + string);
            }
        }
        throw new IOException("Failed to call " + function + ".");
    }




}
