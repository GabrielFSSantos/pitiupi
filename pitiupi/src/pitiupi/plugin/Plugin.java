/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package pitiupi.plugin;

import javax.swing.JMenu;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;

/**
 * Interface base para plugins da aplicação.
 *
 * <p>Cada plugin deve implementar esta interface para ser carregado
 * pela aplicação e participar do sistema de comunicação por mensagens.</p>
 *
 * @author flavio
 */
public interface Plugin {

    public String getName();
    public String getAuthor();
    public String getVersion();

    /**
     * Inicializa o plugin.
     *
     * <p>Este método é chamado pela aplicação após o carregamento do plugin.</p>
     *
     * @param window janela principal da aplicação.
     */
    public void createPlugin(MainWindow window);

    /**
     * Retorna uma mensagem utilizada pelo plugin.
     *
     * @return mensagem criada pelo plugin.
     */
    public Message getMessage();

    /**
     * Recebe uma mensagem enviada pela aplicação.
     *
     * <p>A mensagem é recebida em formato serializado. O plugin deve verificar
     * se a mensagem pertence ao seu tipo e realizar o processamento necessário.</p>
     *
     * @param message mensagem serializada recebida.
     */
    public void receiveMessage(byte[] message);
}