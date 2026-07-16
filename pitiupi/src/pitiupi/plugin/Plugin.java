/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package pitiupi.plugin;

import javax.swing.JMenu;
import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;

/**
 *
 * @author flavio
 */
public interface Plugin {

    public String getName();
    public String getAuthor();
    public String getVersion();
    public void createPlugin(MainWindow window);
    public Message getMessage();
    public void receiveMessage(byte[] message);
}