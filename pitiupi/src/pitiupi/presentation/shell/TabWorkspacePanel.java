package pitiupi.presentation.shell;

import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abas centrais: Plugins (fixa), dinâmicas por plugin, Configurações (fixa).
 * Faixa de abas fina + corpo único abaixo (mesmo padrão dos plugins de sessão).
 */
public final class TabWorkspacePanel {

    public static final String TAB_PLUGINS = "Plugins";
    public static final String TAB_SETTINGS = "Configurações";

    private final StripOnlyTabbedPane strip = new StripOnlyTabbedPane();
    private final List<JComponent> tabContents = new ArrayList<>();
    private final JPanel contentHost = new JPanel(new BorderLayout());
    private final JPanel root = new JPanel(new BorderLayout(0, 0));
    private final Map<String, Integer> pluginTabIndex = new HashMap<>();
    private Consumer<String> onActivePluginChanged = id -> {
    };
    private Runnable onSettingsTabSelected = () -> {
    };
    private Consumer<String> onPluginTabClosed = id -> {
    };

    public TabWorkspacePanel(JComponent pluginsTab, JComponent settingsTab) {
        tabContents.add(pluginsTab);
        tabContents.add(settingsTab);
        strip.addTab(TAB_PLUGINS, StripOnlyTabbedPane.newHiddenPlaceholder());
        strip.addTab(TAB_SETTINGS, StripOnlyTabbedPane.newHiddenPlaceholder());
        strip.addChangeListener(e -> {
            showContentAt(strip.getSelectedIndex());
            fireActivePlugin();
            if (strip.getSelectedIndex() == strip.getTabCount() - 1) {
                onSettingsTabSelected.run();
            }
        });

        root.add(strip, BorderLayout.NORTH);
        root.add(new SessionBodyChrome(contentHost), BorderLayout.CENTER);
        showContentAt(0);
    }

    public JPanel getRootPanel() {
        return root;
    }

    /** Expõe a faixa de abas para testes. */
    public JTabbedPane getTabbedPane() {
        return strip;
    }

    public void setOnActivePluginChanged(Consumer<String> listener) {
        this.onActivePluginChanged = listener != null ? listener : id -> {
        };
    }

    public void setOnSettingsTabSelected(Runnable onSettingsTabSelected) {
        this.onSettingsTabSelected = onSettingsTabSelected != null ? onSettingsTabSelected : () -> {
        };
    }

    public void setOnPluginTabClosed(Consumer<String> listener) {
        this.onPluginTabClosed = listener != null ? listener : id -> {
        };
    }

    public void openOrFocusTab(String pluginId, String title, JComponent content) {
        Integer existing = pluginTabIndex.get(pluginId);
        if (existing != null && existing < strip.getTabCount()) {
            if (content != null) {
                tabContents.set(existing, content);
                content.putClientProperty("pitiupi.pluginId", pluginId);
                if (strip.getSelectedIndex() == existing) {
                    showContentAt(existing);
                }
            }
            updateTabHeader(existing, title, pluginId);
            strip.setSelectedIndex(existing);
            return;
        }
        JComponent panel = content != null ? content : placeholderPanel(title);
        panel.putClientProperty("pitiupi.pluginId", pluginId);
        int insertAt = Math.max(1, strip.getTabCount() - 1);
        strip.insertTab(" ", null, StripOnlyTabbedPane.newHiddenPlaceholder(), title, insertAt);
        tabContents.add(insertAt, panel);
        setClosableTabHeader(insertAt, title, pluginId);
        rebuildPluginIndex();
        strip.setSelectedIndex(pluginTabIndex.getOrDefault(pluginId, insertAt));
    }

    public void focusPluginsTab() {
        strip.setSelectedIndex(0);
    }

    boolean isPluginTabOpen(String pluginId) {
        return pluginTabIndex.containsKey(pluginId);
    }

    int getTabCount() {
        return strip.getTabCount();
    }

    private void showContentAt(int index) {
        contentHost.removeAll();
        if (index >= 0 && index < tabContents.size()) {
            contentHost.add(tabContents.get(index), BorderLayout.CENTER);
        }
        contentHost.revalidate();
        contentHost.repaint();
    }

    private void setClosableTabHeader(int tabIndex, String title, String pluginId) {
        JPanel header = new JPanel(new BorderLayout(2, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 0, 2));
        JLabel label = new JLabel(title);
        header.add(label, BorderLayout.CENTER);
        header.add(ShellUi.roundTabCloseButton(() -> closePluginTab(pluginId)), BorderLayout.EAST);
        strip.setTabComponentAt(tabIndex, header);
    }

    private void updateTabHeader(int tabIndex, String title, String pluginId) {
        strip.setToolTipTextAt(tabIndex, title);
        if (strip.getTabComponentAt(tabIndex) instanceof JPanel header) {
            for (java.awt.Component c : header.getComponents()) {
                if (c instanceof JLabel) {
                    ((JLabel) c).setText(title);
                }
            }
        } else {
            setClosableTabHeader(tabIndex, title, pluginId);
        }
    }

    void closePluginTab(String pluginId) {
        Integer index = pluginTabIndex.get(pluginId);
        if (index == null || index <= 0 || index >= strip.getTabCount() - 1) {
            return;
        }
        onPluginTabClosed.accept(pluginId);
        strip.removeTabAt(index);
        tabContents.remove(index.intValue());
        pluginTabIndex.remove(pluginId);
        rebuildPluginIndex();
        if (strip.getSelectedIndex() < 0 || strip.getSelectedIndex() >= strip.getTabCount()) {
            strip.setSelectedIndex(0);
        } else {
            showContentAt(strip.getSelectedIndex());
        }
        fireActivePlugin();
    }

    private void rebuildPluginIndex() {
        pluginTabIndex.clear();
        for (int i = 1; i < strip.getTabCount() - 1; i++) {
            if (i >= tabContents.size()) {
                break;
            }
            JComponent content = tabContents.get(i);
            Object id = content.getClientProperty("pitiupi.pluginId");
            if (id instanceof String s) {
                pluginTabIndex.put(s, i);
                if (strip.getTabComponentAt(i) == null) {
                    String tip = strip.getToolTipTextAt(i);
                    setClosableTabHeader(i, tip != null ? tip : s, s);
                }
            }
        }
    }

    private void fireActivePlugin() {
        int idx = strip.getSelectedIndex();
        if (idx <= 0 || idx >= strip.getTabCount() - 1 || idx >= tabContents.size()) {
            onActivePluginChanged.accept(null);
            return;
        }
        Object id = tabContents.get(idx).getClientProperty("pitiupi.pluginId");
        onActivePluginChanged.accept(id instanceof String s ? s : null);
    }

    private static JPanel placeholderPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        JLabel label = new JLabel(
                "<html><center>Plugin <b>" + escapeHtml(title) + "</b><br/>"
                        + "Este plugin não expôs um painel para a aba central.</center></html>",
                SwingConstants.CENTER
        );
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;");
    }
}
