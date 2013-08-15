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
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opensaml.util.resource.ClasspathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 * Main application class.
 */
public class Monitor {

  private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);

  private static final String MUJINA_VERSION = "3.1.0";

  public static final String SERVER_SSL_CERT = "/mujina-server-cert.der";
  public static final String SERVER_SSL_KEY = "/mujina-server-key.der";

  public static final String KEYSTORE_PASSWORD = "000123";
  private static final int SSL_PORT = 8443;
  private static final String SP_ENTITY_ID = "https://monitoring-sp";
  private static final String IDP_ENTITY_ID = "https://monitoring-idp";

  private Server server;


  /**
   *
   * Main method.
   * @param args command line arguments:
   *             <ol>
   *             <li><em>conextDomain</em> The domain to monitor. Examples: <em>surfconext.nl</em>, <em>demo.openconext.org</em></li>
   *             <li><em>engineblock-cert</em> A filename containing the X509, pem-headered, public key of the Engineblock instance to monitor (for validation purposes)</li>
   *             </ol>
   */
  public static void main(String[] args) throws Exception {

    if (args.length < 2) {
      System.err.println("Need 2 arguments.");
      System.err.println("Usage: <program jar> <conextDomain> <certificateFile>\n");
      System.err.println("conextDomain: domain to monitor. Example: surfconext.nl");
      System.err.println("certificateFile: Engineblock's public key, X509, with pem headers");
      return;
    }

    Monitor monitor = new Monitor();
    try {
      String conextDomain = args[0];
      String certificateFile = args[1];
      LOG.info("Setting up Jetty servlet container and deploying Mujina IdP and SP");
      URI mujinaBaseUri = monitor.setupServer(conextDomain);
      LOG.info("Running tests");
      new Tester(conextDomain, mujinaBaseUri, IOUtils.toString(new File(certificateFile).toURI())).runTests();
      LOG.info("All tests succeeded");
    } finally {
      LOG.debug("Tearing down Jetty servlet container");
      monitor.stopServer();
    }
  }

  private void stopServer() throws Exception {
    server.stop();
  }


  public URI setupServer(String conextDomain) throws Exception {

    String baseURI = "https://localhost:" + SSL_PORT;

    server = new Server();

    configureSSL(server, SSL_PORT);

    deployMujinaApps(server);

    server.start();

    setSAMLEndpoints(baseURI, conextDomain);

    return URI.create(baseURI);
  }

  /**
   * Configure Mujina IdP and SP using their respective APIs.
   *
   * @throws Exception
   */
  private void setSAMLEndpoints(String baseURI, String conextDomain) throws Exception {
    DefaultHttpClient httpClient = new DefaultHttpClient();

    // SSL socket factory that does not complain about self signed certs.
    SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {
      public boolean isTrusted(
              final X509Certificate[] chain, String authType) throws CertificateException {
        // Oh, I am easy...
        return true;
      }

    });
    httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSL_PORT, sslsf));

    // Set Engine's URL as the endpoint for Mujina SP
    HttpPut put = new HttpPut(baseURI + "/sp/api/ssoServiceURL");
    String ssoServiceUrl = String.format("https://engine.%s/authentication/idp/single-sign-on", conextDomain);
    put.setEntity(new StringEntity(String.format("{\"value\": \"%s\"}", ssoServiceUrl), ContentType.APPLICATION_JSON));
    httpClient.execute(put);

    put = new HttpPut(baseURI + "/sp/api/assertionConsumerServiceURL");
    String assertionConsumerUrl = String.format("%s/sp/AssertionConsumerService", baseURI);
    put.setEntity(new StringEntity(String.format("{\"value\": \"%s\"}", assertionConsumerUrl), ContentType.APPLICATION_JSON));
    httpClient.execute(put);

    // Set SP Entity ID on Mujina SP
    put = new HttpPut(baseURI + "/sp/api/entityid");
    put.setEntity(new StringEntity(String.format("{\"value\": \"%s\"}", SP_ENTITY_ID), ContentType.APPLICATION_JSON));
    httpClient.execute(put);

    // Set Entity ID on Mujina IdP
    put = new HttpPut(baseURI + "/idp/api/entityid");
    put.setEntity(new StringEntity(String.format("{\"value\": \"%s\"}", IDP_ENTITY_ID), ContentType.APPLICATION_JSON));
    httpClient.execute(put);


  }

  private KeyStore createKeystore() throws Exception {
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(null, KEYSTORE_PASSWORD.toCharArray());
    Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ClasspathResource(SERVER_SSL_CERT).getInputStream());

    KeyFactory kf = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(IOUtils.toByteArray(new ClasspathResource(SERVER_SSL_KEY).getInputStream()));
    PrivateKey ff = kf.generatePrivate(keysp);
    keystore.setKeyEntry("alias", ff, KEYSTORE_PASSWORD.toCharArray(), new Certificate[]{cert});

    return keystore;
  }

  private void deployMujinaApps(Server server) throws MalformedURLException {
    String mujinaIdpUrl = String.format("https://build.surfconext.nl/repository/public/releases/org/surfnet/coin/mujina-idp/%s/mujina-idp-%s.war", MUJINA_VERSION, MUJINA_VERSION);
    String mujinaSpUrl = String.format("https://build.surfconext.nl/repository/public/releases/org/surfnet/coin/mujina-sp/%s/mujina-sp-%s.war", MUJINA_VERSION, MUJINA_VERSION);
    WebAppContext idpWebapp = new WebAppContext();
    idpWebapp.setContextPath("/idp");

    idpWebapp.setWar(getLocallyCachedWarFile(new URL(mujinaIdpUrl)));

    WebAppContext spWebapp = new WebAppContext();
    spWebapp.setContextPath("/sp");

    spWebapp.setWar(getLocallyCachedWarFile(new URL(mujinaSpUrl)));

    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[]{idpWebapp, spWebapp});
    server.setHandler(handlers);

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

  public void configureSSL(Server server, int port) throws Exception {

    SslContextFactory sslContextFactory = new SslContextFactory();
    SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");

    sslConnectionFactory.getSslContextFactory().setKeyStore(createKeystore());
    sslConnectionFactory.getSslContextFactory().setKeyStorePassword(KEYSTORE_PASSWORD);

    HttpConfiguration config = new HttpConfiguration();
    config.setSecureScheme("https");
    config.setSecurePort(port);
    HttpConfiguration sslConfiguration = new HttpConfiguration(config);
    sslConfiguration.addCustomizer(new SecureRequestCustomizer());
    ServerConnector connector = new ServerConnector(server,
            sslConnectionFactory,
            new HttpConnectionFactory(sslConfiguration));


    connector.setPort(port);
    server.addConnector(connector);

  }

}
