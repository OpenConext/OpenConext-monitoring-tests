package nl.surfnet.coin.monitoring;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.URI;

import static org.junit.Assert.assertTrue;

public class Tester {

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tester.class);

  private final String conextDomain;
  private final String engineblockCert;
  private WebDriver driver;

  private Mujina mujina;

  private void initWebDriver() {
    driver = new FirefoxDriver();
  }

  public Tester(String conextDomain, URI serverBaseUri, String engineblockCert) throws Exception {
    initWebDriver();
    mujina = new Mujina(driver, serverBaseUri);
    this.conextDomain = conextDomain;
    this.engineblockCert = engineblockCert;
  }

  public void runTests() {

    loginFlow();

    metadata();

    driver.quit();
  }

  public void restartBrowser() {
    driver.quit();
    initWebDriver();
    mujina.setDriver(driver);
  }

  public void metadata() {
    try {

      Engineblock engineblock = new Engineblock("https://engine." + conextDomain, engineblockCert);

      engineblock.validateMetadata();
      engineblock.destroy();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void loginFlow() {
    mujina.spHome();

    LOG.debug("url: {}", driver.getCurrentUrl());

    mujina.protectedPage();

    assertTrue(driver.getCurrentUrl().contains("/idp/"));

    mujina.login("monitor-user", "somepass");

    assertTrue(driver.getCurrentUrl().contains("/sp/"));
    assertTrue(driver.getPageSource().contains("John Doe"));

  }

}
