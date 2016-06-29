package nl.surfnet.coin.monitoring;

import org.junit.Test;

public class PdPMonitorTest {

  private PdPMonitor subject = new PdPMonitor();

  @Test
  public void testMonitor() throws Exception {
    subject.doMonitor("test2.surfconext.nl", "pdp_admin", "secret", "https://engine.test.surfconext.nl/authentication/idp/metadata", "https://engine.test2.surfconext.nl/authentication/sp/metadata");
  }
}