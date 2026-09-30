/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.GUI;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.control.PluginManager;

import javax.swing.JMenu;

import pitiupi.control.HeartbeatManager;

/**
 * Janela principal da aplicação.
 *
 * <p>Responsável por inicializar a interface gráfica e coordenar os
 * principais componentes da aplicação, incluindo comunicação de rede,
 * gerenciamento de heartbeat e carregamento de plugins.</p>
 *
 * <p>A instância desta classe é compartilhada com os plugins para permitir
 * integração com a aplicação principal.</p>
 *
 * @author flavio
 * @author tony
 * @author Gustavo
 */
public class MainWindow extends javax.swing.JFrame {

    private MenuBar menuBar;
    private javax.swing.JLabel status;

    private final ConnectionManager connectionManager;
    private final PluginManager pluginManager;
    private final GuiManager guiManager;


    /**
     * Cria a janela principal da aplicação.
     *
     * @param userName nome utilizado para identificação na rede.
     * @param port porta utilizada na comunicação multicast.
     */
    public MainWindow(String userName, int port) {
        initMenu();

        pluginManager = new PluginManager(this);

        connectionManager = new ConnectionManager(this, userName, pluginManager, port);
        guiManager = new GuiManager(this);

        initUserPanel();
        initStatusPanel();

        loadPlugins();

        this.pack();

        this.setSize(new java.awt.Dimension(1024, 768));
    }

    private void initMenu() {
        menuBar = new MenuBar(this);

        this.setJMenuBar(menuBar);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Share My Sheet");

        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();

        jScrollPane1.setViewportView(tabbedPane);

        this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }

    private void initUserPanel() {
        OnlineUsersPanel onlineUsersPanel = new OnlineUsersPanel();
        this.connectionManager.addPeerListener(onlineUsersPanel);
        this.getContentPane().add(onlineUsersPanel, java.awt.BorderLayout.LINE_END);
    }

    private void initStatusPanel() {
        javax.swing.JPanel statusPanel = new javax.swing.JPanel();
        status = new javax.swing.JLabel();

        statusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        updateStatus();
        statusPanel.add(status);

        this.getContentPane().add(statusPanel, java.awt.BorderLayout.PAGE_END);
    }

    public void exit() {
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void loadPlugins() {
        pluginManager.loadPlugins(connectionManager, guiManager);
    }

    public void addMenu(JMenu menu) {
        this.menuBar.addPlugin(menu);
    }
    public void setUserName(String userName) {
        connectionManager.setUserName(userName);
        updateStatus();
    }

    private void updateStatus() {
        this.status.setText("Connected on port " + connectionManager.getUserPort() + " name " + connectionManager.getUserName());
    }

}
