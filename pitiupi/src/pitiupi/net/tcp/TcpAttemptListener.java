package pitiupi.net.tcp;

/**
 * Resultado de um connect TCP disparado fora da thread da interface.
 * Os métodos são chamados na EDT.
 */
public interface TcpAttemptListener {

    void onConnecting(String endpoint);

    void onSuccess(TcpConnection connection);

    void onFailure(String message);
}
