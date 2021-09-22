#include "one_papachi_tuntap4j_NetworkDevice.h"

#include <unistd.h>
#include <sys/fcntl.h>
#include <sys/ioctl.h>
#include <string.h>
#include <errno.h>
#include <net/if.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <net/if_dl.h>
#include <stdlib.h>
#include <arpa/inet.h>

#include <sys/kern_event.h>
#include <sys/kern_control.h>
#include <ctype.h>

#define UTUN_CONTROL_NAME "com.apple.net.utun_control"
#define UTUN_OPT_IFNAME 2

struct utunDevice {
    jlong fd;
    char *ifName;
    struct utunDevice *Next;
};

struct utunDevice *list = NULL;

void add(struct utunDevice *device) {
    if (list == NULL) {
        list = device;
    } else {
        struct utunDevice *item = list;
        while (item) {
            if (item->Next == NULL) {
                item->Next = device;
                break;
            }
            item = item->Next;
        }
    }
}

void delete(jlong deviceHandle) {
    if (list != NULL) {
        struct utunDevice *item = list;
        struct utunDevice *previous = NULL;
        while (item) {
            if (item->fd == deviceHandle) {
                if (previous == NULL) {
                    list = item->Next;
                } else {
                    previous->Next = item->Next;
                }
                free(item->ifName);
                free(item);
                break;
            }
            previous = item;
            item = item->Next;
        }
    }
}

char * getDeviceName(jlong deviceHandle) {
    char *deviceName = NULL;
    if (list != NULL) {
        struct utunDevice *item = list;
        while (item) {
            if (item->fd == deviceHandle) {
                deviceName = item->ifName;
                break;
            }
            item = item->Next;
        }
    }
    return deviceName;
}

char* concat(const char *s1, const char *s2) {
    const size_t len1 = strlen(s1);
    const size_t len2 = strlen(s2);
    char *result = (char*) malloc(len1 + len2 + 1);
    memcpy(result, s1, len1);
    memcpy(result + len1, s2, len2 + 1);
    return result;
}

void throwException(JNIEnv *env, const char *exceptionClassName, int error) {
    jclass exceptionClass;
    exceptionClass = (*env)->FindClass(env, exceptionClassName);
    (*env)->ThrowNew(env, exceptionClass, strerror(error));
}

JNIEXPORT jboolean JNICALL Java_one_papachi_tuntap4j_NetworkDevice_isOpen(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    return fcntl(deviceHandle, F_GETFD) != -1 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_one_papachi_tuntap4j_NetworkDevice_open(JNIEnv *env, jclass class, jstring deviceName, jboolean isTAP) {
    int fd = -1;
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    if (isTAP == JNI_TRUE) {
        char *devicePath = concat("/dev/", _device);
        if ((fd= open(devicePath, O_RDWR)) == -1) {
            throwException(env, "java/io/IOException", errno);
        }
        free(devicePath);
    } else {
        if ((fd = socket(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL)) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            struct ctl_info info;
            bzero(&info, sizeof info);
            strncpy(info.ctl_name, UTUN_CONTROL_NAME, MAX_KCTL_NAME);
            if (ioctl(fd, CTLIOCGINFO, &info) == -1) {
                close(fd);
                fd = -1;
                throwException(env, "java/io/IOException", errno);
            } else {
                struct sockaddr_ctl addr;
                addr.sc_len = sizeof(addr);
                addr.sc_family = AF_SYSTEM;
                addr.ss_sysaddr = AF_SYS_CONTROL;
                addr.sc_id = info.ctl_id;
                addr.sc_unit = atoi(_device);
                if (connect(fd, (struct sockaddr *) &addr, sizeof addr) == -1) {
                    close(fd);
                    fd = -1;
                    throwException(env, "java/io/IOException", errno);
                } else {
                    int utunname_len = 255;
                    char* utunname = (char*) malloc(sizeof(char) * (utunname_len + 1));
                    if (getsockopt(fd, SYSPROTO_CONTROL, UTUN_OPT_IFNAME, utunname, (socklen_t*) &utunname_len) == -1) {
                        close(fd);
                        fd = -1;
                        free(utunname);
                        throwException(env, "java/io/IOException", errno);
                    } else {
                        struct utunDevice *device = (struct utunDevice*) malloc(sizeof(struct utunDevice));
                        device->fd = fd;
                        device->ifName = utunname;
                        device->Next = NULL;
                        add(device);
                    }
                }
            }
        }
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
    return (jlong) fd;
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
        if ((bytesRead = read(deviceHandle, _dst, remaining)) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, dst, methodIdSetPosition, position + bytesRead);
        }
    } else {
        jmethodID methodArray = (*env)->GetMethodID(env, class1, "array", "()[B");
        jmethodID methodArrayOffset = (*env)->GetMethodID(env, class1, "arrayOffset", "()I");
        jbyteArray array = (*env)->CallObjectMethod(env, dst, methodArray);
        jint arrayOffset = (*env)->CallIntMethod(env, dst, methodArrayOffset);
        jboolean isCopy;
        jbyte *byteArray = (*env)->GetByteArrayElements(env, array, &isCopy);
        if ((bytesRead = read(deviceHandle, byteArray + arrayOffset + position, remaining)) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, dst, methodIdSetPosition, position + bytesRead);
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
        if ((bytesWritten = write(deviceHandle, _src, remaining)) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, src, methodIdSetPosition, position + bytesWritten);
        }
    } else {
        jmethodID methodArray = (*env)->GetMethodID(env, class1, "array", "()[B");
        jmethodID methodArrayOffset = (*env)->GetMethodID(env, class1, "arrayOffset", "()I");
        jbyteArray array = (*env)->CallObjectMethod(env, src, methodArray);
        jint arrayOffset = (*env)->CallIntMethod(env, src, methodArrayOffset);
        jboolean isCopy;
        jbyte *byteArray = (*env)->GetByteArrayElements(env, array, &isCopy);
        if ((bytesWritten = write(deviceHandle, byteArray + arrayOffset + position, remaining)) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            jmethodID methodIdSetPosition = (*env)->GetMethodID(env, class1, "position", "(I)Ljava/nio/Buffer;");
            (*env)->CallObjectMethod(env, src, methodIdSetPosition, position + bytesWritten);
        }
        (*env)->ReleaseByteArrayElements(env, array, byteArray, 0);
    }
    return bytesWritten;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_close(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    delete(deviceHandle);
    if (close(deviceHandle) == -1) {
        throwException(env, "java/io/IOException", errno);
    }
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setStatus(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jboolean isUp) {
    const char *device = getDeviceName(deviceHandle);
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, device != NULL ? device : _device, IFNAMSIZ);
    ifr.ifr_flags = JNI_TRUE == isUp ? IFF_UP : 0;
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCGIFFLAGS, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            if (isUp == JNI_TRUE) {
                ifr.ifr_ifru.ifru_flags |= IFF_UP;
            } else {
                ifr.ifr_ifru.ifru_flags &= ~IFF_UP;
            }
            if (ioctl(sockfd, SIOCSIFFLAGS, &ifr) == -1) {
                throwException(env, "java/io/IOException", errno);
            }
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
}

// /sbin/ifconfig <utun0>

JNIEXPORT jint JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getMTU(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    jint result = -1;
    const char *device = getDeviceName(deviceHandle);
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, device != NULL ? device : _device, IFNAMSIZ);
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCGIFMTU, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            result = ifr.ifr_ifru.ifru_mtu;
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
    return result;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setMTU(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jint mtu) {
    const char *device = getDeviceName(deviceHandle);
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, device != NULL ? device : _device, IFNAMSIZ);
    ifr.ifr_ifru.ifru_mtu = mtu;
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCSIFMTU, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
}

JNIEXPORT jstring JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getIPAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    const char *device = getDeviceName(deviceHandle);
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    jstring string = NULL;
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, device != NULL ? device :  _deviceName, IFNAMSIZ);
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        ifr.ifr_addr.sa_family = AF_INET;
        if (ioctl(sockfd, SIOCGIFADDR, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            struct sockaddr_in *Address = (struct sockaddr_in *)&(ifr.ifr_addr);
            const char *address = inet_ntoa(Address->sin_addr);
            char *tmp1 = concat(address, "/");
            if (ioctl(sockfd, SIOCGIFNETMASK, &ifr) == -1) {
                throwException(env, "java/io/IOException", errno);
            } else {
                struct sockaddr_in *Mask = (struct sockaddr_in *)&(ifr.ifr_addr);
                const char *mask = inet_ntoa(Mask->sin_addr);
                char *tmp2 = concat(tmp1, mask);
                string = (*env)->NewStringUTF(env, tmp2);
                free(tmp2);
            }
            free(tmp1);
        }
        close(sockfd);
    }
    if (string == NULL) {
        string = (*env)->NewStringUTF(env, "0.0.0.0/0.0.0.0");
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
    return string;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setIPAddress(JNIEnv *env, jclass class1, jstring deviceName, jlong deviceHandle, jstring address, jstring mask) {
    const char *device = getDeviceName(deviceHandle);
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    const char *_address = (*env)->GetStringUTFChars(env, address, NULL);
    const char *_mask = (*env)->GetStringUTFChars(env, mask, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, device != NULL ? device : _deviceName, IFNAMSIZ);
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        ifr.ifr_addr.sa_family = AF_INET;
        struct sockaddr_in* addr = (struct sockaddr_in*) &ifr.ifr_addr;
        inet_pton(AF_INET, _address, &addr->sin_addr);
        if (ioctl(sockfd, SIOCSIFADDR, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            inet_pton(AF_INET, _mask, &addr->sin_addr);
            if (ioctl(sockfd, SIOCSIFNETMASK, &ifr) == -1) {
                throwException(env, "java/io/IOException", errno);
            }
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, mask, _mask);
    (*env)->ReleaseStringUTFChars(env, address, _address);
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
}

JNIEXPORT jbyteArray JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getMACAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    jbyteArray mac = (*env)->NewByteArray(env, 6);
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    int mib[6];
    size_t len;
    char *buf;
    unsigned char *ptr;
    struct if_msghdr *ifm;
    struct sockaddr_dl *sdl;
    mib[0] = CTL_NET;
    mib[1] = AF_ROUTE;
    mib[2] = 0;
    mib[3] = AF_LINK;
    mib[4] = NET_RT_IFLIST;
    mib[5] = if_nametoindex("tap0");
    if (sysctl(mib, 6, NULL, &len, NULL, 0) == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if ((buf = malloc(len)) == NULL) {
            throwException(env, "java/io/IOException", errno);
        } else {
            if (sysctl(mib, 6, buf, &len, NULL, 0) < 0) {
                throwException(env, "java/io/IOException", errno);
            } else {
                ifm = (struct if_msghdr *)buf;
                sdl = (struct sockaddr_dl *)(ifm + 1);
                ptr = (unsigned char *)LLADDR(sdl);
                (*env)->SetByteArrayRegion(env, mac, 0, 6, (const jbyte *)ptr);
            }
        }
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
    return mac;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setMACAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jbyteArray mac) {
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
    (*env)->GetByteArrayRegion(env, mac, 0, 6, (jbyte *) &ifr.ifr_ifru.ifru_addr.sa_data);
    ifr.ifr_ifru.ifru_addr.sa_len = 6;
    ifr.ifr_ifru.ifru_addr.sa_family = AF_LINK;
    int sockfd = socket(AF_LOCAL, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCSIFLLADDR, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        }
    }
    close(sockfd);
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_nativeList(JNIEnv *env, jclass class) {
    throwException(env, "java/lang/UnsupportedOperationException", ENOSYS);
    return NULL;
}
