/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.GUI;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 *
 * @author flavio
 */
public class MenuBar extends JMenuBar {

    private javax.swing.JMenuItem setupMenu;
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu pluginMenu;
    private javax.swing.JMenuItem exitMenu;
    private final MainWindow mainWindow;

    public MenuBar(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.initComponents();
    }

    private void initComponents() {
        fileMenu = new javax.swing.JMenu();
        exitMenu = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        pluginMenu = new javax.swing.JMenu();
        setupMenu = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();

        fileMenu.setText("File");
        exitMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitMenu.setText("Exit");
        exitMenu.addActionListener(this::exitMenuActionPerformed);
        fileMenu.add(exitMenu);
        this.add(fileMenu);

        pluginMenu.setText("Plugin");
        this.add(pluginMenu);

        helpMenu.setText("Help");

        setupMenu.setText("Preferences");
        setupMenu.addActionListener(this::setupMenuActionPerformed);
        helpMenu.add(setupMenu);

        aboutMenu.setText("About");
        aboutMenu.addActionListener(this::aboutMenuActionPerformed);
        helpMenu.add(aboutMenu);

        this.add(helpMenu);
    }



    private void exitMenuActionPerformed(java.awt.event.ActionEvent evt) {
        mainWindow.exit();
    }

    private void setupMenuActionPerformed(java.awt.event.ActionEvent evt) {
        Login login = new Login();
        login.setVisible(true);
        login.dispose();
    }

    private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {
        About about = new About();
        about.setVisible(true);
    }
    
    public void addPlugin(JMenu menu){
     this.pluginMenu.add(menu);
    }
}
