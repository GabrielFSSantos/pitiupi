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

    @Test
    void closingMiddleTabKeepsTheTabOnItsRightClosable() {
        SessionTabBar bar = new SessionTabBar();
        bar.addOrSelectTab(PluginSessionKeys.ROOM, "Sala geral", new JPanel(), false);
        String firstPeer = PluginSessionKeys.peer("169.254.165.248");
        String secondPeer = PluginSessionKeys.peer("192.168.3.131");
        bar.addOrSelectTab(firstPeer, "Afrodite", "169.254.165.248", new JPanel(), true);
        bar.addOrSelectTab(secondPeer, "Afrodite", "192.168.3.131", new JPanel(), true);
        bar.selectTab(PluginSessionKeys.ROOM);

        bar.closeTab(firstPeer);

        assertEquals(2, bar.getTabCount());
        assertEquals(false, bar.hasTab(firstPeer));
        assertEquals(true, bar.hasTab(secondPeer));
        assertEquals(PluginSessionKeys.ROOM, bar.selectedKey());

        bar.closeTab(secondPeer);

        assertEquals(1, bar.getTabCount());
        assertEquals(false, bar.hasTab(secondPeer));
        assertEquals(PluginSessionKeys.ROOM, bar.selectedKey());
    }

    @Test
    void replaceKeyKeepsTheSameTabClosableUnderTheNewAddress() {
        SessionTabBar bar = new SessionTabBar();
        bar.addOrSelectTab(PluginSessionKeys.ROOM, "Sala geral", new JPanel(), false);
        String cable = PluginSessionKeys.peer("169.254.165.248");
        String wifi = PluginSessionKeys.peer("192.168.3.131");
        bar.addOrSelectTab(cable, "Afrodite", "169.254.165.248", new JPanel(), true);

        assertEquals(true, bar.replaceKey(cable, wifi, "Afrodite", "192.168.3.131", true));
        assertEquals(false, bar.hasTab(cable));
        assertEquals(true, bar.hasTab(wifi));
        assertEquals(2, bar.getTabCount());

        bar.closeTab(wifi);

        assertEquals(1, bar.getTabCount());
        assertEquals(false, bar.hasTab(wifi));
    }
}
