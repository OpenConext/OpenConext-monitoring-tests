package monitoring.eb;

import org.junit.Test;

import java.util.UUID;

public class SamlProxyMonitorTest {

    @Test
    public void monitor() throws Exception {
        doMonitor("admin");
    }

    @Test
    public void monitorWithConsent() throws Exception {
        doMonitor(UUID.randomUUID().toString());
    }

    private void doMonitor(String userName) throws Exception {
        new SamlProxyMonitor(
            "https://mujina-sp.test2.surfconext.nl", "http://mock-idp",
            userName).monitor();
    }
}