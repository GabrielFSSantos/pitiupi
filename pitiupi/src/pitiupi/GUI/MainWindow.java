/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.GUI;

import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.control.plugins.PluginManager;
import pitiupi.presentation.shell.NetworkUsersPanel;
import pitiupi.presentation.shell.ShellHost;
import pitiupi.presentation.shell.ShellMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @deprecated Substituído por {@link pitiupi.presentation.shell.MainShellFrame}.
 */
@Deprecated
public class MainWindow extends JFrame implements ShellHost {

    private JLabel status;
    private final NetworkUsersPanel networkUsersPanel;

    private final ConnectionManager connectionManager;
    private final PluginManager pluginManager;
    private final GuiManager guiManager;

    public MainWindow(String userName, int port) {
        pluginManager = new PluginManager();
        connectionManager = new ConnectionManager(userName, pluginManager, port);

        ShellMenuBar menuBar = new ShellMenuBar();
        setJMenuBar(menuBar);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Pitiupi");

        networkUsersPanel = new NetworkUsersPanel(connectionManager);
        getContentPane().add(new JScrollPane(new JTabbedPane()), BorderLayout.CENTER);
        getContentPane().add(networkUsersPanel, BorderLayout.LINE_END);

        initStatusPanel();

        guiManager = new GuiManager(this, menuBar, networkUsersPanel);

        loadPlugins();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        pack();
        setSize(new Dimension(1024, 768));
    }

    private void initStatusPanel() {
        JPanel statusPanel = new JPanel();
        status = new JLabel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        updateStatus();
        statusPanel.add(status);
        getContentPane().add(statusPanel, BorderLayout.PAGE_END);
    }

    @Override
    public void exit() {
        connectionManager.shutdownGracefully();
        setVisible(false);
        dispose();
        System.exit(0);
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void loadPlugins() {
        pluginManager.loadPlugins(connectionManager, guiManager);
    }

    @Override
    public void setUserName(String userName) {
        connectionManager.setUserName(userName);
        updateStatus();
    }

    @Override
    public void setStatusMessage(String message) {
        status.setText(message);
    }

    private void updateStatus() {
        status.setText("Connected on port " + connectionManager.getPort() + " name " + connectionManager.getUserName());
    }
}
