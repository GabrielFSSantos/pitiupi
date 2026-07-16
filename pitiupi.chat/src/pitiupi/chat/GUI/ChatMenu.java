/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.chat.GUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;

/**
 *
 * @author flavio
 */
public class ChatMenu extends JMenu {

    private final ChatWindow chatWindow;
    private javax.swing.JMenuItem menuItem;

    public ChatMenu(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
        this.setText("Chat");
        menuItem = new javax.swing.JMenuItem();
        menuItem.setText("Show Chat Window");
        menuItem.addActionListener(this::chatMenuActionPerformed);
        this.add(menuItem);
    }

    private void chatMenuActionPerformed(ActionEvent ae) {
        this.chatWindow.setVisible(true);
    }
}
