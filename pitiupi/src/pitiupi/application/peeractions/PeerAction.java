package pitiupi.application.peeractions;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ação exibida no menu contextual da lista de usuários.
 */
public final class PeerAction {

    private final String id;
    private final String label;
    private final Set<PeerActionTarget> targets;
    private final PeerActionHandler handler;

    public PeerAction(String id, String label, Set<PeerActionTarget> targets, PeerActionHandler handler) {
        this.id = id;
        this.label = label;
        this.targets = EnumSet.copyOf(targets);
        this.handler = handler;
    }

    public static PeerAction of(String id, String label, PeerActionTarget target, PeerActionHandler handler) {
        return new PeerAction(id, label, Set.of(target), handler);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Set<PeerActionTarget> getTargets() {
        return targets;
    }

    public PeerActionHandler getHandler() {
        return handler;
    }

    public boolean supports(PeerActionTarget target) {
        return targets.contains(target);
    }
}
