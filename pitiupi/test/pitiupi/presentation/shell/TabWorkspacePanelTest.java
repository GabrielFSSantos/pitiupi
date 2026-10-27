package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabWorkspacePanelTest {

    @Test
    void openAndCloseDynamicTabKeepsFixedTabs() {
        TabWorkspacePanel workspace = new TabWorkspacePanel(new JPanel(), new JPanel());
        assertEquals(2, workspace.getTabCount());

        workspace.openOrFocusTab("plugin.a", "Plugin A", new JPanel());
        assertEquals(3, workspace.getTabCount());
        assertTrue(workspace.isPluginTabOpen("plugin.a"));

        workspace.closePluginTab("plugin.a");
        assertEquals(2, workspace.getTabCount());
        assertFalse(workspace.isPluginTabOpen("plugin.a"));
        assertEquals(TabWorkspacePanel.TAB_PLUGINS,
                workspace.getTabbedPane().getTitleAt(0));
        assertEquals(TabWorkspacePanel.TAB_SETTINGS,
                workspace.getTabbedPane().getTitleAt(1));
    }
}
