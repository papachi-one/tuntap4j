package one.papachi.tuntap4j.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class NativeWindowsApi {

    protected Arena arena;
    protected Linker linker;
    protected SymbolLookup lookup;
    protected MethodHandle GetLastError;
    protected MethodHandle FormatMessageA;
    protected MethodHandle GetHandleInformation;
    protected MethodHandle CreateFileA;
    protected MethodHandle ReadFile;
    protected MethodHandle WriteFile;
    protected MethodHandle CloseHandle;
    protected MethodHandle DeviceIoControl;

    NativeWindowsApi() throws Throwable {
        arena = Arena.ofShared();
        linker = Linker.nativeLinker();
        lookup = linker.defaultLookup()
                .or(SymbolLookup.libraryLookup("kernel32", arena))
                .or(SymbolLookup.libraryLookup("advapi32", arena))
                .or(SymbolLookup.libraryLookup("iphlpapi", arena));
        GetLastError = linker.downcallHandle(lookup.find("GetLastError").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT));
        FormatMessageA = linker.downcallHandle(lookup.find("FormatMessageA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        JAVA_INT.withName("dwFlags"),
                        ADDRESS.withName("lpSource"),
                        JAVA_INT.withName("dwMessageId"),
                        JAVA_INT.withName("dwLanguageId"),
                        ADDRESS.withName("lpBuffer"),
                        JAVA_INT.withName("nSize"),
                        ADDRESS.withName("Arguments")
                ));
        GetHandleInformation = linker.downcallHandle(lookup.find("GetHandleInformation").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hObject"),
                        ADDRESS.withTargetLayout(JAVA_INT).withName("lpdwFlags")
                )
        );
        CreateFileA = linker.downcallHandle(lookup.find("CreateFileA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("lpFileName"),
                        JAVA_INT.withName("dwDesiredAccess"),
                        JAVA_INT.withName("dwShareMode"),
                        ADDRESS.withName("lpSecurityAttributes"),
                        JAVA_INT.withName("dwCreationDisposition"),
                        JAVA_INT.withName("dwFlagsAndAttributes"),
                        ADDRESS.withName("hTemplateFile")
                ));
        ReadFile = linker.downcallHandle(lookup.find("ReadFile").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hFile"),
                        ADDRESS.withName("lpBuffer"),
                        JAVA_INT.withName("nNumberOfBytesToRead"),
                        ADDRESS.withName("lpNumberOfBytesRead"),
                        ADDRESS.withName("lpOverlapped")
                ));
        WriteFile = linker.downcallHandle(lookup.find("WriteFile").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hFile"),
                        ADDRESS.withName("lpBuffer"),
                        JAVA_INT.withName("nNumberOfBytesToWrite"),
                        ADDRESS.withName("lpNumberOfBytesWritten"),
                        ADDRESS.withName("lpOverlapped")
                ));
        CloseHandle = linker.downcallHandle(lookup.find("CloseHandle").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hObject")
                ));
        DeviceIoControl = linker.downcallHandle(lookup.find("DeviceIoControl").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hDevice"),
                        JAVA_INT.withName("dwIoControlCode"),
                        ADDRESS.withName("lpInBuffer"),
                        JAVA_INT.withName("nInBufferSize"),
                        ADDRESS.withName("lpOutBuffer"),
                        JAVA_INT.withName("nOutBufferSize"),
                        ADDRESS.withName("lpBytesReturned"),
                        ADDRESS.withName("lpOverlapped")
                ));
        RegOpenKeyA = linker.downcallHandle(lookup.find("RegOpenKeyA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        ADDRESS.withName("lpSubKey"),
                        ADDRESS.withName("phkResult")
                ));
        RegOpenKeyExA = linker.downcallHandle(lookup.find("RegOpenKeyExA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        ADDRESS.withName("lpSubKey"),
                        JAVA_INT.withName("ulOptions"),
                        JAVA_INT.withName("samDesired"),
                        ADDRESS.withName("phkResult")
                ));
        RegCloseKey = linker.downcallHandle(lookup.find("RegCloseKey").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey")
                ));
        RegQueryInfoKeyA = linker.downcallHandle(lookup.find("RegQueryInfoKeyA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        ADDRESS.withName("lpClass"),
                        ADDRESS.withName("lpcchClass"),
                        ADDRESS.withName("lpReserved"),
                        ADDRESS.withName("lpcSubKeys"),
                        ADDRESS.withName("lpcbMaxSubKeyLen"),
                        ADDRESS.withName("lpcbMaxClassLen"),
                        ADDRESS.withName("lpcValues"),
                        ADDRESS.withName("lpcbMaxValueNameLen"),
                        ADDRESS.withName("lpcbMaxValueLen"),
                        ADDRESS.withName("lpcbSecurityDescriptor"),
                        ADDRESS.withName("lpftLastWriteTime")

                ));
        RegEnumKeyA = linker.downcallHandle(lookup.find("RegEnumKeyA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        JAVA_INT.withName("dwIndex"),
                        ADDRESS.withName("lpName"),
                        JAVA_INT.withName("cchName")
                ));
        RegGetValueA = linker.downcallHandle(lookup.find("RegGetValueA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        ADDRESS.withName("lpSubKey"),
                        ADDRESS.withName("lpValue"),
                        JAVA_INT.withName("dwFlags"),
                        ADDRESS.withName("pdwType"),
                        ADDRESS.withName("pvData"),
                        ADDRESS.withName("pcbData")
                ));
        RegSetValueExA = linker.downcallHandle(lookup.find("RegSetValueExA").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_INT.withName("return"),
                        ADDRESS.withName("hKey"),
                        ADDRESS.withName("lpValueName"),
                        JAVA_INT.withName("Reserved"),
                        JAVA_INT.withName("dwType"),
                        ADDRESS.withName("lpData"),
                        JAVA_INT.withName("cbData")
                ));
    }

    MethodHandle RegOpenKeyA;
    MethodHandle RegOpenKeyExA;
    MethodHandle RegCloseKey;
    MethodHandle RegQueryInfoKeyA;
    MethodHandle RegEnumKeyA;
    MethodHandle RegGetValueA;
    MethodHandle RegSetValueExA;

    public int RegOpenKeyA(MemorySegment hKey, MemorySegment lpSubKey, MemorySegment phkResult) throws Throwable {
        return (int) RegOpenKeyA.invokeExact(hKey, lpSubKey, phkResult);
    }

    public int RegOpenKeyExA(MemorySegment hKey, MemorySegment lpSubKey, int ulOptions, int samDesired, MemorySegment phkResult) throws Throwable {
        return (int) RegOpenKeyExA.invokeExact(hKey, lpSubKey, ulOptions, samDesired, phkResult);
    }

    public int RegCloseKey(MemorySegment hKey) throws Throwable {
        return (int) RegCloseKey.invokeExact(hKey);
    }

    public int RegQueryInfoKeyA(MemorySegment hKey, MemorySegment lpClass, MemorySegment lpcchClass,
                                MemorySegment lpReserved, MemorySegment lpcSubKeys, MemorySegment lpcbMaxSubKeyLen,
                                MemorySegment lpcbMaxClassLen, MemorySegment lpcValue, MemorySegment lpcvMaxValueNameLen,
                                MemorySegment lpcbMaxValueLen, MemorySegment lpcbSecurityDescriptor,
                                MemorySegment lpftLastWriteTime) throws Throwable {
        return (int) RegQueryInfoKeyA.invokeExact(hKey, lpClass, lpcchClass, lpReserved, lpcSubKeys, lpcbMaxSubKeyLen,
                lpcbMaxClassLen, lpcValue, lpcvMaxValueNameLen, lpcbMaxValueLen, lpcbSecurityDescriptor, lpftLastWriteTime);
    }

    public int RegEnumKeyA(MemorySegment hKey, int dwIndex, MemorySegment lpName, int cchName) throws Throwable {
        return (int) RegEnumKeyA.invokeExact(hKey, dwIndex, lpName, cchName);
    }

    public int RegGetValueA(MemorySegment hKey, MemorySegment lpSubKey, MemorySegment lpValue, int dwFlags, MemorySegment pdwType, MemorySegment pvData, MemorySegment pcbData) throws Throwable {
        return (int) RegGetValueA.invokeExact(hKey, lpSubKey, lpValue, dwFlags, pdwType, pvData, pcbData);
    }

    public int RegSetValueExA(MemorySegment hKey, MemorySegment lpValueName, int Reserved, int dwType, MemorySegment lpData, int cbData) throws Throwable {
        return (int) RegSetValueExA.invokeExact(hKey, lpValueName, Reserved, dwType, lpData, cbData);
    }

    public int GetLastError() throws Throwable {
        return (int) GetLastError.invokeExact();
    }

    public int FormatMessageA(int dwFlags, MemorySegment lpSource, int dwMessageId, int dwLanguageId, MemorySegment lpBuffer, int nSize, MemorySegment Arguments) throws Throwable {
        return (int) FormatMessageA.invokeExact(dwFlags, lpSource, dwMessageId, dwLanguageId, lpBuffer, nSize, Arguments);
    }

    public int GetHandleInformation(MemorySegment hObject, MemorySegment lpdwFlags) throws Throwable {
        return (int) GetHandleInformation.invokeExact(hObject, lpdwFlags);
    }

    public int CreateFileA(MemorySegment lpFileName, int dwDesiredAccess, int dwShareMode, MemorySegment lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes, MemorySegment hTemplateFile) throws Throwable {
        return (int) CreateFileA.invokeExact(lpFileName, dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
    }

    public int ReadFile(MemorySegment hFile, MemorySegment lpBuffer, int nNumberOfBytesToRead, MemorySegment lpNumberOfBytesRead, MemorySegment lpOverlapped) throws Throwable {
        return (int) ReadFile.invokeExact(hFile, lpBuffer, nNumberOfBytesToRead, lpNumberOfBytesRead, lpOverlapped);
    }

    public int WriteFile(MemorySegment hFile, MemorySegment lpBuffer, int nNumberOfBytesToWrite, MemorySegment lpNumberOfBytesWritten, MemorySegment lpOverlapped) throws Throwable {
        return (int) WriteFile.invokeExact(hFile, lpBuffer, nNumberOfBytesToWrite, lpNumberOfBytesWritten, lpOverlapped);
    }

    public int CloseHandle(MemorySegment hObject) throws Throwable {
        return (int) CloseHandle.invokeExact(hObject);
    }

    public int DeviceIoControl(MemorySegment hDevice, int dwIoControlCode, MemorySegment lpInBuffer, int nInBufferSize, MemorySegment lpOutBuffer, int nOutBufferSize, MemorySegment lpBytesReturned, MemorySegment lpOverlapped) throws Throwable {
        return (int) DeviceIoControl.invokeExact(hDevice, dwIoControlCode, lpInBuffer, nInBufferSize, lpOutBuffer, nOutBufferSize, lpBytesReturned, lpOverlapped);
    }


    {

        // getIPAddress
//        MethodHandle GetAdaptersInfo;

        // setIPAddress
//        MethodHandle AddIPAddress;
//        MethodHandle DeleteIPAddress;

        // getMTU

        // setMTU

        // getMACAddress

        // setMACAddress

//        RegGetValueA
//        RegEnumKeyA
//        RegQueryInfoKeyA
//        RegOpenKeyA
//        RegCloseKey
//        RegOpenKeyExA
//        RegGetValueA

//        AddIPAddress
//        DeleteIPAddress
//        GetAdaptersInfo

//        mbstowcs
//        CLSIDFromString
//        ConvertInterfaceGuidToLuid
//        ConvertInterfaceLuidToIndex

    }

}
