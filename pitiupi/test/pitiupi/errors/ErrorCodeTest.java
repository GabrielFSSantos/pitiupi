package pitiupi.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeTest {

    @Test
    void userMessagesInPortuguese() {
        for (ErrorCode code : ErrorCode.values()) {
            String msg = code.getUserMessage();
            assertFalse(msg.isBlank());
            assertTrue(msg.length() > 5);
        }
    }
}
