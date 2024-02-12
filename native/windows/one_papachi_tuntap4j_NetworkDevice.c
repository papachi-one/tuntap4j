#define _CRT_SECURE_NO_WARNINGS
#include "one_papachi_tuntap4j_NetworkDevice.h"

#include <stdio.h>
#include <Windows.h>
#include <winnt.h>
#include <winreg.h>
#include <string.h>
#include <iphlpapi.h>
#pragma comment(lib, "iphlpapi.lib")
#include <netioapi.h>
#include <fcntl.h>

#define TAP_WIN_IOCTL_SET_MEDIA_STATUS 2228248
#define TAP_WIN_IOCTL_GET_MAC 2228228
#define ADAPTER_KEY "SYSTEM\\CurrentControlSet\\Control\\Class\\{4D36E972-E325-11CE-BFC1-08002BE10318}"
// #define NETWORK_KEY "SYSTEM\\CurrentControlSet\\Control\\Network\\{4D36E972-E325-11CE-BFC1-08002BE10318}"

char* concat(const char *s1, const char *s2) {
    const size_t len1 = strlen(s1);
    const size_t len2 = strlen(s2);
    char *result = malloc(len1 + len2 + 1);
    memcpy(result, s1, len1);
    memcpy(result + len1, s2, len2 + 1);
    return result;
}

void throwException(JNIEnv *env, const char *exceptionClassName, DWORD error) {
    LPTSTR message;
    FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL, error, 0, (LPSTR) &message, 0, NULL);
    message[strlen(message) - 2] = '\0';
    jclass exceptionClass;
    exceptionClass = (*env)->FindClass(env, exceptionClassName);
    (*env)->ThrowNew(env, exceptionClass, message);
    free(message);
}

NET_IFINDEX getIfIndex(JNIEnv *env, const char *deviceName) {
    WCHAR wDeviceName[39];
    mbstowcs(wDeviceName, deviceName, 39);
    wDeviceName[38] = 0;
    const WCHAR *guidString = wDeviceName;
    GUID guid;
    HRESULT hResult = CLSIDFromString(guidString, (LPCLSID)&guid);
    if (hResult != S_OK) {
        throwException(env, "java/lang/IllegalArgumentException", hResult);
        return -1;
    }
    NET_LUID luid;
    ConvertInterfaceGuidToLuid(&guid, &luid);
    NET_IFINDEX ifIndex;
    ConvertInterfaceLuidToIndex(&luid, &ifIndex);
    return ifIndex;
}

ULONG getAddr(JNIEnv *env, const char *input) {
    typedef LONG (NTAPI *RtlIpv4StringToAddressA)(PCSTR, BOOLEAN, PCSTR *, struct in_addr *);
    RtlIpv4StringToAddressA pRtlIpv4StringToAddressA = (RtlIpv4StringToAddressA)GetProcAddress(GetModuleHandle("ntdll.dll"), "RtlIpv4StringToAddressA");
    struct in_addr addr;
    char c;
    char *cc = &c;
    NTSTATUS status = pRtlIpv4StringToAddressA(input, TRUE, &cc, &addr);
    if (status) {
        typedef LONG (NTAPI *RtlNtStatusToDosError)(NTSTATUS);
        RtlNtStatusToDosError pRtlNtStatusToDosError = (RtlNtStatusToDosError)GetProcAddress(GetModuleHandle("ntdll.dll"), "RtlNtStatusToDosError");
        DWORD error = pRtlNtStatusToDosError(status);
        throwException(env, "java/lang/IllegalArgumentException", error);
        return -1;
    }
    ULONG addrI = addr.S_un.S_addr;
    return addrI;
}

JNIEXPORT jboolean JNICALL Java_one_papachi_tuntap4j_NetworkDevice_isOpen(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    INVALID_HANDLE_VALUE
    DWORD flags;
    return GetHandleInformation((HANDLE) deviceHandle, &flags) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_one_papachi_tuntap4j_NetworkDevice_open(JNIEnv *env, jclass class, jstring deviceName, jboolean isTAP) {
    if (isTAP == JNI_FALSE) {
        throwException(env, "java/lang/UnsupportedOperationException", ERROR_CALL_NOT_IMPLEMENTED);
        return -1;
    }
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    char *tmp1 = concat("\\\\.\\", _deviceName);
    char *tmp2 = concat(tmp1, ".tap");
    HANDLE handle = CreateFileA(tmp2, GENERIC_READ | GENERIC_WRITE, 0, 0, OPEN_EXISTING, FILE_ATTRIBUTE_SYSTEM, 0);
    free(tmp1);
    free(tmp2);
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
    if (handle == INVALID_HANDLE_VALUE) {
        DWORD error = GetLastError();
        printf("%d\n", error);
        throwException(env, "java/io/IOException", error);
    }
    return (jlong) handle;
}

JNIEXPORT jint JNICALL Java_one_papachi_tuntap4j_NetworkDevice_read(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jobject dst) {
    jclass class1 = (*env)->GetObjectClass(env, dst);
    jmethodID methodIsDirect = (*env)->GetMethodID(env, class1, "isDirect", "()Z");
    jboolean isDirect = (*env)->CallBooleanMethod(env, dst, methodIsDirect);
    jmethodID methodIdGetPosition = (*env)->GetMethodID(env, class1, "position", "()I");
    jint position = (*env)->CallIntMethod(env, dst, methodIdGetPosition);
    jmethodID methodIdRemaining = (*env)->GetMethodID(env, class1, "remaining", "()I");
    jint remaining = (*env)->CallIntMethod(env, dst, methodIdRemaining);
    jint bytesRead = 0;
    if (isDirect == JNI_TRUE) {
        jbyte *_dst = (*env)->GetDirectBufferAddress(env, dst);
        if (ReadFile((HANDLE) deviceHandle, _dst + position, remaining, &bytesRead, NULL) != FALSE) {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, dst, methodIdSetPosition, position + bytesRead);
        } else {
            throwException(env, "java/io/IOException", GetLastError());
        }
    } else {
        jmethodID methodArray = (*env)->GetMethodID(env, class1, "array", "()[B");
        jmethodID methodArrayOffset = (*env)->GetMethodID(env, class1, "arrayOffset", "()I");
        jbyteArray array = (*env)->CallObjectMethod(env, dst, methodArray);
        jint arrayOffset = (*env)->CallIntMethod(env, dst, methodArrayOffset);
        jboolean isCopy;
        jbyte *byteArray = (*env)->GetByteArrayElements(env, array, &isCopy);
        if (ReadFile((HANDLE) deviceHandle, byteArray + arrayOffset + position, remaining, &bytesRead, NULL) != FALSE) {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, dst, methodIdSetPosition, position + bytesRead);
        } else {
            throwException(env, "java/io/IOException", GetLastError());
        }
        (*env)->ReleaseByteArrayElements(env, array, byteArray, 0);
    }
    return bytesRead;
}

JNIEXPORT jint JNICALL Java_one_papachi_tuntap4j_NetworkDevice_write(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jobject src) {
    jclass class1 = (*env)->GetObjectClass(env, src);
    jmethodID methodIsDirect = (*env)->GetMethodID(env, class1, "isDirect", "()Z");
    jboolean isDirect = (*env)->CallBooleanMethod(env, src, methodIsDirect);
    jmethodID methodIdGetPosition = (*env)->GetMethodID(env, class1, "position", "()I");
    jint position = (*env)->CallIntMethod(env, src, methodIdGetPosition);
    jmethodID methodIdRemaining = (*env)->GetMethodID(env, class1, "remaining", "()I");
    jint remaining = (*env)->CallIntMethod(env, src, methodIdRemaining);
    jint bytesWritten = 0;
    if (isDirect == JNI_TRUE) {
        const jbyte *_src = (*env)->GetDirectBufferAddress(env, src);
        if (WriteFile((HANDLE) deviceHandle, _src + position, remaining, &bytesWritten, NULL) != FALSE) {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, src, methodIdSetPosition, position + bytesWritten);
        } else {
            throwException(env, "java/io/IOException", GetLastError());
        }
    } else {
        jmethodID methodArray = (*env)->GetMethodID(env, class1, "array", "()[B");
        jmethodID methodArrayOffset = (*env)->GetMethodID(env, class1, "arrayOffset", "()I");
        jbyteArray array = (*env)->CallObjectMethod(env, src, methodArray);
        jint arrayOffset = (*env)->CallIntMethod(env, src, methodArrayOffset);
        jboolean isCopy;
        jbyte *byteArray = (*env)->GetByteArrayElements(env, array, &isCopy);
        if (WriteFile((HANDLE) deviceHandle, byteArray + arrayOffset + position, remaining, &bytesWritten, NULL) != FALSE) {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, src, methodIdSetPosition, position + bytesWritten);
        } else {
            throwException(env, "java/io/IOException", GetLastError());
        }
        (*env)->ReleaseByteArrayElements(env, array, byteArray, 0);
    }
    return bytesWritten;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_close(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    if (CloseHandle((HANDLE) deviceHandle) == FALSE) {
        throwException(env, "java/io/IOException", GetLastError());
    }
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setStatus(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jboolean isUp) {
    ULONG _isUp = isUp == JNI_TRUE ? 1 : 0;
    DWORD len;
    if (DeviceIoControl((HANDLE) deviceHandle, TAP_WIN_IOCTL_SET_MEDIA_STATUS, &_isUp, sizeof _isUp, &_isUp, sizeof _isUp, &len, NULL) == FALSE) {
        throwException(env, "java/io/IOException", GetLastError());
    }
}

JNIEXPORT jstring JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getIPAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    jstring string = NULL;
    NET_IFINDEX ifIndex = getIfIndex(env, _deviceName);
    if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
        ULONG ulOutBufLen = sizeof (IP_ADAPTER_INFO);
        PIP_ADAPTER_INFO pAdapterInfo = (IP_ADAPTER_INFO *) malloc(sizeof (IP_ADAPTER_INFO));
        if (GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
            free(pAdapterInfo);
            pAdapterInfo = (IP_ADAPTER_INFO *) malloc(ulOutBufLen);
        }
        ULONG error = GetAdaptersInfo(pAdapterInfo, &ulOutBufLen);
        if (error) {
            throwException(env, "java/io/IOException", error);
        } else {
            PIP_ADAPTER_INFO pAdapter = pAdapterInfo;
            while (pAdapter) {
                if (pAdapter->Index == ifIndex) {
                    PIP_ADDR_STRING addr = &pAdapter->IpAddressList;
                    const char *address = addr->IpAddress.String;
                    const char *mask = addr->IpMask.String;
                    char *tmp1 = concat(address, "/");
                    char *tmp2 = concat(tmp1, mask);
                    string = (*env)->NewStringUTF(env, tmp2);
                    free(tmp1);
                    free(tmp2);
                }
                pAdapter = pAdapter->Next;
            }
            free(pAdapterInfo);
        }
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
    if (string == NULL) {
        string = (*env)->NewStringUTF(env, "0.0.0.0/0.0.0.0");
    }
    return string;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setIPAddress(JNIEnv *env, jclass class1, jstring deviceName, jlong deviceHandle, jstring address, jstring mask) {
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    const char *_address = (*env)->GetStringUTFChars(env, address, NULL);
    const char *_mask = (*env)->GetStringUTFChars(env, mask, NULL);
    NET_IFINDEX ifIndex = getIfIndex(env, _deviceName);
    if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
        ULONG Address = getAddr(env, _address);
        if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
            ULONG Mask = getAddr(env, _mask);
            if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
                ULONG NTEContext = 0;
                ULONG NTEInstance = 0;
                ULONG ulOutBufLen = sizeof (IP_ADAPTER_INFO);
                PIP_ADAPTER_INFO pAdapterInfo = (IP_ADAPTER_INFO *) malloc(sizeof (IP_ADAPTER_INFO));
                if (GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
                    free(pAdapterInfo);
                    pAdapterInfo = (IP_ADAPTER_INFO *) malloc(ulOutBufLen);
                }
                ULONG error = GetAdaptersInfo(pAdapterInfo, &ulOutBufLen);
                if (error) {
                    throwException(env, "java/io/IOException", error);
                } else {
                    PIP_ADAPTER_INFO pAdapter = pAdapterInfo;
                    while (pAdapter) {
                        if (pAdapter->Index == ifIndex) {
                            PIP_ADDR_STRING addr = &pAdapter->IpAddressList;
                            while (addr) {
                                DeleteIPAddress(addr->Context);
                                addr = addr->Next;
                            }
                        }
                        pAdapter = pAdapter->Next;
                    }
                    free(pAdapterInfo);
                    error = AddIPAddress(Address, Mask, ifIndex, &NTEContext, &NTEInstance);
                    if (error) {
                        throwException(env, "java/io/IOException", error);
                    }
                }
            }
        }
    }
    (*env)->ReleaseStringUTFChars(env, mask, _mask);
    (*env)->ReleaseStringUTFChars(env, address, _address);
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
}

JNIEXPORT jint JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getMTU(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    jint mtu = 1500;
    const char *_netCfgInstanceId = (*env)->GetStringUTFChars(env, deviceName, NULL);
    const char adapterKey[] = ADAPTER_KEY;
    HKEY hKey;
    LSTATUS status;
    if ((status = RegOpenKeyA(HKEY_LOCAL_MACHINE, adapterKey, &hKey)) == ERROR_SUCCESS) {
        DWORD lpcSubKeys = 0;
        DWORD lpcMaxSubKeyLen = 0;
        if ((status = RegQueryInfoKeyA(hKey, NULL, NULL, NULL, &lpcSubKeys, &lpcMaxSubKeyLen, NULL, NULL, NULL, NULL, NULL, NULL)) == ERROR_SUCCESS) {
            char name[255];
            DWORD nameLength = 255;
            for (DWORD i = 0; i < lpcSubKeys; i++) {
                RegEnumKeyA(hKey, i, name, nameLength);
                DWORD dataTypeNetCfgInstanceId;
                char valueNetCfgInstanceId[255];
                PVOID pvDataNetCfgInstanceId = valueNetCfgInstanceId;
                DWORD sizeNetCfgInstanceId = sizeof(valueNetCfgInstanceId);
                if ((status = RegGetValueA(hKey, name, "NetCfgInstanceId", RRF_RT_REG_SZ | RRF_ZEROONFAILURE, &dataTypeNetCfgInstanceId, pvDataNetCfgInstanceId, &sizeNetCfgInstanceId)) == ERROR_SUCCESS) {
                    if (strcmp(valueNetCfgInstanceId, _netCfgInstanceId) == 0) {
                        DWORD dataTypeMTU;
                        char valueMTU[255];
                        PVOID pvDataMTU = valueMTU;
                        DWORD sizeMTU = sizeof(valueMTU);
                        if ((status = RegGetValueA(hKey, name, "MTU", RRF_RT_REG_SZ | RRF_ZEROONFAILURE, &dataTypeMTU, pvDataMTU, &sizeMTU)) == ERROR_SUCCESS) {
                            mtu = atoi(valueMTU);
                            break;
                        } else {
                            throwException(env, "java/io/IOException", status);
                            break;
                        }
                    }
                } else {
                    throwException(env, "java/io/IOException", status);
                    break;
                }
            }
        } else {
            throwException(env, "java/io/IOException", status);
        }
        RegCloseKey(hKey);
    } else {
        throwException(env, "java/io/IOException", status);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _netCfgInstanceId);
    return mtu;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setMTU(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jint mtu) {
    const char *_netCfgInstanceId = (*env)->GetStringUTFChars(env, deviceName, NULL);
    const char adapterKey[] = ADAPTER_KEY;
    char _mtu[5];
    _itoa_s(mtu, _mtu, 5, 10);
    HKEY hKey;
    LSTATUS status;
    if ((status = RegOpenKeyA(HKEY_LOCAL_MACHINE, adapterKey, &hKey)) == ERROR_SUCCESS) {
        DWORD lpcSubKeys = 0;
        DWORD lpcMaxSubKeyLen = 0;
        if ((status = RegQueryInfoKeyA(hKey, NULL, NULL, NULL, &lpcSubKeys, &lpcMaxSubKeyLen, NULL, NULL, NULL, NULL, NULL, NULL)) == ERROR_SUCCESS) {
            char name[255];
            DWORD nameLength = 255;
            for (DWORD i = 0; i < lpcSubKeys; i++) {
                RegEnumKeyA(hKey, i, name, nameLength);
                DWORD dataTypeNetCfgInstanceId;
                char valueNetCfgInstanceId[255];
                PVOID pvDataNetCfgInstanceId = valueNetCfgInstanceId;
                DWORD sizeNetCfgInstanceId = sizeof(valueNetCfgInstanceId);
                if ((status = RegGetValueA(hKey, name, "NetCfgInstanceId", RRF_RT_REG_SZ | RRF_ZEROONFAILURE, &dataTypeNetCfgInstanceId, pvDataNetCfgInstanceId, &sizeNetCfgInstanceId)) == ERROR_SUCCESS) {
                    if (strcmp(valueNetCfgInstanceId, _netCfgInstanceId) == 0) {
                        char * tmp1 = concat(adapterKey, "\\");
                        char * tmp2 = concat(tmp1, name);
                        HKEY hKey2;
                        if ((status = RegOpenKeyExA(HKEY_LOCAL_MACHINE, tmp2, 0, KEY_SET_VALUE, &hKey2)) == ERROR_SUCCESS) {
                            RegSetValueExA(hKey2, "MTU", 0, REG_SZ, _mtu, (DWORD) (strlen(_mtu) + 1));
                            RegCloseKey(hKey2);
                        } else {
                            throwException(env, "java/io/IOException", status);
                        }
                        free(tmp1);
                        free(tmp2);
                    }
                } else {
                    throwException(env, "java/io/IOException", status);
                }
            }
        } else {
            throwException(env, "java/io/IOException", status);
        }
        RegCloseKey(hKey);
    } else {
        throwException(env, "java/io/IOException", status);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _netCfgInstanceId);
}

JNIEXPORT jbyteArray JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getMACAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    jbyteArray array = NULL;
    char macAddress[] = {0, 0, 0, 0, 0, 0};
    DWORD len;
    if (DeviceIoControl((HANDLE) deviceHandle, TAP_WIN_IOCTL_GET_MAC, 0, 0, &macAddress, 6, &len, NULL) == FALSE) {
        throwException(env, "java/io/IOException", GetLastError());
    } else {
        array = (*env)->NewByteArray(env, 6);
        (*env)->SetByteArrayRegion(env, array, 0, 6, macAddress);
    }
    return array;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setMACAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jbyteArray mac) {
    throwException(env, "java/lang/UnsupportedOperationException", ERROR_CALL_NOT_IMPLEMENTED);
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getAvailableTunDevices(JNIEnv *env, jclass _class) {
    jclass class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID constructor = (*env)->GetMethodID(env, class, "<init>", "()V");
    jobject result = (*env)->NewObject(env, class, constructor);
    return result;
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getAvailableTapDevices(JNIEnv *env, jclass _class) {
    jclass class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID constructor = (*env)->GetMethodID(env, class, "<init>", "()V");
    jmethodID add = (*env)->GetMethodID(env, class, "add", "(Ljava/lang/Object;)Z");
    jobject result = (*env)->NewObject(env, class, constructor);
    const char adapterKey[] = ADAPTER_KEY;
    HKEY hKey;
    if (RegOpenKeyA(HKEY_LOCAL_MACHINE, adapterKey, &hKey) == ERROR_SUCCESS) {
        DWORD lpcSubKeys = 0;
        DWORD lpcMaxSubKeyLen = 0;
        if (RegQueryInfoKeyA(hKey, NULL, NULL, NULL, &lpcSubKeys, &lpcMaxSubKeyLen, NULL, NULL, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
            char name[255];
            DWORD nameLength = 255;
            for (DWORD i = 0; i < lpcSubKeys; i++) {
                if (RegEnumKeyA(hKey, i, name, nameLength) == ERROR_SUCCESS) {
                    DWORD dataType;
                    char value[255];
                    PVOID pvData = value;
                    DWORD size = sizeof(value);
                    if (RegGetValueA(hKey, name, "ComponentId", RRF_RT_REG_SZ | RRF_ZEROONFAILURE, &dataType, pvData, &size) == ERROR_SUCCESS) {
                        if (strcmp(value, "tap0901") == 0) {
                            DWORD dataTypeNetCfgInstanceId;
                            char valueNetCfgInstanceId[255];
                            PVOID pvDataNetCfgInstanceId = valueNetCfgInstanceId;
                            DWORD sizeNetCfgInstanceId = sizeof(valueNetCfgInstanceId);
                            if (RegGetValueA(hKey, name, "NetCfgInstanceId", RRF_RT_REG_SZ | RRF_ZEROONFAILURE, &dataTypeNetCfgInstanceId, pvDataNetCfgInstanceId, &sizeNetCfgInstanceId) == ERROR_SUCCESS) {
                                char *tmp1 = concat("\\\\.\\", pvDataNetCfgInstanceId);
                                char *tmp2 = concat(tmp1, ".tap");
                                HANDLE handle = CreateFileA(tmp2, GENERIC_READ | GENERIC_WRITE, 0, 0, OPEN_EXISTING, FILE_ATTRIBUTE_SYSTEM, 0);
                                if (handle != INVALID_HANDLE_VALUE) {
                                    jstring netCfgInstanceId = (*env)->NewStringUTF(env, valueNetCfgInstanceId);
                                    (*env)->CallBooleanMethod(env, result, add, netCfgInstanceId);
                                }
                                free(tmp1);
                                free(tmp2);
                                CloseHandle(handle);
                            }
                        }
                    }
                }
            }
        }
        RegCloseKey(hKey);
    }
    return result;
}
