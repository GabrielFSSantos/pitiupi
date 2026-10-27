package pitiupi.screenCast.GUI;

import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.PluginTransportCatalog;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.api.plugin.PeerActivityFocusTokens;
import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;
import pitiupi.application.session.SessionNoticeChannel;
import pitiupi.application.session.SessionNoticePhase;
import pitiupi.application.session.SessionNoticeScope;
import pitiupi.models.Messages.SessionNoticeMessage;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerDraftPanel;
import pitiupi.presentation.shell.PeerIpv4;
import pitiupi.presentation.shell.PluginRoutingPanel;
import pitiupi.presentation.shell.PluginSessionKeys;
import pitiupi.presentation.shell.PluginSessionWorkspace;
import pitiupi.presentation.shell.SessionPeerLabels;
import pitiupi.presentation.shell.SessionTabBar;
import pitiupi.presentation.shell.SessionTabDirectionLabels;
import pitiupi.presentation.shell.ui.ShellUi;

import pitiupi.screenCast.control.AlarmClock;
import pitiupi.screenCast.control.ImageTransformation;
import pitiupi.screenCast.control.ScreenCaptureDiagnostics;
import pitiupi.screenCast.control.ScreenShot;
import pitiupi.screenCast.net.ScreenCastMessage;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * Painel de compartilhamento de tela.
 * Backlog UX: prévia dividida (recebendo + enviando) na mesma aba peer; multi-share simultâneo.
 */
public class ScreenCastPanel extends JPanel {

    public static final int TIME = 1000;
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;
    private static final String CARD_DRAFT = "__draft__";
    private static final String IDLE_ROOM_HTML =
            "<html><p>Ative <b>Compartilhamento da sua tela</b> para enviar para todos na rede (multicast).</p></html>";
    private static final String IDLE_PEER_HTML =
            "<html><p>Ative o compartilhamento para enviar só para este peer (unicast).</p></html>";

    private AlarmClock alarmClock;
    private Timer timer;
    private float compressRatio;
    private final ConnectionManager connectionManager;
    private final PluginSessionWorkspace workspace;
    private final PluginRoutingPanel routingPanel;
    private final SessionTabBar sessionTabs;
    private final JToggleButton shareToggle = new JToggleButton("Compartilhamento da sua tela (desligado)");
    private final Map<String, Boolean> sharingByTabKey = new HashMap<>();
    private boolean suppressShareToggleEvents;
    private String shareContextTabKey;
    private String lastSessionKey = PluginSessionKeys.ROOM;
    private final Map<String, String> peerIpsByViewKey = new HashMap<>();
    private final Map<String, String> peerDisplayNames = new HashMap<>();
    private final Map<String, ScreenCastReceiveScope> receiveScopes = new HashMap<>();
    private final Map<String, TransportMode> receiveTransportByViewKey = new HashMap<>();
    private PluginRoutingPanel.RoutingSnapshot lastShareRoutingSnapshot;
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel bodyCards = new JPanel(bodyLayout);
    private final Map<String, JPanel> bodyPanels = new HashMap<>();
    private PeerDraftPanel draftPanel;
    private String pluginId;

    public ScreenCastPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.compressRatio = 0.55f;
        this.routingPanel = new PluginRoutingPanel(
                PluginTransportCatalog.PluginKind.SCREEN_CAST,
                "<html><i>Receba pela lista lateral (notificação) ou use + Nova para um IP. "
                        + "A aba Todos envia em multicast; abas de peer usam unicast.</i></html>",
                false
        );
        this.workspace = new PluginSessionWorkspace(routingPanel, buildShareToolbar());
        this.sessionTabs = workspace.getSessionTabBar();
        sessionTabs.setCloseFallbackKey(PluginSessionKeys.ROOM);
        initComponents();
    }

    private JPanel buildShareToolbar() {
        shareToggle.setFocusPainted(false);
        shareToggle.addActionListener(e -> {
            if (suppressShareToggleEvents) {
                return;
            }
            String key = sessionTabs.selectedKey();
            if (!ScreenCastOutboundTabs.isOutboundShareTabKey(key)) {
                return;
            }
            boolean nowOn = shareToggle.isSelected();
            if (!nowOn) {
                sendShareEndedNoticeForActiveTab();
            }
            sharingByTabKey.put(key, nowOn);
            updateShareToggleLabel();
            applySharingForActiveTab();
        });
        JPanel shareRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        shareRow.add(shareToggle);
        return shareRow;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        draftPanel = new PeerDraftPanel(
                "Nova sessão por IP",
                "Informe o IP para enviar ou receber unicast. O nome na aba atualiza quando o peer for conhecido.",
                this::commitDraftIp
        );
        bodyCards.add(draftPanel, CARD_DRAFT);
        workspace.getBodyHost().add(bodyCards, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
        timer = new Timer();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                onSessionTabSelected(sessionTabs.selectedKey());
            }
        });
        initSessionTabs();
    }

    private void initSessionTabs() {
        sessionTabs.addOrSelectTab(
                PluginSessionKeys.ROOM,
                SessionTabDirectionLabels.outboundTitle("Todos"),
                "Transmissão para todos na rede (multicast)",
                new JPanel(),
                false
        );
        sessionTabs.setOnTabSelected(this::onSessionTabSelected);
        sessionTabs.setOnAddPeerRequested(this::openDraftPeerTab);
        sessionTabs.setOnTabClosed(this::onSessionTabClosed);
        ensureBodyPanel(PluginSessionKeys.ROOM);
        setBodyMessage(PluginSessionKeys.ROOM, ShellUi.mutedLabel(IDLE_ROOM_HTML));
        sessionTabs.selectTab(PluginSessionKeys.ROOM);
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    private void onSessionTabClosed(String key) {
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            if (draftPanel != null) {
                draftPanel.clearIpField();
            }
            SwingUtilities.invokeLater(this::restoreSessionAfterDraftClosed);
            return;
        }
        if (key != null && key.startsWith("view:")) {
            peerIpsByViewKey.remove(key);
            receiveScopes.remove(key);
            receiveTransportByViewKey.remove(key);
            bodyPanels.remove(key);
            return;
        }
        boolean wasSharing = isSharingForTab(key);
        if (wasSharing) {
            applyRoutingForSessionKey(key);
            sendShareEndedNotice(routingPanel.snapshot());
        }
        sharingByTabKey.remove(key);
        if (wasSharing && key != null && key.equals(shareContextTabKey)) {
            cancelShareTimer();
            setShareToggleSelectedSilently(false);
            applySharingForActiveTab();
        }
    }

    private void restoreSessionAfterDraftClosed() {
        String restore = lastSessionKey != null && sessionTabs.hasTab(lastSessionKey)
                ? lastSessionKey
                : PluginSessionKeys.ROOM;
        if (PluginSessionKeys.PEER_DRAFT.equals(restore)) {
            restore = PluginSessionKeys.ROOM;
        }
        sessionTabs.selectTabSilently(restore);
        onSessionTabSelected(restore);
    }

    private void onSessionTabSelected(String key) {
        persistShareStateForContextTab();
        if (key != null && !PluginSessionKeys.PEER_DRAFT.equals(key)) {
            lastSessionKey = key;
        }
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            cancelShareTimer();
            applyShareToggleForTab(key);
            showBodyCard(CARD_DRAFT);
            draftPanel.focusIpField();
            return;
        }
        if (key == null) {
            key = PluginSessionKeys.ROOM;
        }
        showBodyCard(key);
        applyRoutingForSessionKey(key);
        applyShareToggleForTab(key);
        applySharingForActiveTab();
        if (!isSharingForTab(key)) {
            refreshOutboundBodyForTab(key);
        }
        reportActivityFocusForSession(key);
    }

    private void reportActivityFocusForSession(String key) {
        if (pluginId == null || key == null || PluginSessionKeys.PEER_DRAFT.equals(key)) {
            return;
        }
        var focus = connectionManager.getPeerActivityFocus();
        if (PluginSessionKeys.ROOM.equals(key)) {
            focus.setFocusedChannel(pluginId, PeerActivityFocusTokens.broadcast());
            return;
        }
        if (key.startsWith("peer:")) {
            String ip = key.substring("peer:".length());
            focus.setFocusedChannel(pluginId, resolvePeerFocusToken(ip));
            return;
        }
        if (key.startsWith("view:")) {
            ScreenCastReceiveScope scope = receiveScopes.getOrDefault(key, ScreenCastReceiveScope.BROADCAST);
            if (scope == ScreenCastReceiveScope.BROADCAST) {
                focus.setFocusedChannel(pluginId, PeerActivityFocusTokens.broadcast());
            } else {
                String ip = peerIpsByViewKey.get(key);
                if (ip != null && !ip.isBlank()) {
                    focus.setFocusedChannel(pluginId, resolvePeerFocusToken(ip));
                }
            }
        }
    }

    private String resolvePeerFocusToken(String ip) {
        String canonical = canonicalPeerIp(ip);
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (view.isBroadcastTarget() || view.isSelf()) {
                continue;
            }
            if (view.getInstanceId() != null
                    && view.getInetAddress().map(PeerIpv4::fromInetAddress).orElse("").equals(canonical)) {
                return PeerActivityFocusTokens.peerInstance(view.getInstanceId());
            }
        }
        return PeerActivityFocusTokens.peerIp(canonical);
    }

    private void persistShareStateForContextTab() {
        if (shareContextTabKey != null && ScreenCastOutboundTabs.isOutboundShareTabKey(shareContextTabKey)) {
            sharingByTabKey.put(shareContextTabKey, shareToggle.isSelected());
        }
    }

    private void applyShareToggleForTab(String key) {
        shareContextTabKey = ScreenCastOutboundTabs.isOutboundShareTabKey(key) ? key : null;
        suppressShareToggleEvents = true;
        try {
            if (shareContextTabKey == null) {
                shareToggle.setEnabled(false);
                shareToggle.setSelected(false);
            } else {
                shareToggle.setEnabled(true);
                shareToggle.setSelected(sharingByTabKey.getOrDefault(shareContextTabKey, false));
            }
            updateShareToggleLabel();
        } finally {
            suppressShareToggleEvents = false;
        }
    }

    private void setShareToggleSelectedSilently(boolean selected) {
        suppressShareToggleEvents = true;
        try {
            shareToggle.setSelected(selected);
            updateShareToggleLabel();
        } finally {
            suppressShareToggleEvents = false;
        }
    }

    private boolean isSharingForTab(String key) {
        return ScreenCastOutboundTabs.isOutboundShareTabKey(key)
                && sharingByTabKey.getOrDefault(key, false);
    }

    private void applySharingForActiveTab() {
        cancelShareTimer();
        String key = sessionTabs.selectedKey();
        if (!ScreenCastOutboundTabs.isOutboundShareTabKey(key)) {
            return;
        }
        boolean on = sharingByTabKey.getOrDefault(key, shareToggle.isSelected());
        sharingByTabKey.put(key, on);
        if (!on) {
            refreshOutboundBodyForTab(key);
            return;
        }
        syncRoutingFromActiveSessionTab();
        lastShareRoutingSnapshot = routingPanel.snapshot();
        timer = new Timer();
        alarmClock = new AlarmClock(this);
        timer.scheduleAtFixedRate(alarmClock, 0, TIME);
        String previewKey = previewTabKey();
        ensureBodyPanel(previewKey);
        setBodyMessage(previewKey, ShellUi.mutedLabel("Capturando tela…"));
        showBodyCard(previewKey);
    }

    private void refreshOutboundBodyForTab(String key) {
        if (key == null || PluginSessionKeys.PEER_DRAFT.equals(key) || key.startsWith("view:")) {
            return;
        }
        if (isSharingForTab(key)) {
            return;
        }
        if (PluginSessionKeys.ROOM.equals(key)) {
            setBodyMessage(key, ShellUi.mutedLabel(IDLE_ROOM_HTML));
        } else if (key.startsWith("peer:")) {
            setBodyMessage(key, ShellUi.mutedLabel(IDLE_PEER_HTML));
        }
    }

    private void syncRoutingFromActiveSessionTab() {
        applyRoutingForSessionKey(sessionTabs.selectedKey());
    }

    private void applyRoutingForSessionKey(String key) {
        if (key == null) {
            routingPanel.bindRoomSession();
            routingPanel.setReceiveScopeHint(null);
            return;
        }
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            return;
        }
        if (PluginSessionKeys.ROOM.equals(key)) {
            routingPanel.bindRoomSession();
            routingPanel.setReceiveScopeHint(null);
            return;
        }
        if (key.startsWith("peer:")) {
            String ip = key.substring("peer:".length());
            String name = peerDisplayNames.getOrDefault(ip, ip);
            routingPanel.bindPeerSession(ip, name);
            routingPanel.setReceiveScopeHint(null);
            return;
        }
        if (key.startsWith("view:")) {
            String ip = peerIpsByViewKey.get(key);
            String title = key.substring("view:".length());
            ScreenCastReceiveScope scope = receiveScopes.getOrDefault(key, ScreenCastReceiveScope.BROADCAST);
            TransportMode mode = receiveTransportByViewKey.getOrDefault(
                    key,
                    transportForReceiveScope(scope)
            );
            routingPanel.bindReceiveSession(
                    ip != null ? ip : "",
                    title,
                    scope.bannerHtml(),
                    mode
            );
        }
    }

    private static TransportMode transportForReceiveScope(ScreenCastReceiveScope scope) {
        return scope == ScreenCastReceiveScope.DIRECT
                ? TransportMode.UNICAST_UDP
                : TransportMode.MULTICAST_UDP;
    }

    private void openDraftPeerTab() {
        String previous = sessionTabs.selectedKey();
        if (previous != null && !PluginSessionKeys.PEER_DRAFT.equals(previous)) {
            lastSessionKey = previous;
        }
        if (!sessionTabs.hasTab(PluginSessionKeys.PEER_DRAFT)) {
            sessionTabs.addOrSelectTab(
                    PluginSessionKeys.PEER_DRAFT,
                    "Nova conversa",
                    "Informe o IP do peer",
                    new JPanel(),
                    true
            );
        } else {
            sessionTabs.selectTab(PluginSessionKeys.PEER_DRAFT);
        }
        showBodyCard(CARD_DRAFT);
        draftPanel.focusIpField();
    }

    private void commitDraftIp(String ip) {
        if (draftPanel != null) {
            draftPanel.clearIpField();
        }
        if (sessionTabs.hasTab(PluginSessionKeys.PEER_DRAFT)) {
            sessionTabs.closeTab(PluginSessionKeys.PEER_DRAFT);
        }
        openPeerTab(ip, ip);
    }

    private void openPeerTab(String ip, String displayName) {
        String canonical = canonicalPeerIp(ip);
        String resolved = SessionPeerLabels.resolveDisplayName(connectionManager, canonical, displayName);
        String key = PluginSessionKeys.peer(canonical);
        peerDisplayNames.put(canonical, resolved);
        ensureBodyPanel(key);
        setBodyMessage(key, ShellUi.mutedLabel(IDLE_PEER_HTML));
        String tabTitle = SessionTabDirectionLabels.outboundTitle(resolved);
        if (sessionTabs.hasTab(key)) {
            sessionTabs.setTabTitle(key, tabTitle, canonical);
            sessionTabs.selectTab(key);
        } else {
            sessionTabs.addOrSelectTab(key, tabTitle, canonical, new JPanel(), true);
        }
        routingPanel.bindPeerSession(canonical, resolved);
        showBodyCard(key);
    }

    private static String canonicalPeerIp(String ip) {
        return PeerIpv4.normalize(ip);
    }

    public void focusMulticastSession() {
        ensureTodosTab();
        sharingByTabKey.put(PluginSessionKeys.ROOM, false);
        sessionTabs.selectTab(PluginSessionKeys.ROOM);
        routingPanel.bindRoomSession();
        routingPanel.selectTransport(TransportMode.MULTICAST_UDP);
        setShareToggleSelectedSilently(false);
        applySharingForActiveTab();
    }

    public void focusPeerSharing(UserPeerView peer) {
        focusPeerAddress(peer);
    }

    public void focusPeerAddress(UserPeerView peer) {
        if (peer == null || peer.isBroadcastTarget() || peer.isSelf()) {
            return;
        }
        peer.getInetAddress().ifPresent(addr -> openPeerTab(PeerIpv4.fromInetAddress(addr), peer.getDisplayName()));
    }

    private void ensureTodosTab() {
        if (!sessionTabs.hasTab(PluginSessionKeys.ROOM)) {
            sessionTabs.addOrSelectTab(
                    PluginSessionKeys.ROOM,
                    SessionTabDirectionLabels.outboundTitle("Todos"),
                    "Transmissão para todos na rede (multicast)",
                    new JPanel(),
                    false
            );
            ensureBodyPanel(PluginSessionKeys.ROOM);
            setBodyMessage(PluginSessionKeys.ROOM, ShellUi.mutedLabel(IDLE_ROOM_HTML));
        }
    }

    private void updateShareToggleLabel() {
        shareToggle.setText(shareToggle.isSelected()
                ? "Compartilhamento da sua tela (ligado)"
                : "Compartilhamento da sua tela (desligado)");
    }

    private void cancelShareTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void stopSharing() {
        if (isSharingForTab(sessionTabs.selectedKey())) {
            sendShareEndedNoticeForActiveTab();
        }
        sharingByTabKey.replaceAll((k, v) -> false);
        setShareToggleSelectedSilently(false);
        cancelShareTimer();
        refreshOutboundBodyForTab(sessionTabs.selectedKey());
    }

    public void sendScreenCastMessage() {
        String activeKey = sessionTabs.selectedKey();
        if (!isSharingForTab(activeKey)) {
            return;
        }
        syncRoutingFromActiveSessionTab();
        try {
            BufferedImage image = ScreenShot.takeAShot();
            if (image == null || image.getWidth() < 1 || image.getHeight() < 1) {
                Logger.getLogger(ScreenCastPanel.class.getName()).log(Level.WARNING, "Captura de tela vazia");
                showCaptureWarning();
                return;
            }
            boolean black = ScreenCaptureDiagnostics.isLikelyBlackFrame(image);
            BufferedImage rescaled = ImageTransformation.resize(image, MAX_WIDTH, MAX_HEIGHT);
            byte[] byteImage = ImageTransformation.compress(rescaled, compressRatio);
            if (byteImage == null || byteImage.length < 32) {
                return;
            }
            ScreenCastMessage message = new ScreenCastMessage(connectionManager.getUserName(), byteImage);

            PluginRoutingPanel.RoutingSnapshot routing = routingPanel.snapshot();
            TransportMode mode = routing.transport();
            SendTarget target = resolveSendTarget(routing);
            if (target == null || mode == null) {
                return;
            }
            int sent = MessageDispatcher.send(connectionManager, message, target, mode);
            if (sent == 0 && mode == TransportMode.MULTICAST_UDP) {
                compressRatio = Math.max(0.25f, compressRatio - 0.1f);
            }
            if (black) {
                showCaptureWarning();
            } else {
                showLocalPreview(rescaled);
            }
        } catch (AWTException | IOException ex) {
            Logger.getLogger(AlarmClock.class.getName()).log(Level.SEVERE, null, ex);
            showCaptureWarning();
        } catch (Exception ex) {
            Logger.getLogger(ScreenCastPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showCaptureWarning() {
        SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel(ScreenCaptureDiagnostics.blackFrameUserMessage());
            String key = previewTabKey();
            setBodyContent(key, label);
            showBodyCard(key);
        });
    }

    private void showLocalPreview(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            String key = previewTabKey();
            JLabel label = new JLabel(new ImageIcon(image));
            setBodyContent(key, new JScrollPane(label));
            showBodyCard(key);
        });
    }

    private SendTarget resolveSendTarget(PluginRoutingPanel.RoutingSnapshot routing) throws Exception {
        if (routing.everyoneInRoom()) {
            return SendTarget.broadcastAll();
        }
        String ip = routing.peerIp();
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        return SendTarget.singlePeer(InetAddress.getByName(ip));
    }

    public void receiveScreenCastMessage(ScreenCastMessage message, ScreenCastReceiveScope scope) {
        if (isOwnOutboundFrame(message)) {
            return;
        }
        byte[] bytes = message.getImageByte();
        String viewKey = "view:" + message.getName();
        if (bytes == null || bytes.length < 32) {
            SwingUtilities.invokeLater(() -> updateTabError(viewKey, message.getName(), "Frame vazio ou corrompido.", scope));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                if (image == null) {
                    updateTabError(viewKey, message.getName(), "Não foi possível decodificar a imagem.", scope);
                    return;
                }
                ScreenCastReceiveScope resolvedScope = scope != null ? scope : ScreenCastReceiveScope.BROADCAST;
                receiveScopes.put(viewKey, resolvedScope);
                receiveTransportByViewKey.put(viewKey, transportForReceiveScope(resolvedScope));
                resolveSenderIp(message.getName(), viewKey);
                String tabTitle = SessionTabDirectionLabels.inboundTitle(message.getName());
                sessionTabs.addTabIfAbsent(viewKey, tabTitle, message.getName(), new JPanel(), true);
                setBodyContent(viewKey, new JScrollPane(new JLabel(new ImageIcon(image))));
                if (viewKey.equals(sessionTabs.selectedKey())) {
                    showBodyCard(viewKey);
                }
            } catch (IOException ex) {
                updateTabError(viewKey, message.getName(), "Erro ao ler imagem: " + ex.getMessage(), scope);
            }
        });
    }

    private void resolveSenderIp(String senderName, String viewKey) {
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (view.isBroadcastTarget()) {
                continue;
            }
            if (senderName != null && senderName.equals(view.getDisplayName())) {
                view.getInetAddress().ifPresent(addr -> peerIpsByViewKey.put(viewKey, PeerIpv4.fromInetAddress(addr)));
                return;
            }
        }
    }

    private void updateTabError(String tabKey, String title, String error, ScreenCastReceiveScope scope) {
        ScreenCastReceiveScope resolvedScope = scope != null ? scope : ScreenCastReceiveScope.BROADCAST;
        receiveScopes.put(tabKey, resolvedScope);
        receiveTransportByViewKey.put(tabKey, transportForReceiveScope(resolvedScope));
        JLabel label = new JLabel("<html><p style='color:#c62828'>" + error + "</p></html>");
        setBodyContent(tabKey, label);
        String tabTitle = SessionTabDirectionLabels.inboundTitle(title);
        sessionTabs.addTabIfAbsent(tabKey, tabTitle, title, new JPanel(), true);
        if (tabKey.equals(sessionTabs.selectedKey())) {
            showBodyCard(tabKey);
        }
    }

    public void closeReceiveTab(String senderDisplayName) {
        if (senderDisplayName == null || senderDisplayName.isBlank()) {
            return;
        }
        String viewKey = "view:" + senderDisplayName;
        if (!sessionTabs.hasTab(viewKey)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (viewKey.equals(sessionTabs.selectedKey())) {
                String fallback = sessionTabs.hasTab(PluginSessionKeys.ROOM)
                        ? PluginSessionKeys.ROOM
                        : lastSessionKey;
                if (fallback != null && sessionTabs.hasTab(fallback) && !fallback.startsWith("view:")) {
                    sessionTabs.selectTabSilently(fallback);
                    onSessionTabSelected(fallback);
                }
            }
            peerIpsByViewKey.remove(viewKey);
            receiveScopes.remove(viewKey);
            receiveTransportByViewKey.remove(viewKey);
            bodyPanels.remove(viewKey);
            sessionTabs.closeTab(viewKey);
        });
    }

    private void sendShareEndedNoticeForActiveTab() {
        PluginRoutingPanel.RoutingSnapshot snap = lastShareRoutingSnapshot != null
                ? lastShareRoutingSnapshot
                : routingPanel.snapshot();
        sendShareEndedNotice(snap);
    }

    private void sendShareEndedNotice(PluginRoutingPanel.RoutingSnapshot routing) {
        if (pluginId == null || routing == null) {
            return;
        }
        TransportMode mode = routing.transport();
        if (mode == null) {
            return;
        }
        try {
            SessionNoticeScope scope = routing.everyoneInRoom()
                    ? SessionNoticeScope.BROADCAST
                    : SessionNoticeScope.DIRECT;
            String localIp = PeerIpv4.normalize(LocalNetworkInfo.bestLocalIpv4());
            SessionNoticeMessage notice = new SessionNoticeMessage(
                    pluginId,
                    SessionNoticePhase.END,
                    SessionNoticeChannel.SCREEN_CAST,
                    connectionManager.getUserName(),
                    localIp,
                    scope,
                    mode
            );
            SendTarget target = resolveSendTarget(routing);
            if (target != null) {
                MessageDispatcher.send(connectionManager, notice, target, mode);
            }
        } catch (Exception ex) {
            Logger.getLogger(ScreenCastPanel.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        lastShareRoutingSnapshot = null;
    }

    private void ensureBodyPanel(String key) {
        if (!bodyPanels.containsKey(key)) {
            JPanel panel = new JPanel(new BorderLayout());
            bodyPanels.put(key, panel);
            bodyCards.add(panel, key);
        }
    }

    private void setBodyContent(String key, java.awt.Component content) {
        ensureBodyPanel(key);
        JPanel panel = bodyPanels.get(key);
        panel.removeAll();
        panel.add(content, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void setBodyMessage(String key, JLabel label) {
        setBodyContent(key, label);
    }

    private String previewTabKey() {
        String selected = sessionTabs.selectedKey();
        if (selected != null && !PluginSessionKeys.PEER_DRAFT.equals(selected)) {
            return selected;
        }
        return PluginSessionKeys.ROOM;
    }

    private boolean isOwnOutboundFrame(ScreenCastMessage message) {
        if (message == null || message.getName() == null) {
            return false;
        }
        String localName = connectionManager.getUserName();
        return localName != null && localName.equals(message.getName().trim());
    }

    private void showBodyCard(String card) {
        bodyLayout.show(bodyCards, card);
    }
}
