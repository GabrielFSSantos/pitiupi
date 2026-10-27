package pitiupi.chat.control;

import pitiupi.chat.net.LegacyMpChatMessage;

/**
 * TCP privado integrado ao painel principal (sem janela extra).
 */
public interface PrivateChatUiSink {

    void onPrivateChannelReady(String peerIp);

    void onPrivateChannelClosed(String peerIp);

    void appendPrivateLine(String peerIp, LegacyMpChatMessage message, boolean outgoing);
}
