package uk.gov.hmcts.reform.idam.bdd;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    plugin = {"pretty", "html:target/cucumber/cucumber-report.html"},
    monochrome = true)
public class CucumberTest {

}
