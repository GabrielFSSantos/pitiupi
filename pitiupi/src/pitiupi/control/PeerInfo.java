package pitiupi.control;

import java.net.InetAddress;
/**
 *
 * @author tony
 */
public class PeerInfo {

    private final String userName;
    private final InetAddress address;
    private volatile long lastSeen;

    public PeerInfo(String userName, InetAddress address, long lastSeen) {
        this.userName = userName;
        this.address = address;
        this.lastSeen = lastSeen;
    }

    public String getUserName() {
        return userName;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void touch(long timestamp) {
        this.lastSeen = timestamp;
    }

    @Override
    public String toString() {
        return userName + " (" + address.getHostAddress() + ")";
    }
}
