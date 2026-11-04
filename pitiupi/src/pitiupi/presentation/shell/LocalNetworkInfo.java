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

    public enum EgressMode {
        AUTO,
        WIRED,
        WIRELESS
    }

    private static final String APIPA_PREFIX = "169.254.";
    private static volatile EgressMode egressMode = EgressMode.AUTO;

    private LocalNetworkInfo() {
    }

    public static void setEgressMode(EgressMode mode) {
        egressMode = mode == null ? EgressMode.AUTO : mode;
    }

    public static EgressMode getEgressMode() {
        return egressMode;
    }

    /**
     * Modo automático sempre pode ser aplicado. Cabo e Wi-Fi exigem uma interface dessa família.
     */
    public static boolean canUse(EgressMode mode) {
        if (mode == null || mode == EgressMode.AUTO) {
            return true;
        }
        return selectInterface(mode) != null;
    }

    /**
     * Interface preferida para LAN, respeitando o modo de saída configurado.
     */
    public static NetworkInterface preferredNetworkInterface() {
        return selectInterface(egressMode);
    }

    public static NetworkInterface selectInterface(EgressMode mode) {
        EgressMode effective = mode == null ? EgressMode.AUTO : mode;
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
                Kind kind = classify(ni);
                if (kind == Kind.WIRED) {
                    if (hasApipaIpv4(ni)) {
                        if (wiredApipa == null) {
                            wiredApipa = ni;
                        }
                    } else if (wiredOther == null) {
                        wiredOther = ni;
                    }
                } else if (kind == Kind.WIRELESS && wireless == null) {
                    wireless = ni;
                }
            }
            if (effective == EgressMode.WIRED) {
                if (wiredApipa != null) {
                    return wiredApipa;
                }
                return wiredOther;
            }
            if (effective == EgressMode.WIRELESS) {
                return wireless;
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

    public static String describeActiveInterface() {
        NetworkInterface ni = preferredNetworkInterface();
        if (ni == null) {
            return "nenhuma interface";
        }
        String ip = bestIpv4OnInterface(ni);
        return ni.getName() + " · " + (ip == null ? "—" : ip);
    }

    public static InetAddress bestLocalInet4Address() {
        try {
            NetworkInterface preferred = preferredNetworkInterface();
            if (preferred == null) {
                return null;
            }
            String ip = bestIpv4OnInterface(preferred);
            if (ip == null) {
                return null;
            }
            return InetAddress.getByName(ip);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isWiredInterface(String name, String displayName) {
        return matchesWiredToken(name) || matchesWiredToken(displayName);
    }

    public static boolean isWirelessInterface(String name, String displayName) {
        return matchesWirelessToken(name) || matchesWirelessToken(displayName);
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

    private enum Kind {
        WIRED,
        WIRELESS,
        OTHER
    }

    private static Kind classify(NetworkInterface ni) {
        String name = ni.getName();
        String display = ni.getDisplayName();
        boolean wireless = isWirelessInterface(name, display);
        boolean wired = isWiredInterface(name, display);
        if (wireless && !wired) {
            return Kind.WIRELESS;
        }
        if (wired && !wireless) {
            return Kind.WIRED;
        }
        if (wireless) {
            return Kind.WIRELESS;
        }
        return Kind.OTHER;
    }

    private static boolean matchesWiredToken(String value) {
        if (value == null) {
            return false;
        }
        String n = value.toLowerCase(Locale.ROOT);
        return n.startsWith("eth")
                || n.startsWith("eno")
                || n.startsWith("enp")
                || n.startsWith("ens")
                || n.contains("ethernet");
    }

    private static boolean matchesWirelessToken(String value) {
        if (value == null) {
            return false;
        }
        String n = value.toLowerCase(Locale.ROOT);
        return n.startsWith("wlan")
                || n.startsWith("wlp")
                || n.startsWith("wl")
                || n.contains("wi-fi")
                || n.contains("wifi")
                || n.contains("wireless");
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
