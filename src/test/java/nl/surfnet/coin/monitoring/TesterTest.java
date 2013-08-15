package nl.surfnet.coin.monitoring;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.junit.Assert.assertTrue;

public class TesterTest {

  private static final Logger LOG = LoggerFactory.getLogger(TesterTest.class);


  @Test
  @Ignore("Because it needs a running Mujina sp/idp")
  public void loginFlow() throws Exception {

    WebDriver driver = new FirefoxDriver();
    driver.manage().deleteAllCookies();

    Mujina mujina = new Mujina(driver, URI.create("https://localhost:8443"));
    Engineblock eb = new Engineblock("https://engine.demo.openconext.org", IOUtils.toString(getClass().getResourceAsStream("/engineblock-prod.crt")));
    mujina.protectedPage();
    eb.chooseIdPByLabel(driver, "monitoring-idp");
    mujina.login("i-am-a-user-" + System.currentTimeMillis(), "pass");
    eb.acceptConsentIfPresent(driver);
    assertTrue("should be on SP", driver.getCurrentUrl().contains("/sp/user.jsp"));
    assertTrue("Should contain SAML attributes", driver.findElement(By.id("assertionAttributes")).getText().contains("j.doe@example.com"));
    driver.quit();
  }

  @Test
  public void validateMetadata() throws Exception {
      String crt = IOUtils.toString(getClass().getResourceAsStream("/engineblock-prod.crt"));
      new Engineblock("https://engine.surfconext.nl", crt).validateMetadata();
  }
}
