package alien4cloud.it;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = {
        //
        "classpath:alien/rest/runtime"
        // "classpath:alien/rest/runtime/custom_command.feature"
}, format = { "pretty", "html:target/cucumber/runtime", "json:target/cucumber/cucumber-runtime.json" })
// @Ignore
public class RunRuntimeIT {
}
