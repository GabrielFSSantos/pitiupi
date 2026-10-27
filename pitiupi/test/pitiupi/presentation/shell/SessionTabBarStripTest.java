package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTabBarStripTest {

    @Test
    void stripStaysShorterThanLargeTabContentWouldBe() {
        SessionTabBar bar = new SessionTabBar();
        bar.addOrSelectTab(PluginSessionKeys.ROOM, "Sala geral", new JPanel(), false);
        int stripHeight = bar.getPanel().getPreferredSize().height;
        assertTrue(stripHeight < 80, "faixa de abas deve ser baixa, obteve " + stripHeight);
    }

    @Test
    void secondTabDoesNotCollapseStrip() {
        SessionTabBar bar = new SessionTabBar();
        bar.addOrSelectTab(PluginSessionKeys.ROOM, "Sala geral", new JPanel(), false);
        bar.addOrSelectTab(PluginSessionKeys.PEER_DRAFT, "Nova conversa", new JPanel(), true);
        assertEquals(2, bar.getTabCount());
        bar.selectTab(PluginSessionKeys.PEER_DRAFT);
        assertEquals(PluginSessionKeys.PEER_DRAFT, bar.selectedKey());
    }

    @Test
    void addTabIfAbsentDoesNotChangeSelection() {
        SessionTabBar bar = new SessionTabBar();
        bar.addOrSelectTab(PluginSessionKeys.ROOM, "Sala geral", new JPanel(), false);
        bar.addTabIfAbsent(PluginSessionKeys.PEER_DRAFT, "Nova conversa", "draft", new JPanel(), true);
        assertEquals(PluginSessionKeys.ROOM, bar.selectedKey());
        assertEquals(2, bar.getTabCount());
    }
}
