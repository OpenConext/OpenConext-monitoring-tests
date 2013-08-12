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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Engineblock {
  private final String engineUrlBase;

  public Engineblock(String url) {
    this.engineUrlBase = url;
  }


  public InputStream getMetadata() {

    try {
      HttpClient defaultHttpClient = new DefaultHttpClient();

      defaultHttpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, acceptAnySSLSocketFactory()));
      return defaultHttpClient.execute(new HttpGet(engineUrlBase + "/authentication/idp/metadata")).getEntity().getContent();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public SSLSocketFactory acceptAnySSLSocketFactory() {

    try {
      return new SSLSocketFactory(new TrustStrategy() {

        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          // Oh, I am easy...
          return true;
        }

      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
