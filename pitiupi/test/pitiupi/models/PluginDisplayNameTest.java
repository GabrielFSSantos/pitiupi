package pitiupi.models;

import org.junit.jupiter.api.Test;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;

import javax.swing.JPanel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PluginDisplayNameTest {

    @Test
    void defaultDisplayNameMatchesName() {
        Plugin plugin = new Plugin() {
            @Override
            public String getName() {
                return "Tech";
            }

            @Override
            public String getAuthor() {
                return "test";
            }

            @Override
            public String getVersion() {
                return "1";
            }

            @Override
            public String getIdentifier() {
                return "id";
            }

            @Override
            public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
            }

            @Override
            public List<Class<? extends Message>> getMessages() {
                return List.of();
            }

            @Override
            public void receiveMessage(Message message) {
            }
        };
        assertEquals("Tech", plugin.getDisplayName());
    }

    @Test
    void bindShellWorkspaceContentExposesPanel() {
        JPanel panel = new JPanel();
        Plugin plugin = new Plugin() {
            @Override
            public String getName() {
                return "P";
            }

            @Override
            public String getAuthor() {
                return "a";
            }

            @Override
            public String getVersion() {
                return "1";
            }

            @Override
            public String getIdentifier() {
                return "id";
            }

            @Override
            public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
            }

            @Override
            public List<Class<? extends Message>> getMessages() {
                return List.of();
            }

            @Override
            public void receiveMessage(Message message) {
            }
        };
        assertNull(plugin.getShellWorkspaceContent());
        plugin.bindShellWorkspaceContent(panel);
        assertSame(panel, plugin.getShellWorkspaceContent());
    }
}
