package pitiupi.presentation.shell;

import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

/**
 * Faixa de abas de sessão com botão à direita para nova conversa por IP.
 */
public final class SessionTabBar {

    public static final String ADD_PEER_BUTTON_LABEL = "+ Nova";
    public static final String ADD_PEER_BUTTON_TOOLTIP = "Nova conversa… — informar o IP do peer manualmente";

    private static final int ADD_BUTTON_MIN_WIDTH = 100;

    private final ClosableTabbedPane tabs = new ClosableTabbedPane();
    private final JButton addPeerButton = new JButton(ADD_PEER_BUTTON_LABEL);
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JPanel stripRow = new JPanel(new GridBagLayout());

    public SessionTabBar() {
        addPeerButton.setToolTipText(ADD_PEER_BUTTON_TOOLTIP);
        GridBagConstraints tabsGbc = new GridBagConstraints();
        tabsGbc.gridx = 0;
        tabsGbc.gridy = 0;
        tabsGbc.weightx = 1.0;
        tabsGbc.fill = GridBagConstraints.HORIZONTAL;
        tabsGbc.anchor = GridBagConstraints.WEST;
        stripRow.add(tabs.getTabbedPane(), tabsGbc);

        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 1;
        btnGbc.gridy = 0;
        btnGbc.weightx = 0;
        btnGbc.fill = GridBagConstraints.NONE;
        btnGbc.anchor = GridBagConstraints.EAST;
        stripRow.add(addPeerButton, btnGbc);

        GridBagConstraints rowGbc = new GridBagConstraints();
        rowGbc.gridx = 0;
        rowGbc.gridy = 0;
        rowGbc.weightx = 1.0;
        rowGbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(stripRow, rowGbc);
        alignAddButtonToStrip();
    }

    private void alignAddButtonToStrip() {
        int h = Math.max(26, tabs.stripHeight());
        int w = Math.max(ADD_BUTTON_MIN_WIDTH, addPeerButton.getPreferredSize().width);
        addPeerButton.setPreferredSize(new Dimension(w, h));
        addPeerButton.setMinimumSize(new Dimension(ADD_BUTTON_MIN_WIDTH, h));
        addPeerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        ShellUi.alignButtonToTabStrip(addPeerButton);
        stripRow.setPreferredSize(new Dimension(stripRow.getPreferredSize().width, h));
    }

    public JPanel getPanel() {
        alignAddButtonToStrip();
        return panel;
    }

    public JButton getAddPeerButton() {
        return addPeerButton;
    }

    public void setOnAddPeerRequested(Runnable action) {
        addPeerButton.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnTabSelected(Consumer<String> onTabSelected) {
        tabs.setOnTabSelected(onTabSelected);
    }

    public void setOnTabClosed(Consumer<String> onTabClosed) {
        tabs.setOnTabClosed(onTabClosed);
    }

    public void setCloseFallbackKey(String key) {
        tabs.setCloseFallbackKey(key);
    }

    public void addOrSelectTab(String key, String title, JComponent content, boolean closable) {
        tabs.addOrSelectTab(key, title, content, closable);
        alignAddButtonToStrip();
    }

    public void addOrSelectTab(String key, String title, String tooltip, JComponent content, boolean closable) {
        tabs.addOrSelectTab(key, title, tooltip, content, closable);
        alignAddButtonToStrip();
    }

    public void addTabIfAbsent(String key, String title, String tooltip, JComponent content, boolean closable) {
        tabs.addTabIfAbsent(key, title, tooltip, content, closable);
        alignAddButtonToStrip();
    }

    public void selectTab(String key) {
        tabs.selectTab(key);
    }

    public void selectTabSilently(String key) {
        tabs.selectTabSilently(key);
    }

    public void closeTab(String key) {
        tabs.closeTab(key);
        alignAddButtonToStrip();
    }

    public boolean hasTab(String key) {
        return tabs.hasTab(key);
    }

    public int getTabCount() {
        return tabs.getTabCount();
    }

    public void setTabTitle(String key, String title, String tooltip) {
        tabs.setTabTitle(key, title, tooltip);
    }

    public String selectedKey() {
        return tabs.selectedKey();
    }
}
