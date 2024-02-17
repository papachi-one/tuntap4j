package one.papachi.tuntap4j.devices;

import one.papachi.tuntap4j.ffm.NativeMacosApi;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.ByteBuffer;

public abstract class AbstractMacosNetworkDevice {

    protected NativeMacosApi api;

    protected String deviceName;

    protected int handle;

    public boolean isOpen() throws IOException {
        return api.isOpen(handle);
    }

    public int read(ByteBuffer dst) throws IOException {
        return api.read(handle, dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return api.write(handle, src);
    }

    public void close() throws IOException {
        api.close(handle);
    }

    public boolean isUp() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            return api.isUp(arena, deviceName);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void setUp(boolean isUp) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.setUp(arena, deviceName, isUp);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public int getMtu() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            return api.getMtu(arena, deviceName);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

    public void setMtu(int mtu) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            api.setMtu(arena, deviceName, mtu);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Native function has failed.", e);
        }
    }

}
