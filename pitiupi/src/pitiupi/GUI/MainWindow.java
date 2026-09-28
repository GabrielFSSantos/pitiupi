/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.GUI;

import pitiupi.control.ConnectionManager;
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

    private final MenuBar menuBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel status;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTabbedPane tabbedPane;

    private OnlineUsersPanel onlineUsersPanel;

    private String userName;

    private ConnectionManager connectionManager;
    private PluginManager pluginManager;

    /**
     * Cria a janela principal da aplicação.
     *
     * @param userName nome utilizado para identificação na rede.
     * @param port porta utilizada na comunicação multicast.
     */
    public MainWindow(String userName, int port) {
        this.userName = userName;

        pluginManager = new PluginManager(this);
        loadPlugins();

        connectionManager = new ConnectionManager(this, pluginManager, port);

        menuBar = new MenuBar(this);

        initComponents();
    }

    private void initComponents() {
        this.setJMenuBar(menuBar);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Share My Sheet");

        jScrollPane1 = new javax.swing.JScrollPane();
        tabbedPane = new javax.swing.JTabbedPane();
        statusPanel = new javax.swing.JPanel();
        status = new javax.swing.JLabel();

        jScrollPane1.setViewportView(tabbedPane);
        statusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        updateStatus();
        statusPanel.add(status);

        this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        this.getContentPane().add(statusPanel, java.awt.BorderLayout.PAGE_END);

        this.onlineUsersPanel = new OnlineUsersPanel();
        this.connectionManager.addPeerListener(onlineUsersPanel);
        this.getContentPane().add(this.onlineUsersPanel, java.awt.BorderLayout.LINE_END);

        this.pack();

        this.setSize(new java.awt.Dimension(1024, 768));
    }

    public void exit() {
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    public String getUserName(){
        return this.userName;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void loadPlugins() {
        pluginManager.loadPlugins();
    }
    
    public void addMenu(JMenu menu) {
        this.menuBar.addPlugin(menu);
    }

    public void setUserName(String userName) {
        this.userName = userName;
        updateStatus();
    }

    private void updateStatus() {
        this.status.setText("Connected on port " + connectionManager + " name " + this.userName);
    }

}
