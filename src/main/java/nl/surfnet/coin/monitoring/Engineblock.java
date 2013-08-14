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

import org.joda.time.DateTime;
import org.junit.Assert;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.*;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Timer;

import static org.junit.Assert.assertTrue;

public class Engineblock {

  private static final Logger LOG = LoggerFactory.getLogger(Engineblock.class);

  private final String engineUrlBase;

  private AbstractReloadingMetadataProvider metadataProvider;

  public Engineblock(String url, String engineblockCert) throws Exception {
    this.engineUrlBase = url;

    org.opensaml.DefaultBootstrap.bootstrap();

    Timer backgroundTaskTimer = new Timer(true); // Run as daemon, to not block JVM from quitting
    // Provider of the Engine metadata.
    metadataProvider = new HTTPMetadataProvider(backgroundTaskTimer, new org.apache.commons.httpclient.HttpClient(), engineUrlBase + "/authentication/idp/metadata");
//    metadataProvider = new FilesystemMetadataProvider(new File(getClass().getResource("/metadata.xml").toURI()));

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
            new SignatureValidationFilter(buildTrustEngine(engineblockCert))
    ));
    metadataProvider.setMetadataFilter(filterChain);
  }

  public void validateMetadata() throws Exception {

    // Retrieve from source, immediately thereafter validating it using the configured MetadataFilter(s)
    metadataProvider.initialize();

    // Additional validations below

    // Entity ID from metadata is expected to follow a certain naming scheme. ('https://engine' + domain + '/authentication/idp/metadata')
    String correctEntityId = engineUrlBase + "/authentication/idp/metadata";
    String entityIdFromMetadata = ((EntityDescriptor) metadataProvider.getMetadata()).getEntityID();
    LOG.debug("Engine metadata has entity ID: {}", entityIdFromMetadata);
    Assert.assertEquals(correctEntityId, entityIdFromMetadata);


    EntityDescriptor entityDescriptor = metadataProvider.getEntityDescriptor(entityIdFromMetadata);

    // validUntil interval should be greater than 12 hrs from now
    DateTime halfDayFromNow = new DateTime().plusHours(12);
    assertTrue("validUntil of the metadata should be at least 12 hrs in future", entityDescriptor.getValidUntil().isAfter(halfDayFromNow));

    metadataProvider.destroy();
  }


  /**
   * Get a TrustEngine by the given X509 certificate.
   *
   * @param x509cert the certificate to use in the TrustEngine
   */
  private SignatureTrustEngine buildTrustEngine(String x509cert) throws CertificateException {

    // Local certificate, used for validation of the metadata.
    BasicX509Credential ebCredential = new BasicX509Credential();
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(x509cert.getBytes()));
    ebCredential.setEntityCertificate(cert);
    CredentialResolver ebCredentialResolver = new StaticCredentialResolver(ebCredential);
    return new ExplicitKeySignatureTrustEngine(ebCredentialResolver, new StaticKeyInfoCredentialResolver(ebCredential));
  }

  public void destroy() {
    metadataProvider.destroy();
  }
}
