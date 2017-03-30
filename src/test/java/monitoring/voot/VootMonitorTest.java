package monitoring.voot;

import org.junit.Test;

public class VootMonitorTest {

    @Test
    public void testMonitor() throws Exception {
        new VootMonitor(
            "https://authz.test2.surfconext.nl",
            "https://voot.test2.surfconext.nl",
            "cool_app_id",
            "secret",
            "urn:collab:person:example.com:admin")
            .monitor();
    }
}