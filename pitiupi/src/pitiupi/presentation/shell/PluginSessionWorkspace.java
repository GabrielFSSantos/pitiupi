package pitiupi.presentation.shell;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Chrome compartilhado dos plugins de sessão: roteamento, faixa de abas e corpo.
 */
public final class PluginSessionWorkspace extends JPanel {

    private final PluginRoutingPanel routingPanel;
    private final SessionTabBar sessionTabBar = new SessionTabBar();
    private final JPanel bodyHost = new JPanel(new BorderLayout());

    public PluginSessionWorkspace(PluginRoutingPanel routingPanel, JComponent pluginToolbar) {
        this.routingPanel = routingPanel;
        setLayout(new BorderLayout(0, 0));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(routingPanel);
        if (pluginToolbar != null) {
            top.add(pluginToolbar);
        }
        add(top, BorderLayout.NORTH);

        JPanel sessionColumn = new JPanel(new BorderLayout(0, 0));
        sessionColumn.add(sessionTabBar.getPanel(), BorderLayout.NORTH);
        sessionColumn.add(new SessionBodyChrome(bodyHost), BorderLayout.CENTER);
        add(sessionColumn, BorderLayout.CENTER);
    }

    public PluginRoutingPanel getRoutingPanel() {
        return routingPanel;
    }

    public SessionTabBar getSessionTabBar() {
        return sessionTabBar;
    }

    public JPanel getBodyHost() {
        return bodyHost;
    }
}
