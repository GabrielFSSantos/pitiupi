package pitiupi.api.plugin;

import pitiupi.application.peeractions.PeerAction;

/**
 * Registro de ações contextuais na lista de usuários (plugin da aba ativa).
 */
public interface PeerActionsRegistrar {

    void register(PeerAction action);
}
