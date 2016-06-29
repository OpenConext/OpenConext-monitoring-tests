package nl.surfnet.coin.monitoring;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run call to PdP for policy decision mimicking EB
 */
public class PdPMonitor extends AbstractMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(PdPMonitor.class);

  public static void main(String[] args) throws Exception {
    PdPMonitor pdPMonitor = new PdPMonitor();

    if (args.length == 4) {
      pdPMonitor.doMonitor(args[0], args[1], args[2], args[3], args[4]);
    } else {
      pdPMonitor.monitor();
    }
  }

  public void monitor() {
    String conextDomain = getProperty("conextDomain");
    String userName = getProperty("pdpUserName");
    String password = getProperty("pdpPassword");
    String idpEntityId = getProperty("idpEntityId");
    String spEntityId = getProperty("spEntityId");

    doMonitor(conextDomain, userName, password, idpEntityId, spEntityId);
  }

  public void doMonitor(String conextDomain, String userName, String password, String idpEntityId, String spEntityId) {
    try {
      RestTemplate restTemplate = new RestTemplate();

      String body = IOUtils.toString(new ClassPathResource("pdp/request.json").getInputStream());
      body = body.replaceAll("@@IDP_ENTITY_ID@@", idpEntityId);
      body = body.replaceAll("@@SP_ENTITY_ID@@", spEntityId);

      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json");
      headers.add("Authorization", "Basic "+ Base64.getEncoder().encodeToString(new String(userName + ":" + password).getBytes()));
      HttpEntity httpEntity = new HttpEntity(body, headers);
      String url = "https://pdp." + conextDomain + "/pdp/api/decide/policy";
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

      assertEquals(200, response.getStatusCode().value());
      assertTrue(response.getBody().contains("urn:oasis:names:tc:xacml:1.0:status:ok"));
      assertTrue(response.getBody().contains("NotApplicable"));
    } catch (Throwable e) {
      LOG.error("Exception while running PdPMonitor, will exit(2)", e);
      System.exit(2);
    }
  }
}
