/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.GUI;

import pitiupi.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import pitiupi.control.PluginLoader;
import pitiupi.plugin.Plugin;
import pitiupi.control.HeartbeatManager;

/**
 *
 * @author flavio
 * @author tony
 */
public class MainWindow extends javax.swing.JFrame {

    private final MenuBar menuBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel status;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTabbedPane tabbedPane;

    private HeartbeatManager heartbeatManager;
    private OnlineUsersPanel onlineUsersPanel;


    private Socket socket;

    private final String userName;
    private final int port;

    private PluginLoader pluginManager;
    private final List<Plugin> plugins;

    
    public MainWindow(String userName, int port) {
        this.userName = userName;
        this.port = port;
        this.menuBar = new MenuBar(this);
        this.plugins = new ArrayList<>();
        this.initComponents();
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
        status.setText("Connected on port " + this.port + " name " + this.userName);
        statusPanel.add(status);

        this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        this.getContentPane().add(statusPanel, java.awt.BorderLayout.PAGE_END);

        this.pack();

        this.socket = new Socket(this);
        this.socket.start();

        this.heartbeatManager = new HeartbeatManager(this);
        this.onlineUsersPanel = new OnlineUsersPanel();
        this.heartbeatManager.addPeerListener(this.onlineUsersPanel);
        this.getContentPane().add(this.onlineUsersPanel, java.awt.BorderLayout.LINE_END);
        this.heartbeatManager.start();

        this.setSize(new java.awt.Dimension(1024, 768));
        this.pluginManager = new PluginLoader(this);
        this.pluginManager.loadPlugins();
    }


    public void exit() {
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }

    public String getUserName(){
        return this.userName;
    }
    
    public void addMenu(JMenu menu) {
        this.menuBar.addPlugin(menu);
    }

    public int getPort() {
        return this.port;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }
}
