package pitiupi.chat.GUI;

import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.PluginTransportCatalog;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.application.presence.UserPeerView;
import pitiupi.chat.control.PrivateChatController;
import pitiupi.chat.control.PrivateChatUiSink;
import pitiupi.chat.net.ChatMessage;
import pitiupi.chat.net.LegacyMpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.net.tcp.TcpAttemptListener;
import pitiupi.models.User;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerIpv4;
import pitiupi.presentation.shell.LocalUserIdentity;
import pitiupi.presentation.shell.PeerDraftPanel;
import pitiupi.presentation.shell.PluginRoutingPanel;
import pitiupi.presentation.shell.PluginSessionKeys;
import pitiupi.presentation.shell.PluginSessionWorkspace;
import pitiupi.presentation.shell.SessionPeerLabels;
import pitiupi.presentation.shell.SessionTabBar;
import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPanel extends JPanel implements PrivateChatUiSink {

    private static final String ROOM_KEY = "room";
    private static final String CARD_MAIN = "main";
    private static final String CARD_DRAFT = "draft";

    private final JEditorPane transcript = new JEditorPane();
    private final JTextArea input = new JTextArea();
    private final JLabel tcpAttemptLabel = ShellUi.mutedLabel(" ");
    private final ConnectionManager connectionManager;
    private final PluginSessionWorkspace workspace;
    private final PluginRoutingPanel routingPanel;
    private final SessionTabBar sessionTabs;
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel bodyCards = new JPanel(bodyLayout);
    private String lastSessionKey = PluginSessionKeys.ROOM;
    private final Map<String, StringBuilder> transcripts = new HashMap<>();
    private final Map<String, String> peerDisplayNames = new HashMap<>();
    private String activeConversationKey = ROOM_KEY;
    private PrivateChatController privateChatController;
    private PeerDraftPanel draftPanel;
    private Runnable pendingTranscriptFlush;
    public ChatPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.routingPanel = new PluginRoutingPanel(
                PluginTransportCatalog.PluginKind.CHAT,
                "<html><i>Use o IP do peer ou escolha alguém na lista lateral → Conversar. "
                        + "Envios usam a porta do login (barra de status); na sala, mesma porta para todos.</i></html>",
                true
        );
        routingPanel.setToolTipText(
                "UDP/TCP unicast usa o IP do peer e a porta " + connectionManager.getPort()
                        + " (a que você informou ao entrar)."
        );
        this.workspace = new PluginSessionWorkspace(routingPanel, null);
        this.sessionTabs = workspace.getSessionTabBar();
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildUi();
        initSessionTabs();
        routingPanel.addRoutingListener(s -> applyConversationKeyFromRouting());
        connectionManager.addPeerListener(peers ->
                SwingUtilities.invokeLater(() -> adoptCurrentPeerAddresses(peers)));
        routingPanel.setPrivateTcpListener(selected -> {
            if (selected) {
                User user = resolveUserForSingle();
                if (user != null && privateChatController != null) {
                    privateChatController.openChat(user);
                }
            }
            applyConversationKeyFromRouting();
        });
        selectRoomSession();
    }

    private void initSessionTabs() {
        sessionTabs.addOrSelectTab(
                PluginSessionKeys.ROOM,
                "Sala geral",
                "Sala geral (Todos)",
                new JPanel(),
                false
        );
        sessionTabs.setCloseFallbackKey(PluginSessionKeys.ROOM);
        sessionTabs.setOnTabSelected(this::onSessionTabSelected);
        sessionTabs.setOnAddPeerRequested(this::openDraftPeerTab);
        sessionTabs.setOnTabClosed(this::onSessionTabClosed);
    }

    private void onSessionTabClosed(String key) {
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            if (draftPanel != null) {
                draftPanel.clearIpField();
            }
            SwingUtilities.invokeLater(this::restoreSessionAfterDraftClosed);
            return;
        }
        if (key != null && key.startsWith("peer:")) {
            peerDisplayNames.remove(canonicalPeerIp(key.substring("peer:".length())));
        }
    }

    /**
     * Uma pessoa que troca de cabo para Wi-Fi muda de IP e manteria duas abas com o mesmo nome.
     * A aba aberta no endereço antigo passa a ser a do endereço atual.
     */
    private void adoptCurrentPeerAddresses(List<User> peers) {
        if (peers == null) {
            return;
        }
        Map<String, User> byIp = new HashMap<>();
        for (User peer : peers) {
            if (peer == null || peer.isBroadcastTarget() || peer.getAddress() == null) {
                continue;
            }
            byIp.put(canonicalPeerIp(PeerIpv4.fromInetAddress(peer.getAddress())), peer);
        }
        List<String> openIps = new ArrayList<>(peerDisplayNames.keySet());
        for (User peer : byIp.values()) {
            String newIp = canonicalPeerIp(PeerIpv4.fromInetAddress(peer.getAddress()));
            long sameName = byIp.values().stream()
                    .filter(other -> peer.getUserName().equals(other.getUserName()))
                    .count();
            if (sameName != 1) {
                continue;
            }
            for (String oldIp : openIps) {
                if (oldIp.equals(newIp) || byIp.containsKey(oldIp)) {
                    continue;
                }
                String title = peerDisplayNames.get(oldIp);
                if (title != null && title.equals(peer.getUserName())
                        && sessionTabs.hasTab(PluginSessionKeys.peer(oldIp))) {
                    retargetPeerSession(oldIp, newIp, peer.getUserName());
                }
            }
        }
    }

    private void retargetPeerSession(String oldIp, String newIp, String name) {
        String oldTab = PluginSessionKeys.peer(oldIp);
        String newTab = PluginSessionKeys.peer(newIp);
        mergeTranscript(peerKey(oldIp), peerKey(newIp));
        mergeTranscript(privateKey(oldIp), privateKey(newIp));
        String selectedBefore = sessionTabs.selectedKey();
        boolean wasSelected = oldTab.equals(selectedBefore);
        if (sessionTabs.hasTab(newTab)) {
            sessionTabs.setTabTitle(newTab, name, newIp);
            sessionTabs.closeTab(oldTab);
            String restore = wasSelected ? newTab : selectedBefore;
            if (restore != null && sessionTabs.hasTab(restore)) {
                sessionTabs.selectTab(restore);
            }
        } else {
            sessionTabs.replaceKey(oldTab, newTab, name, newIp, true);
        }
        peerDisplayNames.remove(oldIp);
        peerDisplayNames.put(newIp, name);
        if (oldTab.equals(lastSessionKey)) {
            lastSessionKey = newTab;
        }
        if (peerKey(oldIp).equals(activeConversationKey)) {
            activeConversationKey = peerKey(newIp);
            flushTranscriptToPane();
        } else if (privateKey(oldIp).equals(activeConversationKey)) {
            activeConversationKey = privateKey(newIp);
            flushTranscriptToPane();
        }
        if (wasSelected) {
            routingPanel.bindPeerSession(newIp, name);
        }
    }

    private void mergeTranscript(String oldBucket, String newBucket) {
        StringBuilder oldBuf = transcripts.remove(oldBucket);
        if (oldBuf == null || oldBuf.length() == 0) {
            return;
        }
        StringBuilder current = transcripts.computeIfAbsent(newBucket, key -> new StringBuilder());
        current.append(oldBuf);
    }

    private void applyConversationKeyFromRouting() {
        String next = resolveConversationKey();
        if (!next.equals(activeConversationKey)) {
            switchActiveConversation(next);
        }
    }

    private void onSessionTabSelected(String key) {
        if (key == null) {
            return;
        }
        lastSessionKey = key;
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            showBodyCard(CARD_DRAFT);
            if (draftPanel != null) {
                draftPanel.focusIpField();
            }
            return;
        }
        showBodyCard(CARD_MAIN);
        if (PluginSessionKeys.ROOM.equals(key)) {
            routingPanel.bindRoomSession();
            switchActiveConversation(ROOM_KEY);
            return;
        }
        if (key.startsWith("peer:") && !PluginSessionKeys.PEER_DRAFT.equals(key)) {
            String ip = key.substring("peer:".length());
            String name = peerDisplayNames.getOrDefault(ip, ip);
            routingPanel.bindPeerSession(ip, name);
            switchActiveConversation(resolveConversationKey());
        }
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
        if (draftPanel != null) {
            draftPanel.focusIpField();
        }
    }

    private void commitDraftIp(String ip) {
        if (draftPanel != null) {
            draftPanel.clearIpField();
        }
        if (sessionTabs.hasTab(PluginSessionKeys.PEER_DRAFT)) {
            sessionTabs.closeTab(PluginSessionKeys.PEER_DRAFT);
        }
        showBodyCard(CARD_MAIN);
        openPeerTab(canonicalPeerIp(ip), ip);
    }

    private void syncRoutingFromActiveSessionTab() {
        String key = sessionTabs.selectedKey();
        if (PluginSessionKeys.ROOM.equals(key)) {
            routingPanel.bindRoomSession();
            if (!ROOM_KEY.equals(activeConversationKey)) {
                switchActiveConversation(ROOM_KEY);
            }
            return;
        }
        if (key != null && key.startsWith("peer:") && !PluginSessionKeys.PEER_DRAFT.equals(key)) {
            String ip = key.substring("peer:".length());
            String name = peerDisplayNames.getOrDefault(ip, ip);
            routingPanel.bindPeerSession(ip, name);
            String conv = resolveConversationKey();
            if (!conv.equals(activeConversationKey)) {
                switchActiveConversation(conv);
            }
        }
    }

    private void restoreSessionAfterDraftClosed() {
        showBodyCard(CARD_MAIN);
        String restore = lastSessionKey != null && sessionTabs.hasTab(lastSessionKey)
                ? lastSessionKey
                : PluginSessionKeys.ROOM;
        if (PluginSessionKeys.PEER_DRAFT.equals(restore)) {
            restore = PluginSessionKeys.ROOM;
        }
        sessionTabs.selectTabSilently(restore);
        if (PluginSessionKeys.ROOM.equals(restore)) {
            routingPanel.bindRoomSession();
            switchActiveConversation(ROOM_KEY);
        } else if (restore.startsWith("peer:")) {
            String ip = restore.substring("peer:".length());
            String name = peerDisplayNames.getOrDefault(ip, ip);
            routingPanel.bindPeerSession(ip, name);
            switchActiveConversation(resolveConversationKey());
        }
    }

    private void openPeerTab(String ip, String displayName) {
        String canonical = canonicalPeerIp(ip);
        String resolved = SessionPeerLabels.resolveDisplayName(connectionManager, canonical, displayName);
        peerDisplayNames.put(canonical, resolved);
        String tabKey = PluginSessionKeys.peer(canonical);
        if (sessionTabs.hasTab(tabKey)) {
            sessionTabs.setTabTitle(tabKey, resolved, canonical);
            sessionTabs.selectTab(tabKey);
        } else {
            sessionTabs.addOrSelectTab(tabKey, resolved, canonical, new JPanel(), true);
        }
        routingPanel.bindPeerSession(canonical, resolved);
        switchActiveConversation(resolveConversationKey());
        showBodyCard(CARD_MAIN);
    }

    private void ensurePeerTabForIncoming(String ip, String displayName) {
        String canonical = canonicalPeerIp(ip);
        String resolved = SessionPeerLabels.resolveDisplayName(connectionManager, canonical, displayName);
        peerDisplayNames.put(canonical, resolved);
        String tabKey = PluginSessionKeys.peer(canonical);
        sessionTabs.addTabIfAbsent(tabKey, resolved, canonical, new JPanel(), true);
        if (tabKey.equals(sessionTabs.selectedKey())) {
            routingPanel.bindPeerSession(canonical, resolved);
            switchActiveConversation(resolveConversationKey());
        }
    }

    private static String canonicalPeerIp(String ip) {
        return PeerIpv4.normalize(ip);
    }

    private void selectRoomSession() {
        routingPanel.bindRoomSession();
        switchActiveConversation(ROOM_KEY);
        showBodyCard(CARD_MAIN);
    }

    public void setPrivateChatController(PrivateChatController privateChatController) {
        this.privateChatController = privateChatController;
    }

    public void focusGeneralRoom() {
        sessionTabs.selectTab(PluginSessionKeys.ROOM);
    }

    public void focusPeer(UserPeerView peer) {
        if (peer == null || peer.isBroadcastTarget() || peer.isSelf()) {
            focusGeneralRoom();
            return;
        }
        peer.getInetAddress().ifPresent(addr ->
                openPeerTab(PeerIpv4.fromInetAddress(addr), peer.getDisplayName()));
    }

    public void enablePrivateTcpForPeer(UserPeerView peer) {
        focusPeer(peer);
        routingPanel.setPrivateTcpOn(true);
        switchActiveConversation(resolveConversationKey());
    }

    public void deliverIncomingMessage(ChatMessage message) {
        if (isEchoFromLocalUser(message)) {
            return;
        }
        String bucket = bucketForIncoming(message);
        if (!ROOM_KEY.equals(bucket) && message.getSenderIp() != null && !message.getSenderIp().isBlank()) {
            String canonical = canonicalPeerIp(message.getSenderIp());
            ensurePeerTabForIncoming(canonical, message.getName());
            bucket = peerKey(canonical);
        }
        appendChatLine(bucket, message);
    }

    @Override
    public void onPrivateChannelReady(String peerIp) {
        SwingUtilities.invokeLater(() -> {
            String canonical = canonicalPeerIp(peerIp);
            String resolved = SessionPeerLabels.resolveDisplayName(connectionManager, canonical, peerIp);
            ensurePeerTabForIncoming(canonical, resolved);
            appendSystemLine(peerKey(canonical), "Canal TCP privado conectado com " + resolved + ".");
            if (peerKey(canonical).equals(activeConversationKey)) {
                flushTranscriptToPane();
            }
        });
    }

    @Override
    public void onPrivateChannelClosed(String peerIp) {
        SwingUtilities.invokeLater(() -> {
            appendSystemLine(peerKey(canonicalPeerIp(peerIp)), "Canal TCP privado encerrado (" + peerIp + ").");
            if (peerKey(canonicalPeerIp(peerIp)).equals(activeConversationKey)) {
                flushTranscriptToPane();
            }
        });
    }

    @Override
    public void appendPrivateLine(String peerIp, LegacyMpChatMessage message, boolean outgoing) {
        SwingUtilities.invokeLater(() -> {
            String canonical = canonicalPeerIp(peerIp);
            if (!outgoing) {
                ensurePeerTabForIncoming(canonical, message.getNome());
            }
            appendLegacyLine(peerKey(canonical), message);
        });
    }

    private void buildUi() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.84);
        split.setOneTouchExpandable(true);
        transcript.setContentType("text/html");
        transcript.setEditable(false);

        input.setColumns(20);
        input.setRows(2);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        input.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JButton send = new JButton("Enviar");
        send.setFocusPainted(false);
        send.addActionListener(e -> sendMessage());

        JScrollPane inputScroll = new JScrollPane(input);
        inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ShellUi.borderlessScroll(inputScroll);

        tcpAttemptLabel.setVisible(false);
        JPanel compose = new JPanel(new BorderLayout(0, 4));
        compose.setPreferredSize(new Dimension(0, 88));
        compose.add(tcpAttemptLabel, BorderLayout.NORTH);
        compose.add(inputScroll, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.add(send);
        compose.add(actions, BorderLayout.SOUTH);

        split.setTopComponent(new JScrollPane(transcript));
        split.setBottomComponent(compose);
        split.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                    && split.isShowing()
                    && split.getHeight() > 0) {
                split.setDividerLocation(0.84);
            }
        });

        draftPanel = new PeerDraftPanel(
                "Nova conversa por IP",
                "Informe o endereço IP do peer. A aba será atualizada com o nome quando a conversa abrir.",
                this::commitDraftIp
        );

        bodyCards.add(split, CARD_MAIN);
        bodyCards.add(draftPanel, CARD_DRAFT);
        workspace.getBodyHost().add(bodyCards, BorderLayout.CENTER);

        add(workspace, BorderLayout.CENTER);
    }

    private void showBodyCard(String card) {
        bodyLayout.show(bodyCards, card);
    }

    private void sendMessage() {
        if (input.getText().trim().isEmpty()) {
            return;
        }
        syncRoutingFromActiveSessionTab();
        String text = input.getText();
        PluginRoutingPanel.RoutingSnapshot routing = routingPanel.snapshot();
        try {
            if (routing.privateTcpOn() && !routing.everyoneInRoom()) {
                String peerIp = canonicalPeerIp(routing.peerIp());
                if (LocalNetworkInfo.isLocalIpv4(peerIp)) {
                    appendSystemLine(
                            activeConversationKey,
                            "Canal TCP privado com você mesmo não está disponível; use sala geral ou UDP unicast."
                    );
                    return;
                }
                User user = resolveUserForSingle();
                if (user == null) {
                    appendSystemLine(activeConversationKey, "Informe o IP do peer para o canal TCP privado.");
                    return;
                }
                if (privateChatController != null && privateChatController.hasOpenChannel(user)) {
                    if (privateChatController.sendText(user, text)) {
                        input.setText("");
                    }
                    return;
                }
                if (privateChatController != null) {
                    privateChatController.openChat(user);
                    if (privateChatController.sendText(user, text)) {
                        input.setText("");
                    }
                }
                return;
            }

            TransportMode mode = routing.transport();
            if (mode == TransportMode.TCP) {
                sendTcpOffEdt(text, routing);
                return;
            }
            SendTarget target = resolveSendTarget(routing);
            if (target == null || mode == null) {
                appendSystemLine(activeConversationKey, "Informe um IP válido para enviar a um peer.");
                return;
            }
            String ip = PeerIpv4.normalize(LocalNetworkInfo.bestLocalIpv4());
            ChatMessage message = new ChatMessage(
                    connectionManager.getUserName(),
                    text,
                    mode.getLabel(),
                    ip
            );
            int sent = MessageDispatcher.send(connectionManager, message, target, mode);
            appendChatLine(activeConversationKey, message);
            if (!routing.everyoneInRoom()) {
                String peerIp = canonicalPeerIp(routing.peerIp());
                if (LocalNetworkInfo.isLocalIpv4(peerIp) && sent == 0) {
                    if (mode == TransportMode.TCP) {
                        appendSystemLine(
                                activeConversationKey,
                                "TCP para o próprio host não é suportado; use sala geral ou UDP unicast."
                        );
                    } else {
                        appendSystemLine(
                                activeConversationKey,
                                "Mensagem registrada aqui; eco pela rede pode não voltar no mesmo PC."
                        );
                    }
                }
            }
            input.setText("");
        } catch (Exception ex) {
            appendSystemLine(activeConversationKey, "Erro ao enviar: " + ex.getMessage());
        }
    }

    public void showTcpAttempt(String text) {
        tcpAttemptLabel.setText(text);
        tcpAttemptLabel.setVisible(true);
    }

    public void clearTcpAttempt() {
        tcpAttemptLabel.setText(" ");
        tcpAttemptLabel.setVisible(false);
    }

    public void failTcpAttempt(String message) {
        clearTcpAttempt();
        appendSystemLine(activeConversationKey, message);
        flushTranscriptToPane();
    }

    private void sendTcpOffEdt(String text, PluginRoutingPanel.RoutingSnapshot routing) {
        String localIp = PeerIpv4.normalize(LocalNetworkInfo.bestLocalIpv4());
        ChatMessage message = new ChatMessage(
                connectionManager.getUserName(),
                text,
                TransportMode.TCP.getLabel(),
                localIp
        );
        if (routing.everyoneInRoom()) {
            sendTcpToRoom(message);
            return;
        }
        String peerIp = canonicalPeerIp(routing.peerIp());
        if (peerIp == null || peerIp.isEmpty()) {
            appendSystemLine(activeConversationKey, "Informe um IP válido para enviar a um peer.");
            return;
        }
        if (LocalNetworkInfo.isLocalIpv4(peerIp)) {
            appendSystemLine(activeConversationKey, "TCP para o próprio host não é suportado; use sala geral ou UDP unicast.");
            return;
        }
        connectionManager.sendTcpAsync(message, peerIp, new TcpAttemptListener() {
            @Override
            public void onConnecting(String endpoint) {
                showTcpAttempt("Conectando em " + endpoint + "…");
            }

            @Override
            public void onSuccess(pitiupi.net.tcp.TcpConnection connection) {
                clearTcpAttempt();
                appendChatLine(activeConversationKey, message);
                input.setText("");
                flushTranscriptToPane();
            }

            @Override
            public void onFailure(String error) {
                failTcpAttempt(error);
            }
        });
    }

    private void sendTcpToRoom(ChatMessage message) {
        java.util.List<String> peers = new java.util.ArrayList<>();
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (view.isBroadcastTarget() || view.isSelf()) {
                continue;
            }
            view.getInetAddress().ifPresent(address -> peers.add(PeerIpv4.fromInetAddress(address)));
        }
        if (peers.isEmpty()) {
            appendSystemLine(activeConversationKey, "Nenhum colega na sala para receber por TCP.");
            return;
        }
        int total = peers.size();
        int[] remaining = {total};
        int[] succeeded = {0};
        boolean[] announced = {false};
        for (String ip : peers) {
            connectionManager.sendTcpAsync(message, ip, new TcpAttemptListener() {
                @Override
                public void onConnecting(String endpoint) {
                    if (!announced[0]) {
                        announced[0] = true;
                        showTcpAttempt("Conectando por TCP…");
                    }
                }

                @Override
                public void onSuccess(pitiupi.net.tcp.TcpConnection connection) {
                    finishRoomTcp(message, total, remaining, succeeded, true);
                }

                @Override
                public void onFailure(String error) {
                    finishRoomTcp(message, total, remaining, succeeded, false);
                }
            });
        }
    }

    private void finishRoomTcp(ChatMessage message, int total, int[] remaining, int[] succeeded, boolean thisOk) {
        if (thisOk) {
            succeeded[0]++;
        }
        remaining[0]--;
        if (remaining[0] > 0) {
            return;
        }
        clearTcpAttempt();
        if (succeeded[0] == 0) {
            appendSystemLine(activeConversationKey, "Não foi possível conectar nos colegas por TCP.");
            flushTranscriptToPane();
            return;
        }
        if (succeeded[0] < total) {
            appendSystemLine(activeConversationKey, "Não foi possível conectar em um ou mais colegas por TCP.");
        }
        appendChatLine(activeConversationKey, message);
        input.setText("");
        flushTranscriptToPane();
    }

    private SendTarget resolveSendTarget(PluginRoutingPanel.RoutingSnapshot routing) throws Exception {
        if (routing.everyoneInRoom()) {
            return SendTarget.broadcastAll();
        }
        String ip = canonicalPeerIp(routing.peerIp());
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        return SendTarget.singlePeer(InetAddress.getByName(ip));
    }

    private User resolveUserForSingle() {
        try {
            String ip = canonicalPeerIp(routingPanel.snapshot().peerIp());
            if (ip == null || ip.isEmpty()) {
                return null;
            }
            InetAddress address = InetAddress.getByName(ip);
            String displayName = ip;
            for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
                if (view.getInetAddress().isPresent()
                        && view.getInetAddress().get().equals(address)) {
                    displayName = view.getDisplayName();
                    return new User(displayName, address, System.currentTimeMillis(), view.getInstanceId());
                }
            }
            return new User(displayName, address, System.currentTimeMillis(), null);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void receiveChatMessage(ChatMessage message) {
        runOnEdt(() -> deliverIncomingMessage(message));
    }

    public void receiveLegacyMessage(String tipo, String nome, String ip, String text) {
        runOnEdt(() -> {
            LegacyMpChatMessage legacy = new LegacyMpChatMessage(nome, ip, tipo, text);
            String bucket = ROOM_KEY;
            if (ip != null && !ip.isBlank()) {
                String canonical = canonicalPeerIp(ip);
                ensurePeerTabForIncoming(canonical, nome);
                bucket = peerKey(canonical);
            }
            appendLegacyLine(bucket, legacy);
        });
    }

    private static void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private void appendChatLine(String bucket, ChatMessage message) {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String tag = message.getTransportLabel() != null ? message.getTransportLabel() : "—";
        String ip = message.getSenderIp() != null ? message.getSenderIp() : "—";
        String line = dt.format(new Date()) + " " + ChatTranscriptHtml.coloredTransportTag(tag)
                + " <b>" + ChatTranscriptHtml.escape(message.getName()) + "</b> ("
                + ChatTranscriptHtml.escape(ip) + "): " + ChatTranscriptHtml.escape(message.getText());
        prependLine(bucket, line);
    }

    private void appendLegacyLine(String bucket, LegacyMpChatMessage message) {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String tag = message.getTipoMensagem() != null ? message.getTipoMensagem() : "Legado";
        String line = dt.format(new Date()) + " " + ChatTranscriptHtml.coloredTransportTag(tag)
                + " <b>" + ChatTranscriptHtml.escape(message.getNome()) + "</b> ("
                + ChatTranscriptHtml.escape(message.getIp()) + "): "
                + ChatTranscriptHtml.escape(message.getMessage());
        prependLine(bucket, line);
    }

    private void appendSystemLine(String bucket, String text) {
        prependLine(bucket, "<i>" + ChatTranscriptHtml.escape(text) + "</i>");
    }

    private void prependLine(String bucket, String htmlLine) {
        StringBuilder buf = transcripts.computeIfAbsent(bucket, k -> new StringBuilder());
        buf.insert(0, htmlLine + "<br>");
        if (bucket.equals(activeConversationKey) && isSessionTabShowingBucket(bucket)) {
            scheduleFlushTranscriptToPane();
        }
    }

    private boolean isSessionTabShowingBucket(String bucket) {
        String key = sessionTabs.selectedKey();
        if (ROOM_KEY.equals(bucket)) {
            return PluginSessionKeys.ROOM.equals(key);
        }
        if (key != null && key.startsWith("peer:") && !PluginSessionKeys.PEER_DRAFT.equals(key)) {
            String tabIp = canonicalPeerIp(key.substring("peer:".length()));
            return peerKey(tabIp).equals(bucket) || privateKey(tabIp).equals(bucket);
        }
        return false;
    }

    private void scheduleFlushTranscriptToPane() {
        if (pendingTranscriptFlush != null) {
            return;
        }
        pendingTranscriptFlush = () -> {
            pendingTranscriptFlush = null;
            flushTranscriptToPane();
        };
        SwingUtilities.invokeLater(pendingTranscriptFlush);
    }

    private void flushTranscriptToPane() {
        StringBuilder buf = transcripts.get(activeConversationKey);
        String inner = buf != null ? buf.toString() : "";
        String html = ChatTranscriptHtml.wrapDocument(inner);
        try {
            transcript.setText(html);
        } catch (RuntimeException ex) {
            resetTranscriptEditor();
            try {
                transcript.setText(html);
            } catch (RuntimeException ignored) {
                // evita quebrar a EDT se o HTML continuar inválido
            }
        }
    }

    private void resetTranscriptEditor() {
        transcript.setEditorKit(new HTMLEditorKit());
        transcript.setContentType("text/html");
        transcript.setEditable(false);
    }

    private void switchActiveConversation(String newKey) {
        if (newKey == null) {
            newKey = ROOM_KEY;
        }
        if (newKey.equals(activeConversationKey)) {
            return;
        }
        activeConversationKey = newKey;
        flushTranscriptToPane();
    }

    private String resolveConversationKey() {
        PluginRoutingPanel.RoutingSnapshot routing = routingPanel.snapshot();
        if (routing.everyoneInRoom()) {
            return ROOM_KEY;
        }
        String ip = canonicalPeerIp(routing.peerIp());
        if (ip == null || ip.isEmpty()) {
            return ROOM_KEY;
        }
        return peerKey(ip);
    }

    private static String peerKey(String ip) {
        return "peer:" + ip;
    }

    private static String privateKey(String ip) {
        return "private:" + ip;
    }

    private String bucketForIncoming(ChatMessage message) {
        String tag = message.getTransportLabel();
        if (tag != null && tag.toLowerCase().contains("multicast")) {
            return ROOM_KEY;
        }
        String ip = message.getSenderIp();
        if (ip == null || ip.isBlank()) {
            return ROOM_KEY;
        }
        return peerKey(canonicalPeerIp(ip));
    }

    private boolean isEchoFromLocalUser(ChatMessage message) {
        if (message == null) {
            return false;
        }
        return LocalUserIdentity.isLocalSender(
                connectionManager,
                message.getName(),
                message.getSenderIp()
        );
    }
}
