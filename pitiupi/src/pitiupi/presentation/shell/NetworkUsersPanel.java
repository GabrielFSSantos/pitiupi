package pitiupi.presentation.shell;

import pitiupi.application.peeractions.GroupedPeerActions;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionBroker;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;
import pitiupi.control.PeerListener;
import pitiupi.models.User;
import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Lista lateral com Todos e peer actions (sem badges de atividade na v1).
 */
public final class NetworkUsersPanel extends JPanel implements PeerListener {

    private final DefaultListModel<DisplayPeer> listModel = new DefaultListModel<>();
    private final JList<DisplayPeer> userList = new JList<>(listModel);
    private final ConnectionManager connectionManager;

    public NetworkUsersPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));

        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof DisplayPeer peer) {
                    label.setText(peer.view.getDisplayName());
                }
                return label;
            }
        });
        JScrollPane userScroll = new JScrollPane(userList);
        ShellUi.borderlessScroll(userScroll);
        add(ShellUi.sidebarPanel("Usuários nesta rede", userScroll), BorderLayout.CENTER);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }
                userList.setSelectedIndex(index);
                DisplayPeer peer = listModel.getElementAt(index);
                PeerActionTarget target = peer.view.isBroadcastTarget()
                        ? PeerActionTarget.ALL_PEERS
                        : PeerActionTarget.SINGLE_PEER;
                showPeerActions(peer, target, e.getX(), e.getY());
            }
        });

        connectionManager.addPeerListener(this);
        refreshFromPresence();
    }

    private void showPeerActions(DisplayPeer peer, PeerActionTarget target, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        PeerActionBroker broker = connectionManager.getPeerActionBroker();
        PeerTarget peerTarget = target == PeerActionTarget.ALL_PEERS
                ? new PeerTarget.All()
                : new PeerTarget.Single(peer.view);

        for (GroupedPeerActions group : broker.getAllActionsFor(target)) {
            JMenu pluginMenu = new JMenu(group.pluginDisplayName());
            for (PeerAction action : group.actions()) {
                JMenuItem item = new JMenuItem(action.getLabel());
                item.addActionListener(e -> {
                    broker.setActivePlugin(group.pluginId());
                    action.getHandler().run(peerTarget);
                });
                pluginMenu.add(item);
            }
            popup.add(pluginMenu);
        }
        if (popup.getComponentCount() > 0) {
            popup.show(userList, x, y);
        }
    }

    @Override
    public void onPeersChanged(List<pitiupi.models.User> peers) {
        refreshFromPresence();
    }

    private void refreshFromPresence() {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
                listModel.addElement(new DisplayPeer(view));
            }
        });
    }

    private record DisplayPeer(UserPeerView view) {
        @Override
        public String toString() {
            return view.getDisplayName();
        }

        User toUser() {
            return new User(
                    view.getDisplayName(),
                    view.getInetAddress().orElse(null),
                    System.currentTimeMillis(),
                    view.getInstanceId()
            );
        }
    }
}
