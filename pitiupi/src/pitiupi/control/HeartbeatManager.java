package pitiupi.control;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import pitiupi.GUI.MainWindow;
import pitiupi.net.HeartbeatMessage;
import java.util.UUID;
/**
 *
 * @author tony
 */
public class HeartbeatManager {

    private static final long HEARTBEAT_INTERVAL_MS = 5000;
    private static final long PEER_TIMEOUT_MS = 15000; // ~3 batimentos perdidos

    private final MainWindow mainWindow;
    private final Map<String, PeerInfo> peers = new ConcurrentHashMap<>();
    private final List<PeerListener> listeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private final String instanceId = UUID.randomUUID().toString();

    public HeartbeatManager(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void start() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::announce, 0, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::checkTimeouts, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void announce() {
        try {
            HeartbeatMessage msg = new HeartbeatMessage(mainWindow.getUserName(), instanceId);
            mainWindow.getSocket().send(msg.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(HeartbeatManager.class.getName()).log(Level.WARNING, "Falha ao enviar heartbeat", ex);
        }
    }

    
    public void receiveHeartbeat(HeartbeatMessage msg, InetAddress from) {
        // ignora o próprio heartbeat 
        if (msg.getInstanceId().equals(instanceId) ) {
            return;
        }

        String key =  msg.getInstanceId();
        boolean isNew = !peers.containsKey(key);

        PeerInfo peer = peers.computeIfAbsent(key, k -> new PeerInfo(msg.getUserName(), from, msg.getTimestamp()));
        peer.touch(System.currentTimeMillis());

        if (isNew) {
            notifyListeners();
        }
    }

    private void checkTimeouts() {
        long now = System.currentTimeMillis();
        boolean changed = peers.values().removeIf(p -> now - p.getLastSeen() > PEER_TIMEOUT_MS);
        if (changed) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        List<PeerInfo> snapshot = new ArrayList<>(peers.values());
        for (PeerListener l : listeners) {
            l.onPeersChanged(snapshot);
        }
    }

    public void addPeerListener(PeerListener l) {
        listeners.add(l);
        l.onPeersChanged(new ArrayList<>(peers.values()));
    }

    public void removePeerListener(PeerListener l) {
        listeners.remove(l);
    }

    public List<PeerInfo> getPeers() {
        return new ArrayList<>(peers.values());
    }
}
