package monitoring.voot;

import org.junit.Test;

public class VootAuthzMonitorTest {

    @Test
    public void testMonitor() throws Exception {
        new VootAuthzMonitor(
            "https://authz.test2.surfconext.nl/oauth/token",
            "https://voot.test2.surfconext.nl",
            "cool_app_id",
            "secret",
            "urn:collab:person:example.com:admin")
            .monitor();
    }
}