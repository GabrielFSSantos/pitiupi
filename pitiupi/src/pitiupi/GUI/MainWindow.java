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

    private HeartbeatManager heartbeatManager;
    private OnlineUsersPanel onlineUsersPanel;


    private Socket socket;

    private final String userName;
    private final int port;

    private PluginLoader pluginManager;
    private final List<Plugin> plugins;

    /**
     * Cria a janela principal da aplicação.
     *
     * @param userName nome utilizado para identificação na rede.
     * @param port porta utilizada na comunicação multicast.
     */
    public MainWindow(String userName, int port) {
        this.userName = userName;
        this.port = port;
        this.menuBar = new MenuBar(this);
        this.plugins = new ArrayList<>();
        this.initComponents();
    }

    private void initComponents() {
        initGUI();
        initSocket();
        initHeartbeat();
        initPlugins();
        this.setSize(new java.awt.Dimension(1024, 768));
    }

    private void initGUI() {
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
    }

    private void initSocket() {
        this.socket = new Socket(this);
        this.socket.start();
    }

    private void initHeartbeat() {
        this.heartbeatManager = new HeartbeatManager(this);
        this.onlineUsersPanel = new OnlineUsersPanel();
        this.heartbeatManager.addPeerListener(this.onlineUsersPanel);
        this.getContentPane().add(this.onlineUsersPanel, java.awt.BorderLayout.LINE_END);
        this.heartbeatManager.start();
    }

    private void initPlugins() {
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
