package pitiupi.chat.net;

import pitiupi.application.messaging.TransportMode;
import pitiupi.models.Messages.Message;

public class ChatMessage extends Message {

    private String text;
    private String name;
    private String transportLabel;
    private String senderIp;

    public ChatMessage() {
    }

    public ChatMessage(String name, String text) {
        this(name, text, TransportMode.MULTICAST_UDP.getLabel(), null);
    }

    public ChatMessage(String name, String text, String transportLabel, String senderIp) {
        this.name = name;
        this.text = text;
        this.transportLabel = transportLabel;
        this.senderIp = senderIp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTransportLabel() {
        return transportLabel;
    }

    public void setTransportLabel(String transportLabel) {
        this.transportLabel = transportLabel;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }
}
