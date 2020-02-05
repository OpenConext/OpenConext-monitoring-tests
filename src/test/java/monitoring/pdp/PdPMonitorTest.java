package monitoring.pdp;

import org.junit.Test;

public class PdPMonitorTest {

    @Test
    public void monitor() throws Exception {
        new PdPMonitor(
                "pdp_admin",
                "secret",
                "https://engine.test2.surfconext.nl/authentication/idp/metadata",
                "https://engine.test2.surfconext.nl/authentication/sp/metadata",
                "https://pdp.test2.surfconext.nl")
                .monitor();
    }

}