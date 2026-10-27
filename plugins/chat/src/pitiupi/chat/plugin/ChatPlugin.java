package pitiupi.chat.plugin;

import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.activity.PeerActivityInbox;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.application.presence.UserPeerView;
import pitiupi.chat.GUI.ChatPanel;
import pitiupi.chat.control.PrivateChatController;
import pitiupi.chat.control.PrivateChatHost;
import pitiupi.chat.net.ChatMessage;
import pitiupi.chat.net.LegacyMpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.presentation.shell.LocalUserIdentity;
import pitiupi.control.GuiManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.models.User;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.SwingUtilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.List;

public class ChatPlugin extends Plugin implements PrivateChatHost {

    private ChatPanel chatPanel;
    private PluginWorkspace workspace;
    private ConnectionManager connectionManager;
    private PrivateChatController privateChatController;

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
        this.connectionManager = connectionManager;
        this.workspace = workspace;
        this.privateChatController = new PrivateChatController(connectionManager, this);
        this.chatPanel = new ChatPanel(connectionManager);
        chatPanel.setPrivateChatController(privateChatController);
        privateChatController.setUiSink(chatPanel);
        bindShellWorkspaceContent(chatPanel);
        installPeerActions(workspace.peerActions());
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar) {
        registrar.register(PeerAction.of(
                "chat.focus",
                "Abrir sala geral",
                PeerActionTarget.ALL_PEERS,
                target -> {
                    chatPanel.focusGeneralRoom();
                    workspace.openOrFocusTab(getIdentifier(), getDisplayName());
                }
        ));
        registrar.register(PeerAction.of(
                "chat.peer",
                "Conversar com este peer",
                PeerActionTarget.SINGLE_PEER,
                this::openChatForPeer
        ));
        registrar.register(PeerAction.of(
                "chat.private.tcp",
                "Ativar TCP privado",
                PeerActionTarget.SINGLE_PEER,
                target -> {
                    workspace.openOrFocusTab(getIdentifier(), getDisplayName());
                    if (target instanceof PeerTarget.Single single) {
                        chatPanel.enablePrivateTcpForPeer(single.peer());
                    }
                }
        ));
    }

    private void openChatForPeer(PeerTarget target) {
        if (!(target instanceof PeerTarget.Single single)) {
            return;
        }
        chatPanel.focusPeer(single.peer());
        workspace.openOrFocusTab(getIdentifier(), getDisplayName());
    }

    private static User toUser(UserPeerView peer) {
        if (peer.isBroadcastTarget() || peer.isSelf()) {
            return null;
        }
        return peer.getInetAddress()
                .map(addr -> new User(peer.getDisplayName(), addr, System.currentTimeMillis(), peer.getInstanceId()))
                .orElse(null);
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(ChatMessage.class, LegacyMpChatMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof ChatMessage msg && chatPanel != null) {
            recordChatActivity(msg);
            SwingUtilities.invokeLater(() -> chatPanel.receiveChatMessage(msg));
        } else if (message instanceof LegacyMpChatMessage legacy && chatPanel != null) {
            SwingUtilities.invokeLater(() -> chatPanel.receiveLegacyMessage(
                    legacy.getTipoMensagem(),
                    legacy.getNome(),
                    legacy.getIp(),
                    legacy.getMessage()
            ));
        }
    }

    private void recordChatActivity(ChatMessage msg) {
        if (LocalUserIdentity.isLocalSender(connectionManager, msg.getName(), msg.getSenderIp())) {
            return;
        }
        PeerActivityInbox inbox = connectionManager.getPeerActivityInbox();
        String tag = msg.getTransportLabel();
        boolean multicast = tag != null && tag.toLowerCase().contains("multicast");
        if (multicast) {
            inbox.recordBroadcast(getIdentifier(), "chat.focus");
            return;
        }
        String instanceId = peerInstanceIdForActivity(msg.getSenderIp());
        if (instanceId != null) {
            inbox.record(instanceId, getIdentifier(), "chat.peer");
        }
    }

    private String peerInstanceIdForActivity(String ip) {
        if (ip == null || ip.isBlank() || connectionManager == null) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
                if (view.isSelf()) {
                    continue;
                }
                if (view.getInetAddress().isPresent() && view.getInetAddress().get().equals(address)) {
                    return view.getInstanceId();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public boolean receivePrivateConnection(TcpConnection connection) {
        SwingUtilities.invokeLater(() -> privateChatController.connectionReceived(connection));
        return true;
    }

    @Override
    public boolean receiveBytes(TcpConnection connection, byte[] bytes) {
        LegacyMpChatMessage legacy = parseLegacy(bytes);
        if (legacy == null) {
            return true;
        }
        privateChatController.receiveMessage(connection, legacy);
        String instanceId = peerInstanceIdForActivity(connection.getAddress().getHostAddress());
        if (instanceId != null) {
            connectionManager.getPeerActivityInbox().record(instanceId, getIdentifier(), "chat.private.tcp");
        }
        return true;
    }

    @Override
    public boolean onSocketClosed(TcpConnection connection) {
        privateChatController.closeChat(connection);
        return true;
    }

    @Override
    public void openConnection(String ip) {
        try {
            connectionManager.connectPluginTcp(
                    InetAddress.getByName(ip),
                    getIdentifier(),
                    getIdentifier()
            );
        } catch (Exception e) {
            System.out.println("Error opening connection: " + e.getMessage());
        }
    }

    @Override
    public String getPluginIdentifier() {
        return getIdentifier();
    }

    private LegacyMpChatMessage parseLegacy(byte[] bytes) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            if (ois.readObject() instanceof LegacyMpChatMessage msg) {
                return msg;
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    @Override
    public String getName() {
        return "Chat";
    }

    @Override
    public String getDisplayName() {
        return "Conversa na rede";
    }

    @Override
    public String getAuthor() {
        return "Flávio Schiavoni";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.Chat.Example";
    }
}
