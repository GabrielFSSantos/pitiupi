package pitiupi.chat.net;

import pitiupi.models.Messages.Message;

/**
 * Formato legado do antigo plugin mpChat (TCP privado e salas antigas).
 */
public class LegacyMpChatMessage extends Message {
    private String nome;
    private String ip;
    private String tipoMensagem;
    private String message;

    public LegacyMpChatMessage() {
    }

    public LegacyMpChatMessage(String nome, String ip, String tipoMensagem, String message) {
        this.nome = nome;
        this.ip = ip;
        this.tipoMensagem = tipoMensagem;
        this.message = message;
    }

    public String getNome() {
        return nome;
    }

    public String getIp() {
        return ip;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public String getMessage() {
        return message;
    }
}
