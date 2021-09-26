#include "one_papachi_tuntap4j_NetworkDevice.h"

#include <sys/ioctl.h>
#include <fcntl.h>
#include <string.h>
#include <linux/if.h>
#include <linux/if_tun.h>
#include <unistd.h>
#include <errno.h>
#include <net/if_arp.h>
#include <arpa/inet.h>
#include <malloc.h>
#include <dirent.h>
#include <sys/stat.h>

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
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    int fd;
    if ((fd = open("/dev/net/tun", O_RDWR)) == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        struct ifreq ifr;
        memset(&ifr, 0, sizeof(ifr));
        strncpy(ifr.ifr_name, _device, IFNAMSIZ);
        if (isTAP == JNI_TRUE) {
            ifr.ifr_flags = IFF_TAP | IFF_NO_PI;
        } else {
            ifr.ifr_flags = IFF_TUN | IFF_NO_PI;
        }
        if (ioctl(fd, TUNSETIFF, &ifr) == -1) {
            close(fd);
            throwException(env, "java/io/IOException", errno);
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
    if (close(deviceHandle) == -1) {
        throwException(env, "java/io/IOException", errno);
    }
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setStatus(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jboolean isUp) {
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
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

JNIEXPORT jint JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getMTU(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle) {
    jint result = -1;
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
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
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
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
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    jstring string = NULL;
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _deviceName, IFNAMSIZ);
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
    const char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, NULL);
    const char *_address = (*env)->GetStringUTFChars(env, address, NULL);
    const char *_mask = (*env)->GetStringUTFChars(env, mask, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _deviceName, IFNAMSIZ);
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
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCGIFHWADDR, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        } else {
            (*env)->SetByteArrayRegion(env, mac, 0, 6, ifr.ifr_hwaddr.sa_data);
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
    return mac;
}

JNIEXPORT void JNICALL Java_one_papachi_tuntap4j_NetworkDevice_setMACAddress(JNIEnv *env, jclass class, jstring deviceName, jlong deviceHandle, jbyteArray mac) {
    const char *_device = (*env)->GetStringUTFChars(env, deviceName, NULL);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, _device, IFNAMSIZ);
    ifr.ifr_ifru.ifru_hwaddr.sa_family = ARPHRD_ETHER;
    (*env)->GetByteArrayRegion(env, mac, 0, 6, (jbyte *) &ifr.ifr_ifru.ifru_hwaddr.sa_data);
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == -1) {
        throwException(env, "java/io/IOException", errno);
    } else {
        if (ioctl(sockfd, SIOCSIFHWADDR, &ifr) == -1) {
            throwException(env, "java/io/IOException", errno);
        }
        close(sockfd);
    }
    (*env)->ReleaseStringUTFChars(env, deviceName, _device);
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_nativeList(JNIEnv *env, jclass class) {
    throwException(env, "java/lang/UnsupportedOperationException", ENOSYS);
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getAvailableTunDevices(JNIEnv *env, jclass _class) {
    jclass class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID constructor = (*env)->GetMethodID(env, class, "<init>", "()V");
    jmethodID add = (*env)->GetMethodID(env, class, "add", "(Ljava/lang/Object;)Z");
    jobject result = (*env)->NewObject(env, class, constructor);
    struct dirent *dir;
    DIR *d;
    if ((d = opendir("/sys/class/net")) != NULL) {
        while ((dir = readdir(d)) != NULL) {
            char *tmp1 = concat("/sys/class/net/", dir->d_name);
            char *tmp2 = concat(tmp2, "/tun_flags");
            char *deviceName = concat("", dir->d_name);
            struct stat st;
            if (stat(tmp2, &st) != -1) {
                char *tmp3 = concat(tmp1, "/addr_len");
                FILE *f;
                if ((f = fopen(tmp3, "r")) != NULL) {
                    int c = fgetc(f);
                    if (c == '0') {
                        jstring device = (*env)->NewStringUTF(env, deviceName);
                        (*env)->CallBooleanMethod(env, result, add, device);
                    }
                    fclose(f);
                } else {
                    throwException(env, "java/io/IOException", errno);
                }
                free(tmp3);
            }
            free(tmp1);
            free(tmp2);
            free(deviceName);
        }
        closedir(d);
    } else {
        throwException(env, "java/io/IOException", errno);
    }
    return result;
}

JNIEXPORT jobject JNICALL Java_one_papachi_tuntap4j_NetworkDevice_getAvailableTapDevices(JNIEnv *env, jclass _class) {
    jclass class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID constructor = (*env)->GetMethodID(env, class, "<init>", "()V");
    jmethodID add = (*env)->GetMethodID(env, class, "add", "(Ljava/lang/Object;)Z");
    jobject result = (*env)->NewObject(env, class, constructor);
    struct dirent *dir;
    DIR *d;
    if ((d = opendir("/sys/class/net")) != NULL) {
        while ((dir = readdir(d)) != NULL) {
            char *tmp1 = concat("/sys/class/net/", dir->d_name);
            char *tmp2 = concat(tmp2, "/tun_flags");
            char *deviceName = concat("", dir->d_name);
            struct stat st;
            if (stat(tmp2, &st) != -1) {
                char *tmp3 = concat(tmp1, "/addr_len");
                FILE *f;
                if ((f = fopen(tmp3, "r")) != NULL) {
                    int c = fgetc(f);
                    if (c == '6') {
                        jstring device = (*env)->NewStringUTF(env, deviceName);
                        (*env)->CallBooleanMethod(env, result, add, device);
                    }
                    fclose(f);
                } else {
                    throwException(env, "java/io/IOException", errno);
                }
                free(tmp3);
            }
            free(tmp1);
            free(tmp2);
            free(deviceName);
        }
        closedir(d);
    } else {
        throwException(env, "java/io/IOException", errno);
    }
    return result;
}