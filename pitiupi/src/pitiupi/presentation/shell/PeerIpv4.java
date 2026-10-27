package pitiupi.presentation.shell;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * IPv4 legível para peers (UDP/TCP/chat). No Windows, {@link InetAddress#getHostAddress()}
 * pode devolver forma IPv6 mapeada ({@code 0:0:...:ffff:a9fe:f8c4}) ou sufixo de zona ({@code %7}).
 */
public final class PeerIpv4 {

    private PeerIpv4() {
    }

    public static String fromInetAddress(InetAddress address) {
        if (address == null) {
            return "";
        }
        if (address instanceof Inet4Address) {
            return address.getHostAddress();
        }
        if (address instanceof Inet6Address inet6 && inet6.isIPv4CompatibleAddress()) {
            byte[] raw = address.getAddress();
            if (raw.length == 16) {
                return ipv4FromBytes(raw, 12);
            }
        }
        byte[] raw = address.getAddress();
        if (raw.length == 16 && isIpv4Mapped(raw)) {
            return ipv4FromBytes(raw, 12);
        }
        return stripZoneId(address.getHostAddress());
    }

    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = stripZoneId(text.trim());
        try {
            return fromInetAddress(InetAddress.getByName(trimmed));
        } catch (Exception ex) {
            return trimmed;
        }
    }

    private static boolean isIpv4Mapped(byte[] raw) {
        return raw[0] == 0 && raw[1] == 0 && raw[2] == 0 && raw[3] == 0
                && raw[4] == 0 && raw[5] == 0 && raw[6] == 0 && raw[7] == 0
                && raw[8] == 0 && raw[9] == 0 && raw[10] == (byte) 0xff && raw[11] == (byte) 0xff;
    }

    private static String ipv4FromBytes(byte[] raw, int offset) {
        return (raw[offset] & 0xff) + "." + (raw[offset + 1] & 0xff) + "."
                + (raw[offset + 2] & 0xff) + "." + (raw[offset + 3] & 0xff);
    }

    private static String stripZoneId(String host) {
        if (host == null) {
            return "";
        }
        int zone = host.indexOf('%');
        return zone >= 0 ? host.substring(0, zone) : host;
    }
}
