package pitiupi.application.peeractions;

import java.util.List;

/**
 * Ações de um plugin para o menu contextual da lista lateral.
 */
public record GroupedPeerActions(String pluginId, String pluginDisplayName, List<PeerAction> actions) {
}
