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
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class Monitor {

  private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);

  public static void main(String[] args) throws Exception {

    Monitor monitor = new Monitor();
    URI mujinaBaseUri = monitor.setupServer();

    String conextDomain = args[0];
    new Tester(conextDomain, mujinaBaseUri).runTests();
  }


  public URI setupServer() throws Exception {
    Server server = new Server(8080);

    WebAppContext idpWebapp = new WebAppContext();
    idpWebapp.setContextPath("/idp");
    idpWebapp.setWar(getLocallyCachedWarFile(new URL("https://build.surfconext.nl/repository/public/releases/org/surfnet/coin/mujina-idp/3.1.0/mujina-idp-3.1.0.war")));

    WebAppContext spWebapp = new WebAppContext();
    spWebapp.setContextPath("/sp");
    spWebapp.setWar(getLocallyCachedWarFile(new URL("https://build.surfconext.nl/repository/public/releases/org/surfnet/coin/mujina-sp/3.1.0/mujina-sp-3.1.0.war")));

    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[]{idpWebapp, spWebapp});
    server.setHandler(handlers);

    server.start();

    return server.getURI();
  }

  public String getLocallyCachedWarFile(URL url) {
    String urlPath = url.getPath();
    File file = new File(FileUtils.getTempDirectoryPath() + "/" + FilenameUtils.getName(urlPath));
    if (file.exists()) {
      LOG.debug("File {} exists already (downloaded before), will use this, for given URL {}", file.getPath(), url);
    } else {
      LOG.debug("File {} does not exist yet, will download from {}", file.getPath(), url);
      try {
        FileUtils.copyURLToFile(url, file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return file.getPath();
  }
}
