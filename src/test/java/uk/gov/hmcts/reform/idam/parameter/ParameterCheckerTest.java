package uk.gov.hmcts.reform.idam.parameter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ParameterCheckerTest {

    @InjectMocks
    private ParameterChecker checker;

    @Test
    void shouldThrowExceptionIfBothEnabled() {
        ReflectionTestUtils.setField(checker, "isRestorerEnabled", true);
        ReflectionTestUtils.setField(checker, "isDisposerEnabled", true);
        Exception exception = assertThrows(IllegalStateException.class, () -> checker.init());
        String expectedMessage = "Deletion and restorer are both enabled, please choose only one.";
        assertTrue(exception.getMessage().contains(expectedMessage), "Failed to find expected message");
    }

    @Test
    void shouldNotThrowExceptionOnOneEnabled() {
        ReflectionTestUtils.setField(checker, "isRestorerEnabled", false);
        ReflectionTestUtils.setField(checker, "isDisposerEnabled", true);
        assertDoesNotThrow(() -> checker.init());
        ReflectionTestUtils.setField(checker, "isRestorerEnabled", true);
        ReflectionTestUtils.setField(checker, "isDisposerEnabled", false);
        assertDoesNotThrow(() -> checker.init());
    }

    @Test
    void shouldNotThrowExceptionOnBothDisabled() {
        ReflectionTestUtils.setField(checker, "isRestorerEnabled", false);
        ReflectionTestUtils.setField(checker, "isDisposerEnabled", false);
        assertDoesNotThrow(() -> checker.init());
    }

}
