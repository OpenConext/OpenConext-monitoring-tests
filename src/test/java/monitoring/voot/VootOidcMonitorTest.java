package monitoring.voot;

import org.junit.Test;

public class VootOidcMonitorTest {

    @Test
    public void testMonitor() throws Exception {
        new VootOidcMonitor(
                "https://connect.test2.surfconext.nl/oidc/token",
                "https://voot.test2.surfconext.nl",
                "playground_client",
                "secret",
                "urn:collab:person:example.com:admin")
                .monitor();
    }
}