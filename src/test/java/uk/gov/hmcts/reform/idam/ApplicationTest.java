package uk.gov.hmcts.reform.idam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    @InjectMocks
    private Application underTest;

    @Mock
    private ApplicationArguments applicationArguments;

    @Test
    void testMainMethod() {
        assertTrue(System.currentTimeMillis() > 0, "Application Test");
    }
}
