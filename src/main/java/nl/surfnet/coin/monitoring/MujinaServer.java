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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opensaml.util.resource.ClasspathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Container for setting up Mujina SP and Mujina IdP
 */
public class MujinaServer {

    private static final Logger LOG = LoggerFactory.getLogger(MujinaServer.class);

    private static final String MUJINA_VERSION = "3.1.0";

    public static final String KEYSTORE_PASSWORD = "000123";
    private static final String SP_ENTITY_ID = "https://monitoring-sp";
    private static final String IDP_ENTITY_ID = "https://monitoring-idp";
    public static final String MUJINA_REPO_BASE = "https://build.openconext.org/repository/public/releases";

    private Server server;
    private int sslPort;
    private X509Certificate certificate;
    private PrivateKey privateKey;

    public URI setupServer(String conextDomain, String privateKeyPath, String certPath, int sslPort) throws Exception {

        this.sslPort = sslPort;
        String baseURI = "https://localhost:" + sslPort;

        Security.addProvider(new BouncyCastleProvider());


        certificate = (X509Certificate) new PEMReader(new InputStreamReader(new ClasspathResource(certPath).getInputStream())).readObject();

        /*
         * Breaking non-compatible change of the JDK in combination with the deprecated PEMReader
         */
        Object key = new PEMReader(new InputStreamReader(new ClasspathResource(privateKeyPath).getInputStream())).readObject();
        if (key instanceof KeyPair) {
            privateKey = ((KeyPair) key).getPrivate();
        } else {
            privateKey = (PrivateKey) key;
        }
        certificate = (X509Certificate) new PEMReader(new InputStreamReader(new ClasspathResource(certPath).getInputStream())).readObject();

        Assert.notNull(certificate, "Could not properly read certificate:" + certPath);
        Assert.notNull(certificate, "Could not properly read privateKey:" + privateKeyPath);

        server = new Server();

        configureSSL(server, sslPort);

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
                return true;
            }

        });
        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslPort, sslsf));

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

        // enable signing by Mujina IdP
        put = new HttpPut(baseURI + "/idp/api/needs-signing");
        put.setEntity(new StringEntity(String.format("{\"value\": \"%s\"}", "true"), ContentType.APPLICATION_JSON));
        httpClient.execute(put);

        // Set signing credentials on the IdP
        HttpPost post = new HttpPost(baseURI + "/idp/api/signing-credential");
        String certificate = new String(Base64.encodeBase64(this.certificate.getEncoded()));
        String key = new String(Base64.encodeBase64(this.privateKey.getEncoded()));
        String content = String.format("{\"certificate\": \"%s\", \"key\": \"%s\"}", certificate, key);
        LOG.debug("Signing content: {}", content);
        post.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(post);

    }

    private KeyStore createKeystore() throws Exception {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, KEYSTORE_PASSWORD.toCharArray());
        keystore.setKeyEntry("alias", privateKey, KEYSTORE_PASSWORD.toCharArray(), new Certificate[]{certificate});
        return keystore;
    }

    private void deployMujinaApps(Server server) throws MalformedURLException {
        String mujinaIdpUrl = String.format("%s/org/surfnet/coin/mujina-idp/%s/mujina-idp-%s.war", MUJINA_REPO_BASE, MUJINA_VERSION, MUJINA_VERSION);
        String mujinaSpUrl = String.format("%s/org/surfnet/coin/mujina-sp/%s/mujina-sp-%s.war", MUJINA_REPO_BASE, MUJINA_VERSION, MUJINA_VERSION);
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
        sslContextFactory.setKeyStore(createKeystore());
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
        SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
        connector.setPort(port);
        server.addConnector(connector);

    }

    public void stop() throws Exception {
        LOG.debug("Tearing down Jetty servlet container");
        server.stop();
        cleanup();

    }

    private void cleanup() throws Exception {
        File tmp = new File(FileUtils.getTempDirectoryPath());
        File[] files = tmp.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("jetty-0.0.0.0-8443-mujina");
            }
        });
        for (final File file : files) {
            if (!file.delete()) {
                LOG.debug("can't delete file " + file.getName());
            }
        }

    }
}
