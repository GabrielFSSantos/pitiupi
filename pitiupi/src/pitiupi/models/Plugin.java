/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package pitiupi.models;

import pitiupi.api.plugin.InviteDetails;
import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.JComponent;
import java.util.List;

/**
 * Interface base para plugins da aplicação.
 *
 * <p>Cada plugin deve implementar esta interface para ser carregado
 * pela aplicação e participar do sistema de comunicação por mensagens.</p>
 *
 * @author flavio
 */
public abstract class Plugin {

    private JComponent shellWorkspaceContent;

    public abstract String getName();

    /**
     * Nome amigável na UI (galeria, abas). Padrão: {@link #getName()}.
     */
    public String getDisplayName() {
        return getName();
    }

    public abstract String getAuthor();
    public abstract String getVersion();
    public abstract String getIdentifier();

    /**
     * Registra o painel principal da aba central (shell). Não abre a aba — use
     * {@link pitiupi.api.plugin.PluginWorkspace#openOrFocusTab} na galeria ou peer actions.
     */
    protected final void bindShellWorkspaceContent(JComponent content) {
        this.shellWorkspaceContent = content;
    }

    public final JComponent getShellWorkspaceContent() {
        return shellWorkspaceContent;
    }

    /**
     * Inicializa o plugin na aba central (shell) e registra peer actions.
     */
    public abstract void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace);

    public void installPeerActions(PeerActionsRegistrar registrar) {
    }

    public void onInviteAccepted(InviteDetails invite) {
    }

    /**
     * Aba central do plugin fechada na shell (× na aba superior). Use para parar timers, captura, etc.
     */
    public void onShellTabClosed() {
    }

    /**
     * Retorna uma mensagem utilizada pelo plugin.
     */
    public abstract List<Class<? extends Message>> getMessages();

    public abstract void receiveMessage(Message message);

    public boolean receiveAllTypes() {return false;}
    public boolean receivePrivateConnection(TcpConnection connection) {
        return false;
    }
    public boolean receiveBytes(TcpConnection connection, byte[] bytes) {
        return false;
    }
    public boolean onSocketClosed(TcpConnection connection) {
        return false;
    }
}
