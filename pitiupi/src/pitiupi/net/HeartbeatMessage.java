package pitiupi.net;
/**
 * Mensagem utilizada para sinalizar a presença de um usuário na rede.
 *
 * <p>É enviada periodicamente para permitir que a aplicação identifique
 * usuários ativos e mantenha o controle dos participantes conectados.</p>
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
