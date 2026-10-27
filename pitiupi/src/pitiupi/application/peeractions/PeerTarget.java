package pitiupi.application.peeractions;

import pitiupi.application.presence.UserPeerView;

/**
 * Destino escolhido na lista (um peer ou Todos).
 */
public sealed interface PeerTarget permits PeerTarget.Single, PeerTarget.All {

    record Single(UserPeerView peer) implements PeerTarget {
    }

    record All() implements PeerTarget {
    }
}
