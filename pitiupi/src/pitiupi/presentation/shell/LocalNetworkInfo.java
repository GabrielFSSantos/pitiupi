package pitiupi.presentation.shell;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Endereço IPv4 local preferencial para compartilhar com peers.
 */
public final class LocalNetworkInfo {

    private LocalNetworkInfo() {
    }

    public static String bestLocalIpv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            InetAddress local = InetAddress.getLocalHost();
            return local.getHostAddress();
        } catch (Exception ex) {
            return "—";
        }
    }

    /**
     * {@code true} se o IP informado é deste host (inclui loopback e interfaces ativas).
     */
    public static boolean isLocalIpv4(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        if ("127.0.0.1".equals(trimmed) || "localhost".equalsIgnoreCase(trimmed)) {
            return true;
        }
        try {
            String canonical = InetAddress.getByName(trimmed).getHostAddress();
            if ("127.0.0.1".equals(canonical)) {
                return true;
            }
            if (canonical.equals(bestLocalIpv4())) {
                return true;
            }
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && canonical.equals(addr.getHostAddress())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
            return trimmed.equals(bestLocalIpv4());
        }
        return false;
    }
}
