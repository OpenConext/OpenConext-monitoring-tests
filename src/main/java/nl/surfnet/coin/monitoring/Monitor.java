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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;


/**
 * Main application class.
 */
public class Monitor {

  private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);
  private static MujinaServer mujinaServer;


  /**
   *
   * Main method.
   * @param args command line arguments:
   *             <ol>
   *             <li><em>conextDomain</em> The domain to monitor. Examples: <em>surfconext.nl</em>, <em>demo.openconext.org</em></li>
   *             <li><em>engineblock-cert</em> A filename containing the X509, pem-headered, public key of the Engineblock instance to monitor (for validation purposes)</li>
   *             <li><em>trustChain</em> A filename containing the X509, pem-headered, trust chain of the Engineblock instance to monitor</li>
   *             </ol>
   */
  public static void main(String[] args) throws Exception {

    if (args.length < 2) {
      System.err.println("Need 2 or 3 arguments.");
      System.err.println("Usage: <program jar> <conextDomain> <certificateFile> <trustChain>\n");
      System.err.println("conextDomain: domain to monitor. Example: surfconext.nl");
      System.err.println("certificateFile: Engineblock's public key, X509, with pem headers");
      System.err.println("trustChainFile (optional): Trust chain resolving Engine Block's public key's trust, X509, with pem headers");
      System.exit(3);
    }

    try {
      String conextDomain = args[0];
      String certificateFile = args[1];
      String trustChainFile = null;
      if (args.length == 3) {
        trustChainFile = args[2];
      }
      LOG.info("Setting up Jetty servlet container and deploying Mujina IdP and SP");
      mujinaServer = new MujinaServer();
      URI mujinaBaseUri = mujinaServer.setupServer(conextDomain);
      LOG.info("Running tests");
      String trustChain = trustChainFile == null ? null : IOUtils.toString(new File(trustChainFile).toURI());
      new Tester(conextDomain, mujinaBaseUri, IOUtils.toString(new File(certificateFile).toURI()), trustChain).runTests();
      LOG.info("All tests succeeded");
      System.exit(0);
    } catch (Throwable t) {
      LOG.error("Exiting because of exception", t);
      System.exit(2);
    } finally {
      LOG.debug("Tearing down Jetty servlet container");
      mujinaServer.stop();
    }
  }


}
