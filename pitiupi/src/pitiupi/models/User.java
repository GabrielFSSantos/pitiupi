package pitiupi.models;

import java.net.InetAddress;
/**
 * Representa um usuário identificado na rede e as informações necessárias
 * para acompanhar sua presença através de mensagens de heartbeat.
 *
 * <p>Armazena o nome do usuário, seu endereço de rede e o instante da
 * última mensagem recebida.</p>
 *
 * @author tony
 */
public class User {

    public static final String BROADCAST_INSTANCE_ID = "__broadcast__";

    private final String userName;
    private final InetAddress address;
    private volatile long lastSeen;
    private final String instanceId;

    public User(String userName, InetAddress address, long lastSeen) {
        this(userName, address, lastSeen, null);
    }

    public User(String userName, InetAddress address, long lastSeen, String instanceId) {
        this.userName = userName;
        this.address = address;
        this.lastSeen = lastSeen;
        this.instanceId = instanceId;
    }

    public static User broadcastAll() {
        return new User("Todos", null, 0, BROADCAST_INSTANCE_ID);
    }

    public boolean isBroadcastTarget() {
        return BROADCAST_INSTANCE_ID.equals(instanceId);
    }

    public String getInstanceId() {
        return instanceId;
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

    /**
     * Atualiza o instante da última atividade do usuário.
     *
     * @param timestamp novo instante de atividade.
     */
    public void touch(long timestamp) {
        this.lastSeen = timestamp;
    }

    @Override
    public String toString() {
        if (isBroadcastTarget() || address == null) {
            return userName;
        }
        return userName + " (" + address.getHostAddress() + ")";
    }
}
