package pitiupi.presentation.shell;

import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abas com botão fechar circular (estilo shell). Conteúdo real fica fora (strip-only).
 */
public final class ClosableTabbedPane {

    private final StripOnlyTabbedPane tabbedPane = new StripOnlyTabbedPane();
    private final List<String> orderedKeys = new ArrayList<>();
    private final Map<String, Integer> keyToIndex = new HashMap<>();
    private Consumer<String> onTabSelected = key -> {
    };
    private Consumer<String> onTabClosed = key -> {
    };
    private boolean suppressSelectionEvents;
    private String closeFallbackKey = PluginSessionKeys.ROOM;

    public ClosableTabbedPane() {
        tabbedPane.addChangeListener(e -> {
            if (suppressSelectionEvents) {
                return;
            }
            onTabSelected.accept(selectedKey());
        });
    }

    public void setCloseFallbackKey(String closeFallbackKey) {
        this.closeFallbackKey = closeFallbackKey;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public int getTabCount() {
        reconcileKeyListWithTabPane();
        return tabbedPane.getTabCount();
    }

    public void setOnTabSelected(Consumer<String> onTabSelected) {
        this.onTabSelected = onTabSelected != null ? onTabSelected : key -> {
        };
    }

    public void setOnTabClosed(Consumer<String> onTabClosed) {
        this.onTabClosed = onTabClosed != null ? onTabClosed : key -> {
        };
    }

    public String selectedKey() {
        reconcileKeyListWithTabPane();
        int idx = tabbedPane.getSelectedIndex();
        if (idx < 0 || idx >= orderedKeys.size()) {
            return null;
        }
        return orderedKeys.get(idx);
    }

    public void selectTab(String key) {
        selectTabInternal(key, false);
    }

    public void selectTabSilently(String key) {
        selectTabInternal(key, true);
    }

    private void selectTabInternal(String key, boolean silent) {
        reconcileKeyListWithTabPane();
        int index = indexOfKey(key);
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        boolean prev = suppressSelectionEvents;
        suppressSelectionEvents = silent;
        try {
            tabbedPane.setSelectedIndex(index);
        } finally {
            suppressSelectionEvents = prev;
        }
    }

    public void addOrSelectTab(String key, String title, JComponent content, boolean closable) {
        addOrSelectTab(key, title, title, content, closable);
    }

    public void addOrSelectTab(String key, String title, String tooltip, JComponent content, boolean closable) {
        reconcileKeyListWithTabPane();
        dedupeKey(key);
        int index = indexOfKey(key);
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            setTabHeader(index, title, tooltip, closable, key);
            tabbedPane.setSelectedIndex(index);
            reindex();
            return;
        }
        tabbedPane.addTab(title, null);
        int newIndex = tabbedPane.getTabCount() - 1;
        orderedKeys.add(key);
        setTabHeader(newIndex, title, tooltip, closable, key);
        reindex();
        tabbedPane.setSelectedIndex(newIndex);
    }

    public void addTabIfAbsent(String key, String title, JComponent content, boolean closable) {
        addTabIfAbsent(key, title, title, content, closable);
    }

    /**
     * Cria a aba se não existir, sem mudar a aba selecionada.
     */
    public void addTabIfAbsent(String key, String title, String tooltip, JComponent content, boolean closable) {
        reconcileKeyListWithTabPane();
        dedupeKey(key);
        int index = indexOfKey(key);
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            setTabHeader(index, title, tooltip, closable, key);
            reindex();
            return;
        }
        int selectedBefore = tabbedPane.getSelectedIndex();
        tabbedPane.addTab(title, null);
        int newIndex = tabbedPane.getTabCount() - 1;
        orderedKeys.add(key);
        setTabHeader(newIndex, title, tooltip, closable, key);
        reindex();
        if (selectedBefore >= 0 && selectedBefore < tabbedPane.getTabCount()) {
            boolean prev = suppressSelectionEvents;
            suppressSelectionEvents = true;
            try {
                tabbedPane.setSelectedIndex(selectedBefore);
            } finally {
                suppressSelectionEvents = prev;
            }
        }
    }

    public void closeTab(String key) {
        closeTab(key, closeFallbackKey);
    }

    public void closeTab(String key, String fallbackKey) {
        reconcileKeyListWithTabPane();
        int index = indexOfKey(key);
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.removeTabAt(index);
        if (index < orderedKeys.size()) {
            orderedKeys.remove(index);
        }
        keyToIndex.remove(key);
        reindex();
        onTabClosed.accept(key);
        String next = null;
        if (fallbackKey != null && !fallbackKey.isBlank() && hasTab(fallbackKey)) {
            next = fallbackKey;
        } else if (closeFallbackKey != null && !closeFallbackKey.isBlank() && hasTab(closeFallbackKey)) {
            next = closeFallbackKey;
        } else {
            next = firstTabKey();
        }
        if (next != null) {
            selectTab(next);
        } else if (!suppressSelectionEvents) {
            onTabSelected.accept(null);
        }
    }

    public boolean hasTab(String key) {
        reconcileKeyListWithTabPane();
        int index = indexOfKey(key);
        return index >= 0 && index < tabbedPane.getTabCount();
    }

    public void setTabTitle(String key, String title) {
        setTabTitle(key, title, title);
    }

    public void setTabTitle(String key, String title, String tooltip) {
        reconcileKeyListWithTabPane();
        int index = indexOfKey(key);
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        Component tabComponent = tabbedPane.getTabComponentAt(index);
        if (tabComponent instanceof JPanel header) {
            for (Component child : header.getComponents()) {
                if (child instanceof JLabel label) {
                    label.setText(title);
                    break;
                }
            }
        }
        String tip = tooltip != null && !tooltip.isBlank() ? tooltip : title;
        tabbedPane.setToolTipTextAt(index, tip);
    }

    int stripHeight() {
        return tabbedPane.stripHeight();
    }

    private String firstTabKey() {
        if (orderedKeys.isEmpty()) {
            return null;
        }
        return orderedKeys.get(0);
    }

    private int indexOfKey(String key) {
        Integer mapped = keyToIndex.get(key);
        if (mapped != null && mapped < orderedKeys.size() && key.equals(orderedKeys.get(mapped))) {
            return mapped;
        }
        for (int i = 0; i < orderedKeys.size(); i++) {
            if (key.equals(orderedKeys.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void reconcileKeyListWithTabPane() {
        while (orderedKeys.size() > tabbedPane.getTabCount()) {
            orderedKeys.remove(orderedKeys.size() - 1);
        }
        reindex();
    }

    private void dedupeKey(String key) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < orderedKeys.size(); i++) {
            if (key.equals(orderedKeys.get(i))) {
                indices.add(i);
            }
        }
        if (indices.size() <= 1) {
            return;
        }
        for (int i = indices.size() - 1; i >= 1; i--) {
            int idx = indices.get(i);
            if (idx >= 0 && idx < tabbedPane.getTabCount()) {
                tabbedPane.removeTabAt(idx);
            }
            if (idx < orderedKeys.size()) {
                orderedKeys.remove(idx);
            }
        }
        reindex();
    }

    private void setTabHeader(int tabIndex, String title, String tooltip, boolean closable, String key) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return;
        }
        JPanel header = new JPanel(new BorderLayout(4, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 6, 0, 2));
        JLabel label = new JLabel(title);
        header.add(label, BorderLayout.CENTER);
        if (closable) {
            header.add(ShellUi.roundTabCloseButton(() -> closeTab(key, closeFallbackKey)), BorderLayout.EAST);
        }
        tabbedPane.setTabComponentAt(tabIndex, header);
        String tip = tooltip != null && !tooltip.isBlank() ? tooltip : title;
        tabbedPane.setToolTipTextAt(tabIndex, tip);
    }

    private void reindex() {
        keyToIndex.clear();
        for (int i = 0; i < orderedKeys.size(); i++) {
            keyToIndex.put(orderedKeys.get(i), i);
        }
    }
}
