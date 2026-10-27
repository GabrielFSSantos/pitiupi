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

    private static final String APIPA_PREFIX = "169.254.";

    private LocalNetworkInfo() {
    }

    /**
     * Interface preferida para LAN: cabo antes de Wi‑Fi; APIPA (169.254.x) antes de outro IPv4 no cabo.
     */
    public static NetworkInterface preferredNetworkInterface() {
        try {
            NetworkInterface wiredApipa = null;
            NetworkInterface wiredOther = null;
            NetworkInterface wireless = null;
            NetworkInterface fallback = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!isCandidateInterface(ni)) {
                    continue;
                }
                if (fallback == null) {
                    fallback = ni;
                }
                String name = ni.getName();
                if (isWiredLanInterfaceName(name)) {
                    if (hasApipaIpv4(ni)) {
                        if (wiredApipa == null) {
                            wiredApipa = ni;
                        }
                    } else if (wiredOther == null) {
                        wiredOther = ni;
                    }
                } else if (isWirelessLanInterfaceName(name) && wireless == null) {
                    wireless = ni;
                }
            }
            if (wiredApipa != null) {
                return wiredApipa;
            }
            if (wiredOther != null) {
                return wiredOther;
            }
            if (wireless != null) {
                return wireless;
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
                String fromPreferred = bestIpv4OnInterface(preferred);
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
                String ip = bestIpv4OnInterface(ni);
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
        return bestIpv4OnInterface(ni) != null;
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

    private static boolean isWiredLanInterfaceName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.startsWith("eth")
                || n.startsWith("eno")
                || n.startsWith("enp")
                || n.startsWith("ens");
    }

    private static boolean isWirelessLanInterfaceName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return n.startsWith("wlan")
                || n.startsWith("wlp")
                || n.startsWith("wl");
    }

    private static boolean hasApipaIpv4(NetworkInterface ni) {
        return apipaIpv4OnInterface(ni) != null;
    }

    private static String apipaIpv4OnInterface(NetworkInterface ni) {
        try {
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    String host = addr.getHostAddress();
                    if (host != null && host.startsWith(APIPA_PREFIX)) {
                        return host;
                    }
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private static String bestIpv4OnInterface(NetworkInterface ni) {
        String apipa = apipaIpv4OnInterface(ni);
        if (apipa != null) {
            return apipa;
        }
        return firstIpv4OnInterface(ni);
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
