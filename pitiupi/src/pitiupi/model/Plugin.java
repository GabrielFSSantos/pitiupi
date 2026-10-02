/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package pitiupi.model;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.model.Messages.Message;
import pitiupi.net.tcp.TcpConnection;

/**
 * Interface base para plugins da aplicação.
 *
 * <p>Cada plugin deve implementar esta interface para ser carregado
 * pela aplicação e participar do sistema de comunicação por mensagens.</p>
 *
 * @author flavio
 */
public abstract class Plugin {

    public abstract String getName();
    public abstract String getAuthor();
    public abstract String getVersion();
    public abstract String getIdentifier();

    /**
     * Inicializa o plugin.
     *
     * <p>Este método é chamado pela aplicação após o carregamento do plugin.</p>
     *
     * @param connectionManager interface com o sistema de conexões da aplicação.
     * @param guiManager interface com a janela principal da aplicação
     */
    public abstract void createPlugin(ConnectionManager connectionManager, GuiManager guiManager);

    /**
     * Retorna uma mensagem utilizada pelo plugin.
     *
     * @return mensagem criada pelo plugin.
     */
    public abstract Message getMessage();

    /**
     * Recebe uma mensagem enviada pela aplicação.
     *
     * <p>A mensagem é recebida em formato serializado. O plugin deve verificar
     * se a mensagem pertence ao seu tipo e realizar o processamento necessário.</p>
     *
     * @param message mensagem serializada recebida.
     */
    public abstract void receiveMessage(byte[] message);

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