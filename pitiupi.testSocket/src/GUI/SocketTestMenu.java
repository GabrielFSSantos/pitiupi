package GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SocketTestMenu extends JMenu {
    private final SocketTestWindow socketTestWindow;

    public SocketTestMenu(SocketTestWindow socketTestWindow) {
        this.socketTestWindow = socketTestWindow;
        this.setText("MP-Chat");
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText("Show Chat Window");
        menuItem.addActionListener(this::showTestWindow);
        this.add(menuItem);
    }

    private void showTestWindow(ActionEvent ae) {
        this.socketTestWindow.setVisible(true);
    }
}
