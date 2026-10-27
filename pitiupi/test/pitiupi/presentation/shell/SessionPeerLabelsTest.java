package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionPeerLabelsTest {

    @Test
    void sessionBannerShowsIpOnceWhenNameEqualsIp() {
        String html = SessionPeerLabels.sessionBannerHtml("192.168.1.10", "192.168.1.10");
        assertEquals("<html><b>Sessão:</b> IP 192.168.1.10</html>", html);
        assertTrue(!html.contains("("));
    }

    @Test
    void sessionBannerShowsNameAndIp() {
        String html = SessionPeerLabels.sessionBannerHtml("Alice", "192.168.1.10");
        assertEquals("<html><b>Sessão:</b> Alice · IP 192.168.1.10</html>", html);
    }

    @Test
    void sessionBannerEscapesHtml() {
        String html = SessionPeerLabels.sessionBannerHtml("A & B", "10.0.0.1");
        assertTrue(html.contains("A &amp; B"));
    }
}
