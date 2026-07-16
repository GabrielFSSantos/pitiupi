/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.screenCast.GUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;

/**
 *
 * @author flavio
 */
public class ScreenCastMenu extends JMenu {

    private final ScreenCastWindow screenCastWindow;
    private final javax.swing.JMenuItem menuItem;

    public ScreenCastMenu(ScreenCastWindow screenCastWindow) {
        this.screenCastWindow = screenCastWindow;
        this.setText("Screen Cast");
        menuItem = new javax.swing.JMenuItem();
        menuItem.setText("ScreenCast Window");
        menuItem.addActionListener(this::chatMenuActionPerformed);
        this.add(menuItem);
    }

    private void chatMenuActionPerformed(ActionEvent ae) {
        this.screenCastWindow.setVisible(true);
    }
}
