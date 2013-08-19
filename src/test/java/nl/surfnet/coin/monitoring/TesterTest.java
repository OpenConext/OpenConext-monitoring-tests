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

    MujinaClient mujinaClient = new MujinaClient(driver, URI.create("https://localhost:8443"));
    Engineblock eb = new Engineblock("https://engine.demo.openconext.org", IOUtils.toString(getClass().getResourceAsStream("/engine.demo.openconext.org.pem")), IOUtils.toString(getClass().getResourceAsStream("/openconext_ca.pem")));
    mujinaClient.protectedPage();
    eb.chooseIdPByLabel(driver, "monitoring-idp");
    mujinaClient.login("i-am-a-user-" + System.currentTimeMillis(), "pass");
    eb.acceptConsentIfPresent(driver);
    assertTrue("should be on SP", driver.getCurrentUrl().contains("/sp/user.jsp"));
    assertTrue("Should contain SAML attributes", driver.findElement(By.id("assertionAttributes")).getText().contains("j.doe@example.com"));
    driver.quit();
  }

  @Test
  public void validateMetadata() throws Exception {
    String crt = IOUtils.toString(getClass().getResourceAsStream("/engine.surfconext.nl.pem"));
    new Engineblock("https://engine.surfconext.nl", crt, null).validateMetadata();
  }
}
