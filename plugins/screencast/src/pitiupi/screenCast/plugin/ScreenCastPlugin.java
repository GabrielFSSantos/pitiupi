package pitiupi.screenCast.plugin;

import java.util.List;

import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.presence.UserPeerView;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.screenCast.GUI.ScreenCastMenu;
import pitiupi.screenCast.GUI.ScreenCastPanel;
import pitiupi.screenCast.GUI.ScreenCastReceiveScope;
import pitiupi.screenCast.net.ScreenCastMessage;

public class ScreenCastPlugin extends Plugin {

    private ScreenCastPanel screenCastPanel;
    private PluginWorkspace workspace;
    private ConnectionManager connectionManager;

    @Override
    public String getName() {
        return "ScreenCast";
    }

    @Override
    public String getDisplayName() {
        return "Compartilhar tela";
    }

    @Override
    public String getAuthor() {
        return "Flávio Schiavoni";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.ScreenCast.Example";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
        this.connectionManager = connectionManager;
        this.workspace = workspace;
        this.screenCastPanel = new ScreenCastPanel(connectionManager);
        screenCastPanel.setPluginId(getIdentifier());
        bindShellWorkspaceContent(screenCastPanel);
        installPeerActions(workspace.peerActions());
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
        this.screenCastPanel = new ScreenCastPanel(connectionManager);
        guiManager.createMenu(MenuCategory.Streaming, new ScreenCastMenu(screenCastPanel));
    }

    @Override
    public void onShellTabClosed() {
        if (screenCastPanel != null) {
            screenCastPanel.stopSharing();
        }
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar) {
        registrar.register(PeerAction.of(
                "screencast.multicast",
                "Compartilhar na rede (multicast)",
                PeerActionTarget.ALL_PEERS,
                target -> {
                    workspace.openOrFocusTab(getIdentifier(), getDisplayName());
                    screenCastPanel.focusMulticastSession();
                }
        ));
        registrar.register(PeerAction.of(
                "screencast.peer",
                "Preparar compartilhamento com este peer",
                PeerActionTarget.SINGLE_PEER,
                target -> {
                    workspace.openOrFocusTab(getIdentifier(), getDisplayName());
                    if (target instanceof PeerTarget.Single single) {
                        screenCastPanel.focusPeerAddress(single.peer());
                    }
                }
        ));
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(ScreenCastMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof ScreenCastMessage msg && screenCastPanel != null) {
            if (isOwnOutboundFrame(msg)) {
                return;
            }
            ScreenCastReceiveScope scope = classifyIncoming(msg);
            screenCastPanel.receiveScreenCastMessage(msg, scope);
        }
    }

    private boolean isOwnOutboundFrame(ScreenCastMessage msg) {
        if (connectionManager == null || msg.getName() == null) {
            return false;
        }
        String localName = connectionManager.getUserName();
        return localName != null && localName.equals(msg.getName().trim());
    }

    private ScreenCastReceiveScope classifyIncoming(ScreenCastMessage msg) {
        if (connectionManager == null || msg.getName() == null) {
            return ScreenCastReceiveScope.BROADCAST;
        }
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (!view.isBroadcastTarget()
                    && msg.getName().equals(view.getDisplayName())
                    && view.getInstanceId() != null) {
                return ScreenCastReceiveScope.DIRECT;
            }
        }
        return ScreenCastReceiveScope.BROADCAST;
    }

}
