package pitiupi.net;
/**
 *
 * @author tony
 */
public class HeartbeatMessage extends Message {

    private final String userName;
    private final long timestamp;

    public HeartbeatMessage(String userName) {
        this.userName = userName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
