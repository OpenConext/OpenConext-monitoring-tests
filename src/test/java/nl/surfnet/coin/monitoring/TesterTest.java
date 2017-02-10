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
import org.junit.Before;
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

  private Engineblock engineblock;

  @Before
  public void setup() throws Exception {
    String crt = IOUtils.toString(getClass().getResourceAsStream("/engine.surfconext.nl.pem"));
    engineblock = new Engineblock("https://engine.surfconext.nl", crt, null);
  }

  @Test
  public void validateIdpProxyMetadata() throws Exception {
    engineblock.validateIdpProxyMetadata();
  }
  @Test
  public void validateIdpsMetadata() throws Exception {
    engineblock.validateIdpsMetadata();
  }
  @Test
  public void validateSpProxyMetadata() throws Exception {
    engineblock.validateSpProxyMetadata();
  }
}
