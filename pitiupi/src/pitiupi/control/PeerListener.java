package pitiupi.control;

import java.util.List;
/**
 *
 * @author tony
 */
public interface PeerListener {
    void onPeersChanged(List<PeerInfo> peers);
}
