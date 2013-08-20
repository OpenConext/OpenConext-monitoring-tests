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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.*;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class Engineblock {

  private static final Logger LOG = LoggerFactory.getLogger(Engineblock.class);

  private final String engineUrlBase;
  private final String engineblockCert;
  private final String trustChain;
  private Set<BaseMetadataProvider> metadataProviders = new HashSet<BaseMetadataProvider>();

  public Engineblock(String url, String engineblockCert, String trustChain) throws Exception {
    this.engineUrlBase = url;

    this.engineblockCert = engineblockCert;
    this.trustChain = trustChain;

    org.opensaml.DefaultBootstrap.bootstrap();

    // Provider of the Engine metadata.

    if (trustChain != null) {
      configureHttpClientSslTrust(trustChain);
    }

  }

  private void configureHttpClientSslTrust(String cert) throws IOException, GeneralSecurityException {

    Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new TrustSSLProtocolSocketFactory(cert), 443));
  }

  public void validateIdpProxyMetadata() throws Exception {

    MetadataProvider metadataProvider = createMetadataProvider(engineUrlBase + "/authentication/idp/metadata");

    // Entity ID from metadata is expected to follow a certain naming scheme. ('https://engine' + domain + '/authentication/idp/metadata')
    String correctEntityId = engineUrlBase + "/authentication/idp/metadata";
    String entityIdFromMetadata = ((EntityDescriptor) metadataProvider.getMetadata()).getEntityID();
    LOG.debug("Engine metadata has entity ID: {}", entityIdFromMetadata);
    Assert.assertEquals(correctEntityId, entityIdFromMetadata);


    EntityDescriptor entityDescriptor = metadataProvider.getEntityDescriptor(entityIdFromMetadata);

    // validUntil interval should be greater than 12 hrs from now
    DateTime halfDayFromNow = new DateTime().plusHours(12);
    assertTrue("validUntil of the metadata should be at least 12 hrs in future", entityDescriptor.getValidUntil().isAfter(halfDayFromNow));

  }


  public void validateIdpsMetadata() throws Exception {

    // creating the provider will effectively validate against schema and signature.
    createMetadataProvider(engineUrlBase + "/authentication/proxy/idps-metadata");

  }


  public void validateSpProxyMetadata() throws Exception {
    createMetadataProvider(engineUrlBase + "/authentication/sp/metadata");
  }

  /**
   * Get a TrustEngine by the given X509 certificate (chain).
   *
   * @param x509certChain the certificate chain to use in the TrustEngine
   */
  private SignatureTrustEngine buildTrustEngine(String entityCertPEM, String x509certChain) throws CertificateException {

    // Local certificate, used for validation of the metadata.
    BasicX509Credential ebCredential = new BasicX509Credential();
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    X509Certificate entityCertificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(entityCertPEM.getBytes()));
    ebCredential.setPublicKey(entityCertificate.getPublicKey());
    ebCredential.setEntityCertificate(entityCertificate);

    if (x509certChain != null) {
      @SuppressWarnings("unchecked")
      Collection<X509Certificate> certs = (Collection<X509Certificate>) cf.generateCertificates(new ByteArrayInputStream(x509certChain.getBytes()));
      ebCredential.setEntityCertificateChain(certs);
    }
    return new ExplicitKeySignatureTrustEngine(new StaticCredentialResolver(ebCredential), new StaticKeyInfoCredentialResolver(ebCredential));
  }

  public void destroy() {
    for (BaseMetadataProvider prov : metadataProviders) {
      prov.destroy();
    }
  }

  public MetadataProvider createMetadataProvider(String url) throws MetadataProviderException, CertificateException {

    Timer backgroundTaskTimer = new Timer(true); // Run as daemon, to not block JVM from quitting


    HttpClient client = new HttpClient();
    AbstractReloadingMetadataProvider metadataProvider = new HTTPMetadataProvider(backgroundTaskTimer, client, url);

    metadataProvider.setRequireValidMetadata(true);
    metadataProvider.setParserPool(new BasicParserPool());

    // Actually validate metadata after retrieval
    MetadataFilterChain filterChain = new MetadataFilterChain();

    /*
      Filters in use are:
       - schemavalidation. Based on the default schemata (SAML, XML), and extended with the MDUI-metadata schema
       - Signature validation, backed by the locally stored certificate of Engineblock
     */
    filterChain.setFilters(Arrays.<MetadataFilter>asList(
            new SchemaValidationFilter(new String[] {"/schema__/sstc-saml-metadata-ui-v1.0.xsd"}),
            new SignatureValidationFilter(buildTrustEngine(engineblockCert, trustChain))
    ));
    metadataProvider.setMetadataFilter(filterChain);

    // Retrieve from source, immediately thereafter validating it using the configured MetadataFilter(s)
    metadataProvider.initialize();

    // for later destroying
    metadataProviders.add(metadataProvider);

    return metadataProvider;
  }


  public void acceptConsentIfPresent(WebDriver driver) {
    try {
      WebElement button = driver.findElement(By.id("accept_terms_button"));
      button.click();
  } catch (NoSuchElementException e) {
      LOG.debug("No consent screen, will skip without error.");
   }

  }

  /**
   * Choose the IdP that we currently working with
   */
  public void chooseIdPByLabel(WebDriver driver, String label) {
    final String xpathExpression = String.format("//span[normalize-space()='%s']", StringEscapeUtils.escapeXml(label));
    final WebElement element = driver.findElement(By.xpath(xpathExpression));
    element.click();
  }

}
