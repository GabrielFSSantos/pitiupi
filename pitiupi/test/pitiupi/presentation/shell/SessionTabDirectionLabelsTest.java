package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTabDirectionLabelsTest {

    @Test
    void outboundAndInboundTitles() {
        assertEquals("Vostro ↑", SessionTabDirectionLabels.outboundTitle("Vostro"));
        assertEquals("Vostro ↓", SessionTabDirectionLabels.inboundTitle("Vostro"));
    }
}
