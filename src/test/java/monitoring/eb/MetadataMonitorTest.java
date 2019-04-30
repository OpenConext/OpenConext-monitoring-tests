package monitoring.eb;

import org.junit.Test;

public class MetadataMonitorTest {

    @Test
    public void monitor() throws Exception {
        new MetadataMonitor(
                "https://engine.test2.surfconext.nl/authentication/idp/metadata",
                "https://engine.test2.surfconext.nl/authentication/sp/metadata")
                .monitor();
    }

}