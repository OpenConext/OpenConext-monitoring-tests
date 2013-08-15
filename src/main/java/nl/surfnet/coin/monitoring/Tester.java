package nl.surfnet.coin.monitoring;

import org.apache.commons.lang.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.net.URI;

import static org.junit.Assert.assertTrue;

public class Tester {

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tester.class);

  private final String conextDomain;
  private final String engineblockCert;
  private WebDriver driver;

  private Mujina mujina;

  private void initWebDriver() {
    driver = new HtmlUnitDriver(true);
    driver.manage().deleteAllCookies();
  }

  public Tester(String conextDomain, URI serverBaseUri, String engineblockCert) throws Exception {
    initWebDriver();
    mujina = new Mujina(driver, serverBaseUri);
    this.conextDomain = conextDomain;
    this.engineblockCert = engineblockCert;
  }

  public void runTests() {
    LOG.info("Running test for login flow using Mujina SP/IdP");
    loginFlow();

    LOG.info("Running test for validating metadata of Engineblock");
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

    // Go to protected page
    mujina.protectedPage();

    // Expect EB WAYF
    assertTrue("Expecting a WAYF. URL was: " + driver.getCurrentUrl(), driver.getPageSource().contains("Login via your institution"));

    chooseIdPByLabel(driver, "monitoring-idp");
    mujina.login("i-am-a-user", "pass");
    acceptConsent(driver);
    assertTrue("should be on SP", driver.getCurrentUrl().contains("/sp/user.jsp"));
    assertTrue("Should contain SAML attributes", driver.findElement(By.id("assertionAttributes")).getText().contains("j.doe@example.com"));
    driver.quit();

    mujina.login("monitor-user", "somepass");

    assertTrue(driver.getCurrentUrl().contains("/sp/"));
    assertTrue(driver.getPageSource().contains("John Doe"));

  }


  private void acceptConsent(WebDriver driver) {
    driver.findElement(By.id("accept_terms_button")).click();
  }

  /**
   * Choose the IdP that we currently working with
   */
  private void chooseIdPByLabel(WebDriver driver, String label) {
    final String xpathExpression = String.format("//span[normalize-space()='%s']", StringEscapeUtils.escapeXml(label));
    final WebElement element = driver.findElement(By.xpath(xpathExpression));
    element.click();

  }

}
