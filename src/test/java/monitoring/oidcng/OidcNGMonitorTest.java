package monitoring.oidcng;

import org.junit.Test;

public class OidcNGMonitorTest {

    @Test
    public void monitor() throws Exception {
        new OidcNGMonitor(
                "https://connect.test2.surfconext.nl/oidc",
                "playground_client",
                "secret",
                "resource-server-playground-client",
                "secret")
                .monitor();
    }
}