package pitiupi.presentation.shell;

import pitiupi.application.messaging.MessagingDestinationUi;
import pitiupi.application.messaging.PluginTransportCatalog;
import pitiupi.application.messaging.TransportMode;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import pitiupi.presentation.shell.ui.ShellUi;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Transporte e contexto da sessão ativa (aba). Destino vem da aba lateral / lista de sessões.
 */
public final class PluginRoutingPanel extends JPanel {

    public record RoutingSnapshot(
            boolean everyoneInRoom,
            String peerIp,
            TransportMode transport,
            boolean privateTcpOn
    ) {
    }

    private final PluginTransportCatalog.PluginKind pluginKind;
    private final JLabel sessionBanner = new JLabel();
    private final JLabel receiveScopeHint = new JLabel();
    private final JLabel destinationModeHint = new JLabel();
    private final JLabel ipLabel = new JLabel("IP do peer:");
    private final JTextField ipField = new JTextField(18);
    private final JComboBox<TransportMode> transportCombo = new JComboBox<>();
    private final JToggleButton privateTcpToggle = new JToggleButton("TCP privado (desligado)");
    private final JLabel ipHint;
    private final JPanel rowPeer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final JPanel rowHint = new JPanel(new BorderLayout());
    private final List<Consumer<RoutingSnapshot>> listeners = new ArrayList<>();
    private Consumer<Boolean> privateTcpListener = on -> {
    };
    private boolean everyoneInRoom = true;
    private TransportMode lastRoomTransport = TransportMode.MULTICAST_UDP;
    private TransportMode lastPeerTransport = TransportMode.UNICAST_UDP;

    public PluginRoutingPanel(
            PluginTransportCatalog.PluginKind pluginKind,
            String peerHintHtml,
            boolean showPrivateTcpToggle
    ) {
        this.pluginKind = pluginKind;
        this.ipHint = new JLabel(peerHintHtml);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        ShellUi.applySectionTitleBorder(this, "Destino e transporte");
        buildUi(showPrivateTcpToggle);
        bindRoomSession();
    }

    public void addRoutingListener(Consumer<RoutingSnapshot> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void setPrivateTcpListener(Consumer<Boolean> listener) {
        this.privateTcpListener = listener != null ? listener : on -> {
        };
    }

    public RoutingSnapshot snapshot() {
        String ip = ipField.getText().trim();
        TransportMode mode = (TransportMode) transportCombo.getSelectedItem();
        return new RoutingSnapshot(everyoneInRoom, ip, mode, privateTcpToggle.isSelected() && !everyoneInRoom);
    }

    public void bindRoomSession() {
        everyoneInRoom = true;
        ipField.setText("");
        sessionBanner.setText("<html><b>Sessão:</b> Sala geral (Todos)</html>");
        setReceiveScopeHint(null);
        applyRoutingState();
    }

    public void bindPeerSession(String ip, String displayName) {
        bindPeerSession(ip, displayName, null);
    }

    public void bindPeerSession(String ip, String displayName, String receiveScopeHtml) {
        everyoneInRoom = false;
        String normalized = PeerIpv4.normalize(ip != null ? ip : "");
        if (!normalized.isEmpty()) {
            ipField.setText(normalized);
        } else if (ip != null) {
            ipField.setText(ip);
        }
        String who = displayName != null && !displayName.isBlank() ? displayName : normalized;
        sessionBanner.setText(SessionPeerLabels.sessionBannerHtml(who, normalized.isEmpty() ? ip : normalized));
        setReceiveScopeHint(receiveScopeHtml);
        applyRoutingState();
    }

    public void setReceiveScopeHint(String html) {
        if (html == null || html.isBlank()) {
            receiveScopeHint.setText("");
            receiveScopeHint.setVisible(false);
        } else {
            receiveScopeHint.setText(html);
            receiveScopeHint.setVisible(true);
        }
    }

    public void setPrivateTcpOn(boolean on) {
        privateTcpToggle.setSelected(on);
        updatePrivateToggleLabel();
        applyRoutingState();
        privateTcpListener.accept(on);
    }

    public void selectTransport(TransportMode mode) {
        if (mode != null) {
            transportCombo.setSelectedItem(mode);
            rememberSelectedTransport(mode);
        }
        updateDestinationModeHint();
        fireRoutingChanged();
    }

    private void buildUi(boolean showPrivateTcpToggle) {
        JPanel rowSession = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        rowSession.add(sessionBanner);
        rowSession.add(destinationModeHint);
        receiveScopeHint.setVisible(false);
        rowSession.add(receiveScopeHint);

        privateTcpToggle.setFocusPainted(false);
        transportCombo.addActionListener(e -> {
            TransportMode selected = (TransportMode) transportCombo.getSelectedItem();
            rememberSelectedTransport(selected);
            updateDestinationModeHint();
            fireRoutingChanged();
        });

        rowPeer.add(ipLabel);
        rowPeer.add(ipField);
        rowPeer.add(new JLabel("Forma de envio:"));
        rowPeer.add(transportCombo);

        privateTcpToggle.setVisible(showPrivateTcpToggle);
        privateTcpToggle.setToolTipText(
                "Ligado: conversa só entre vocês, em conexão dedicada (TCP privado). "
                        + "Desligado: use a forma de envio escolhida ao lado."
        );
        privateTcpToggle.addActionListener(e -> {
            updatePrivateToggleLabel();
            updateDestinationModeHint();
            privateTcpListener.accept(privateTcpToggle.isSelected());
            applyRoutingState();
        });
        if (showPrivateTcpToggle) {
            rowPeer.add(privateTcpToggle);
        }

        rowHint.setBorder(BorderFactory.createEmptyBorder(0, 8, 2, 8));
        rowHint.add(ipHint, BorderLayout.CENTER);

        add(rowSession);
        add(rowPeer);
        add(rowHint);

        ipField.addActionListener(e -> applyRoutingState());
    }

    private void applyRoutingState() {
        ipLabel.setVisible(!everyoneInRoom);
        ipField.setVisible(!everyoneInRoom);
        ipField.setEnabled(!everyoneInRoom);
        privateTcpToggle.setEnabled(!everyoneInRoom);
        rowHint.setVisible(!everyoneInRoom);
        if (everyoneInRoom) {
            privateTcpToggle.setSelected(false);
            updatePrivateToggleLabel();
        }
        boolean privateOn = privateTcpToggle.isSelected();
        transportCombo.setEnabled(!everyoneInRoom && (!privateTcpToggle.isVisible() || !privateOn));

        List<TransportMode> modes = everyoneInRoom
                ? PluginTransportCatalog.forBroadcast(pluginKind)
                : PluginTransportCatalog.forSinglePeer(pluginKind);
        DefaultComboBoxModel<TransportMode> model = new DefaultComboBoxModel<>();
        for (TransportMode mode : modes) {
            model.addElement(mode);
        }
        transportCombo.setModel(model);
        TransportMode preferred = everyoneInRoom ? lastRoomTransport : lastPeerTransport;
        if (preferred != null && modes.contains(preferred)) {
            transportCombo.setSelectedItem(preferred);
        } else if (!modes.isEmpty()) {
            transportCombo.setSelectedIndex(0);
            rememberSelectedTransport((TransportMode) transportCombo.getSelectedItem());
        }
        updateDestinationModeHint();
        fireRoutingChanged();
    }

    private void updateDestinationModeHint() {
        RoutingSnapshot snap = snapshot();
        String line = MessagingDestinationUi.summarizeSendMode(
                snap.everyoneInRoom(),
                snap.transport(),
                snap.privateTcpOn()
        );
        destinationModeHint.setText("<html><i>— " + line + "</i></html>");
    }

    private void updatePrivateToggleLabel() {
        privateTcpToggle.setText(privateTcpToggle.isSelected()
                ? "TCP privado (ligado)"
                : "TCP privado (desligado)");
    }

    private void fireRoutingChanged() {
        RoutingSnapshot snap = snapshot();
        for (Consumer<RoutingSnapshot> listener : listeners) {
            listener.accept(snap);
        }
    }

    private void rememberSelectedTransport(TransportMode mode) {
        if (mode == null) {
            return;
        }
        if (everyoneInRoom) {
            lastRoomTransport = mode;
        } else {
            lastPeerTransport = mode;
        }
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;");
    }
}
