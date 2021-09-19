package one.papachi.tuntap4j;

public class Utils {

    enum OperatingSystemFamily {
        WINDOWS("tuntap4j.dll"), LINUX("libtuntap4j.so"), MAC("libtuntap4j.dylib"), UNKNOWN(null);

        private String libraryName;

        private OperatingSystemFamily(String libraryName) {
            this.libraryName = libraryName;
        }

        public String getLibraryName() {
            return libraryName;
        }
    }

    public static OperatingSystemFamily getOperatingSystemFamily() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OperatingSystemFamily.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OperatingSystemFamily.LINUX;
        } else if (osName.contains("mac")) {
            return OperatingSystemFamily.MAC;
        }
        return OperatingSystemFamily.UNKNOWN;
    }

}
