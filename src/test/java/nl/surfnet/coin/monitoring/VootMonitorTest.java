package nl.surfnet.coin.monitoring;

import org.junit.Test;

import static org.junit.Assert.*;

public class VootMonitorTest {

  private VootMonitor subject = new VootMonitor();

  @Test
  public void testMonitor() throws Exception {
    subject.doMonitor("test.surfconext.nl", "cool_app_id", "secret", "urn:collab:person:example.com:admin");
  }
}