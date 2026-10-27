package pitiupi.presentation.shell;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Endereço IPv4 local preferencial para compartilhar com peers.
 */
public final class LocalNetworkInfo {

    private LocalNetworkInfo() {
    }

    /**
     * Interface preferida para LAN (Wi‑Fi/cabo), ignorando bridges Docker/WSL/VPN virtuais quando possível.
     */
    public static NetworkInterface preferredNetworkInterface() {
        try {
            NetworkInterface fallback = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!isCandidateInterface(ni)) {
                    continue;
                }
                if (isPreferredLanInterfaceName(ni.getName())) {
                    return ni;
                }
                if (fallback == null) {
                    fallback = ni;
                }
            }
            return fallback;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String bestLocalIpv4() {
        try {
            NetworkInterface preferred = preferredNetworkInterface();
            if (preferred != null) {
                String fromPreferred = firstIpv4OnInterface(preferred);
                if (fromPreferred != null) {
                    return fromPreferred;
                }
            }
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!isCandidateInterface(ni)) {
                    continue;
                }
                String ip = firstIpv4OnInterface(ni);
                if (ip != null) {
                    return ip;
                }
            }
            InetAddress local = InetAddress.getLocalHost();
            return local.getHostAddress();
        } catch (Exception ex) {
            return "—";
        }
    }

    private static boolean isCandidateInterface(NetworkInterface ni) {
        if (ni == null) {
            return false;
        }
        try {
            if (!ni.isUp() || ni.isLoopback()) {
                return false;
            }
        } catch (java.net.SocketException ex) {
            return false;
        }
        if (ni.isVirtual() || isVirtualInterfaceName(ni.getName())) {
            return false;
        }
        return firstIpv4OnInterface(ni) != null;
    }

    private static boolean isVirtualInterfaceName(String name) {
        if (name == null) {
            return true;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.equals("lo")
                || n.startsWith("docker")
                || n.startsWith("br-")
                || n.startsWith("veth")
                || n.startsWith("vir")
                || n.contains("wsl")
                || n.startsWith("tailscale");
    }

    private static boolean isPreferredLanInterfaceName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.startsWith("wlan")
                || n.startsWith("wlp")
                || n.startsWith("wl")
                || n.startsWith("eth")
                || n.startsWith("eno")
                || n.startsWith("enp")
                || n.startsWith("ens");
    }

    private static String firstIpv4OnInterface(NetworkInterface ni) {
        try {
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
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
