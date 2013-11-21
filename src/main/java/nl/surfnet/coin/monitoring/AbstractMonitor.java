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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AbstractMonitor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractMonitor.class);

  private Properties properties;

  public AbstractMonitor() {
    String propertiesFileName = System.getProperty("monitor.propertiesFile", "monitor.properties");
    loadProperties(propertiesFileName);
  }

  public void loadProperties(String propertiesFileName) {
    try {
      properties = new Properties();
      properties.load(new FileReader(propertiesFileName));
    } catch (IOException e) {
      LOG.error("while reading properties from " + "monitor.properties", e);
    }
    LOG.debug("Loaded properties from {}", propertiesFileName);
    LOG.trace("Properties that were loaded: ", properties.propertyNames());
  }
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

}
