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
import org.springframework.core.io.ClassPathResource;

public class MetadataMonitor extends AbstractMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataMonitor.class);
  private Engineblock engineblock;

  public static void main(String[] args) throws Exception {
    new MetadataMonitor().monitor();
  }

  public void monitor() {
    try {
      String conextDomain = getProperty("conextDomain");
      String certificateFile = getProperty("engineBlockCertFile");
      String trustChainFile = getProperty("engineBlockTrustChainFile");
      String engineblockCertAsString = IOUtils.toString(new ClassPathResource(certificateFile).getURL());
      String trustChainAsString = null;

      if (!trustChainFile.isEmpty()) {
        trustChainAsString = IOUtils.toString(new ClassPathResource(trustChainFile).getURL());
      }

      engineblock = new Engineblock("https://engine." + conextDomain, engineblockCertAsString, trustChainAsString);
      metadata();
      System.exit(0);
    } catch (Throwable t) {
      LOG.error("Exiting because of exception", t);
      System.exit(2);
    }
  }

  public void metadata() {
    LOG.info("Running tests for validating metadata of Engineblock");
    try {

      LOG.info("Validating IDP Proxy metadata...");
      engineblock.validateIdpProxyMetadata();

      LOG.info("Validating SP Proxy metadata...");
      engineblock.validateSpProxyMetadata();

      LOG.info("Validating IDPs metadata...");
      engineblock.validateIdpsMetadata();

      engineblock.destroy();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
