/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.monitoring;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class Tester {

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tester.class);

  private final String conextDomain;
  private final String engineblockCert;
  private final String trustChain;
  private WebDriver driver;

  private MujinaClient mujinaClient;

  private void initWebDriver() {
    driver = new HtmlUnitDriver(true);
    driver.manage().deleteAllCookies();
  }

  public Tester(String conextDomain, URI serverBaseUri, String engineblockCert, String trustChain) throws Exception {
    initWebDriver();
    mujinaClient = new MujinaClient(driver, serverBaseUri);
    this.conextDomain = conextDomain;
    this.engineblockCert = engineblockCert;
    this.trustChain = trustChain;
  }

  public void runTests() throws IOException {
    try {
      LOG.info("Running test for login flow using Mujina SP/IdP");
      loginFlow();
    } catch (Throwable e) {
      File tmpFile = File.createTempFile("monitor", ".txt");
      String output = String.format("Current URL: %s\nPage source:\n%s", driver.getCurrentUrl(), driver.getPageSource());
      FileUtils.writeStringToFile(tmpFile, output);
      LOG.info("Caught exception. WebDriver's current state has been dumped in file: {} Will rethrow exception.", tmpFile.getPath());
      throw new RuntimeException(e);
    } finally {
      driver.quit();
    }


    LOG.info("Running test for validating metadata of Engineblock");
    metadata();


  }

  public void metadata() {
    try {

      Engineblock engineblock = new Engineblock("https://engine." + conextDomain, engineblockCert, trustChain);

      engineblock.validateMetadata();
      engineblock.destroy();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void loginFlow() {
    mujinaClient.spHome();

    LOG.debug("url: {}", driver.getCurrentUrl());

    // Go to protected page
    mujinaClient.protectedPage();

    // Expect EB WAYF
    assertTrue("Expecting a WAYF. URL was: " + driver.getCurrentUrl(), driver.getPageSource().contains("Login via your institution"));

    chooseIdPByLabel(driver, "monitoring-idp");
    mujinaClient.login("monitor-user-" + System.currentTimeMillis(), "somepass");
    acceptConsent(driver);
    assertTrue("should be on SP", driver.getCurrentUrl().contains("/sp/user.jsp"));
    assertTrue("Should contain SAML attributes", driver.findElement(By.id("assertionAttributes")).getText().contains("j.doe@example.com"));
    assertTrue("Should contain my full name", driver.getPageSource().contains("John Doe"));



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
