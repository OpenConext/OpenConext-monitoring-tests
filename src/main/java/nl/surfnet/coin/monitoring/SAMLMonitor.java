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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.BindException;
import java.net.URI;


/**
 * Main application class.
 */
public class SAMLMonitor extends AbstractMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(SAMLMonitor.class);
  private static MujinaServer mujinaServer;


  public static void main(String[] args) throws Exception {
    new SAMLMonitor().monitor();
  }

  public void monitor() throws Exception {

    try {
      String conextDomain = getProperty("conextDomain");
      String privateKeyPath = getProperty("mujinaPrivateKeyPath");
      String certPath = getProperty("mujinaCertPath");
      String sslPort = getProperty("mujinaSslPort");
      String mujinaUri = getProperty("mujinaURI");

      Preconditions.checkArgument(StringUtils.hasText(sslPort), "The sslPort property in monitor.properties is absent");

      LOG.info("Setting up Jetty servlet container and deploying Mujina IdP and SP");
      mujinaServer = new MujinaServer();
      URI mujinaBaseUri = mujinaServer.setupServer(conextDomain, privateKeyPath, certPath, mujinaUri, Integer.valueOf(sslPort));
      LOG.info("Running tests");
      new Tester(mujinaBaseUri, "https://monitoring-idp", "/sp").runTests();
      LOG.info("All tests succeeded");
      mujinaServer.stop();
      System.exit(0);
    } catch (BindException e) {
      LOG.warn("Address in use. We terminate normally as this can happen because the previous test is still running");
      System.exit(0);
    }
    catch (Throwable t) {
      LOG.error("Exiting because of exception", t);
      mujinaServer.stop();
      System.exit(2);
    }
  }


}
