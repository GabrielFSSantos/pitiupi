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
 * @author tony
 * 
 */
public class MenuBar extends JMenuBar {

    private javax.swing.JMenuItem setupMenu;
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
        this.add(helpMenu);
    }



    private void exitMenuActionPerformed(java.awt.event.ActionEvent evt) {
        mainWindow.exit();
    }

    
   private void setupMenuActionPerformed(java.awt.event.ActionEvent evt) {
        Login login = new Login();
        login.setVisible(true);

        mainWindow.setUserName(login.getName());
        mainWindow.setPort(login.getPort());

        login.dispose();
    }
    
    public void addPlugin(JMenu menu){
     this.pluginMenu.add(menu);
    }
}
