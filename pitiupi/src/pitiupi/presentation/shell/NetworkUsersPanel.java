package pitiupi.presentation.shell;

import pitiupi.application.activity.PeerActivityInbox;
import pitiupi.application.peeractions.GroupedPeerActions;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionBroker;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.control.PeerListener;
import pitiupi.models.Menus.MenuCategory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lista lateral com Todos, badges e peer actions.
 */
public final class NetworkUsersPanel extends JPanel implements PeerListener {

    private final DefaultListModel<DisplayPeer> listModel = new DefaultListModel<>();
    private final JList<DisplayPeer> userList = new JList<>(listModel);
    private final JPopupMenu menu = new JPopupMenu();
    private final Map<MenuCategory, JMenu> legacyCategories = new HashMap<>();
    private final ConnectionManager connectionManager;

    public NetworkUsersPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));

        for (MenuCategory category : MenuCategory.values()) {
            JMenu cat = new JMenu(category.toString());
            menu.add(cat);
            legacyCategories.put(category, cat);
        }

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
                    label.setText(ShellUi.formatPeerListLabel(
                            peer.view.getDisplayName(),
                            peer.view.getNotificationCount(),
                            peer.view.hasActivityIndicator()
                    ));
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
        connectionManager.getInviteInboxService().setOnNotificationsChanged(this::refreshFromPresence);
        connectionManager.getPeerActivityInbox().setOnChanged(this::refreshFromPresence);
    }

    private static boolean isIndicatorAction(String actionId) {
        return actionId != null && actionId.startsWith("screencast.");
    }

    private void showPeerActions(DisplayPeer peer, PeerActionTarget target, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        PeerActionBroker broker = connectionManager.getPeerActionBroker();
        PeerTarget peerTarget = target == PeerActionTarget.ALL_PEERS
                ? new PeerTarget.All()
                : new PeerTarget.Single(peer.view);

        PeerActivityInbox activity = connectionManager.getPeerActivityInbox();
        String activityPeerKey = peer.view.isBroadcastTarget()
                ? PeerActivityInbox.BROADCAST_PEER_KEY
                : peer.view.getInstanceId();

        for (GroupedPeerActions group : broker.getAllActionsFor(target)) {
            JMenu pluginMenu = new JMenu(group.pluginDisplayName());
            for (PeerAction action : group.actions()) {
                int pending = activityPeerKey != null
                        ? activity.countForAction(activityPeerKey, group.pluginId(), action.getId())
                        : 0;
                boolean indicator = activityPeerKey != null
                        && activity.hasIndicatorForAction(activityPeerKey, group.pluginId(), action.getId());
                boolean indicatorOnly = indicator && pending == 0 && isIndicatorAction(action.getId());
                JMenuItem item = new JMenuItem(
                        ShellUi.formatActionLabelWithBadge(action.getLabel(), pending, indicatorOnly)
                );
                item.addActionListener(e -> {
                    if (activityPeerKey != null) {
                        activity.clearAction(activityPeerKey, group.pluginId(), action.getId());
                    }
                    broker.setActivePlugin(group.pluginId());
                    action.getHandler().run(peerTarget);
                });
                pluginMenu.add(item);
            }
            popup.add(pluginMenu);
        }
        if (peer.view.getNotificationCount() > 0 && !peer.view.isBroadcastTarget()) {
            JMenuItem accept = new JMenuItem("Aceitar convite");
            accept.addActionListener(e -> {
                String pluginId = connectionManager.getInviteInboxService()
                        .acceptFirstInviteFrom(peer.view.getInstanceId());
                if (pluginId != null) {
                    connectionManager.getPeerActionBroker().setActivePlugin(pluginId);
                    connectionManager.notifyInviteAcceptedForUi(pluginId);
                }
            });
            popup.add(accept);
        }
        for (JMenu legacy : legacyCategories.values()) {
            if (legacy.getItemCount() > 0 && target == PeerActionTarget.SINGLE_PEER && !peer.view.isBroadcastTarget()) {
                popup.addSeparator();
                JMenu copy = new JMenu(legacy.getText());
                for (int i = 0; i < legacy.getItemCount(); i++) {
                    copy.add(legacy.getItem(i));
                }
                popup.add(copy);
            }
        }
        if (popup.getComponentCount() > 0) {
            popup.show(userList, x, y);
        }
    }

    public void initUnicastMenu(JMenuItem menuItem, Consumer<User> action) {
        menuItem.addActionListener(e -> {
            DisplayPeer selected = userList.getSelectedValue();
            if (selected == null || selected.view.isBroadcastTarget()) {
                return;
            }
            User user = selected.toUser();
            if (user.getAddress() != null) {
                action.accept(user);
            }
        });
    }

    public void addUnicastMenu(MenuCategory category, JMenuItem menuItem) {
        legacyCategories.get(category).add(menuItem);
    }

    @Override
    public void onPeersChanged(List<User> peers) {
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

    record DisplayPeer(UserPeerView view) {
        @Override
        public String toString() {
            String name = view.getDisplayName();
            if (view.getNotificationCount() > 0) {
                return name + " (" + view.getNotificationCount() + ")";
            }
            return name;
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
