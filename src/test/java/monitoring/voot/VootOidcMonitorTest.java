package monitoring.voot;

import org.junit.Test;

public class VootOidcMonitorTest {

    @Test
    public void testMonitor() throws Exception {
        new VootOidcMonitor(
            "https://oidc.test2.surfconext.nl/token",
            "https://voot.test2.surfconext.nl",
            "https@//authz-playground.test2.surfconext.nl",
            "secret",
            "urn:collab:person:example.com:admin")
            .monitor();
    }
}