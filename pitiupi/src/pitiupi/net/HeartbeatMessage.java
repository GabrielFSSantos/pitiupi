package pitiupi.net;
/**
 *
 * @author tony
 */
public class HeartbeatMessage extends Message {

    private final String userName;
    private final long timestamp;
    private final String instanceId; 

    public HeartbeatMessage(String userName, String instanceId) {
        this.userName = userName;
        this.instanceId = instanceId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
