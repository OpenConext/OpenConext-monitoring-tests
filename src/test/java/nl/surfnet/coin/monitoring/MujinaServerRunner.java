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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The name of this class does not end with Test, to make it not run as a regular unit test. Is is meant only as an integration test helper
 */
public class MujinaServerRunner {
  private static final Logger LOG = LoggerFactory.getLogger(MujinaServerRunner.class);

  @Test
  public void setupServer() throws Exception {
    String conextDomain = "demo.openconext.org";
    new MujinaServer().setupServer(conextDomain);
    LOG.debug("Server started, will suspend thread now (sleep for a long long time)");
    Thread.sleep(1000 * 3600 * 24);
  }
}
