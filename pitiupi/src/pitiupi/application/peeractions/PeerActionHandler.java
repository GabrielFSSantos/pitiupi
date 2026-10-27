package pitiupi.application.peeractions;

@FunctionalInterface
public interface PeerActionHandler {

    void run(PeerTarget target);
}
