package net;

import pitiupi.net.Message;

public class TestMessage extends Message {
    private String nome;
    private String ip;
    private String tipoMensagem;
    private String message;

    public TestMessage(String nome, String ip, String tipoMensagem, String message) {
        this.nome = nome;
        this.ip = ip;
        this.tipoMensagem = tipoMensagem;
        this.message = message;
    }

    public TestMessage(){}

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
