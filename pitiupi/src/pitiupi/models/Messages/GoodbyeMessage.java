package pitiupi.models.Messages;

/**
 * Mensagem de saída graciosa enviada em multicast ao fechar a aplicação.
 */
public class GoodbyeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final String userName;
    private final String instanceId;

    public GoodbyeMessage(String userName, String instanceId) {
        super(true);
        this.userName = userName;
        this.instanceId = instanceId;
    }

    public String getUserName() {
        return userName;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
