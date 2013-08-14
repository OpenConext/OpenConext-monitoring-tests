package nl.surfnet.coin.monitoring;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesterTest {

  private static final Logger LOG = LoggerFactory.getLogger(TesterTest.class);


  @Test
  public void validateMetadata() throws Exception {
      String crt = IOUtils.toString(getClass().getResourceAsStream("/engineblock.crt"));
      new Engineblock("https://engine.surfconext.nl", crt).validateMetadata();
  }
}
