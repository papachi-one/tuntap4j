package one.papachi.tuntap4j.ffm;

import one.papachi.tuntap4j.api.NicAddress;
import one.papachi.tuntap4j.api.TunDevice;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public abstract class AbstractMacosTunDevice extends AbstractMacosNetworkDevice implements TunDevice {

    @Override
    public void setTunnelAddresses(InetAddress localAddress, InetAddress remoteAddress) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            if (localAddress instanceof Inet4Address) {


                MemorySegment ifaliasreq = arena.allocate(16 + 16 + 16 + 16);
                ifaliasreq.setString(0, deviceName);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 18L, (short) 0);
                ifaliasreq.asByteBuffer().put(20, localAddress.getAddress());

                ifaliasreq.set(ValueLayout.JAVA_BYTE, 32L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 33L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 34L, (short) 0);
                ifaliasreq.asByteBuffer().put(36, remoteAddress.getAddress());

                ifaliasreq.set(ValueLayout.JAVA_BYTE, 48L, (byte) 16);
                ifaliasreq.set(ValueLayout.JAVA_BYTE, 49L, (byte) AF_INET);
                ifaliasreq.set(ValueLayout.JAVA_SHORT, 50L, (short) 0);
                ifaliasreq.asByteBuffer().put(52, NicAddress.mask(32));

                int handle;
                if ((handle = (int) api.socket.invokeExact(AF_INET, SOCK_DGRAM, 0)) < 0)
                    getAndThrowException("socket");
                if ((int) api.ioctl1.invokeExact(handle, SIOCAIFADDR, ifaliasreq, (int) ifaliasreq.byteSize()) < 0)
                    getAndThrowException("ioctl");
            } else if (localAddress instanceof Inet6Address) {
                int prefixLength = 128;
                byte[] mask = NicAddress.mask(prefixLength);

                MemorySegment in6_aliasreq = arena.allocate(128);
                in6_aliasreq.setString(0, deviceName);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 16L, (byte) 28);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 17L, (byte) AF_INET6);
                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 18L, (short) 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 20L, 0);
                in6_aliasreq.asByteBuffer().put(24, localAddress.getAddress());
                in6_aliasreq.set(ValueLayout.JAVA_INT, 40L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 44L, (byte) 28);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 45L, (byte) AF_INET6);
                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 46L, (short) 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 48L, 0);
                in6_aliasreq.asByteBuffer().put(52, remoteAddress.getAddress());
                in6_aliasreq.set(ValueLayout.JAVA_INT, 68L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 72L, (byte) 28);
                in6_aliasreq.set(ValueLayout.JAVA_BYTE, 73L, (byte) AF_INET6);
                in6_aliasreq.set(ValueLayout.JAVA_SHORT, 74L, (short) 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 76L, 0);
                in6_aliasreq.asByteBuffer().put(80, mask);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 96L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_INT, 100L, 0);

                in6_aliasreq.set(ValueLayout.JAVA_INT, 104L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 108L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 112L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 116L, 0);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 120L, 0xFFFFFF);
                in6_aliasreq.set(ValueLayout.JAVA_INT, 124L, 0xFFFFFF);


                int handle;
                if ((handle = (int) api.socket.invokeExact(AF_INET6, SOCK_DGRAM, 0)) < 0)
                    getAndThrowException("socket");
                if ((int) api.ioctl1.invokeExact(handle, (int) 2155899162L, in6_aliasreq, (int) in6_aliasreq.byteSize()) < 0)
                    getAndThrowException("ioctl");

            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }

    }

    @Override
    public InetAddress getLocalAddress() throws IOException {
        return getAddress(SIOCGIFADDR);
    }

    @Override
    public void setLocalAddress(InetAddress localAddress) throws IOException {
        setAddress((Inet4Address) localAddress, SIOCSIFADDR);
    }

    @Override
    public InetAddress getRemoteAddress() throws IOException {
        return getAddress(SIOCGIFDSTADDR);
    }

    @Override
    public void setRemoteAddress(InetAddress remoteAddress) throws IOException {
        setAddress((Inet4Address) remoteAddress, SIOCSIFDSTADDR);
    }

}
