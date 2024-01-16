package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.Application;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class SpringBootIntegrationTest {


}
