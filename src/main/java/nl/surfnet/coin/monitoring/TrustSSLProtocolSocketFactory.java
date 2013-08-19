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

import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Copied from org.apache.commons.httpclient.contrib.ssl.TrustSSLProtocolSocketFactory.
 * That class does in fact use the wrong 'material' class (KeyMaterial instead of TrustMaterial).
 * KeyMaterial mandates the protection with a private key, which is nonsense for a trust store.
 * TrustMaterial does not do this.
 */
public class TrustSSLProtocolSocketFactory extends HttpSecureProtocol {

  /**
   * Constructor
   * @see {@link org.apache.commons.httpclient.contrib.ssl.TrustSSLProtocolSocketFactory#TrustSSLProtocolSocketFactory(String)}  for more elaborate docs.
   *
   * @param trustStore the contents of the trust store. Either JKS format or in PEM, Base64 encoded.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public TrustSSLProtocolSocketFactory(String trustStore) throws GeneralSecurityException, IOException {
      TrustMaterial tm = new TrustMaterial(trustStore.getBytes(), null);
      super.setTrustMaterial(tm);
  }
}
