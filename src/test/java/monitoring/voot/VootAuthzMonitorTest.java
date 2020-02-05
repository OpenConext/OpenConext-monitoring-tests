package monitoring.voot;

import org.junit.Test;

public class VootAuthzMonitorTest {

    @Test
    public void testMonitor() throws InterruptedException {
        new VootAuthzMonitor(
                "https://authz.test2.surfconext.nl/oauth/token",
                "https://voot.test2.surfconext.nl",
                "engineblock",
                "secret",
                "urn:collab:person:example.com:admin")
                .monitor();
    }
}