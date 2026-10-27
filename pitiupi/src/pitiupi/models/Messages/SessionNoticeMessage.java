package pitiupi.models.Messages;

import pitiupi.application.messaging.TransportMode;
import pitiupi.application.session.SessionNoticeChannel;
import pitiupi.application.session.SessionNoticePhase;
import pitiupi.application.session.SessionNoticeScope;

/**
 * Aviso de início/fim de atividade de sessão (compartilhamento, canais, etc.).
 */
public class SessionNoticeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final String pluginId;
    private final SessionNoticePhase phase;
    private final String channelWireId;
    private final String senderDisplayName;
    private final String senderIp;
    private final SessionNoticeScope scope;
    private final TransportMode transportMode;

    public SessionNoticeMessage(
            String pluginId,
            SessionNoticePhase phase,
            SessionNoticeChannel channel,
            String senderDisplayName,
            String senderIp,
            SessionNoticeScope scope,
            TransportMode transportMode
    ) {
        super(true);
        this.pluginId = pluginId;
        this.phase = phase;
        this.channelWireId = channel != null ? channel.wireId() : null;
        this.senderDisplayName = senderDisplayName;
        this.senderIp = senderIp;
        this.scope = scope;
        this.transportMode = transportMode;
    }

    public String getPluginId() {
        return pluginId;
    }

    public SessionNoticePhase getPhase() {
        return phase;
    }

    public SessionNoticeChannel getChannel() {
        return SessionNoticeChannel.fromWireId(channelWireId);
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public SessionNoticeScope getScope() {
        return scope;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }
}
