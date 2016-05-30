package nl.surfnet.coin.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run group call to VOOT, using Client Credentials
 * <p>
 * It uses the
 */
public class VootMonitor extends AbstractMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(VootMonitor.class);

  private static final String nonExistingPersonId = "urn:collab:person:some-nonexisting-org:monitoring-user";

  public static void main(String[] args) throws Exception {
    VootMonitor vootMonitor = new VootMonitor();

    if (args.length == 4) {
      vootMonitor.doMonitor(args[0], args[1], args[2], args[3]);
    } else {
      vootMonitor.monitor();
    }
  }

  public void monitor() {
    String conextDomain = getProperty("conextDomain");
    String clientId = getProperty("vootClientId");
    String secret = getProperty("vootClientSecret");
    String personId = getProperty("vootPersonId");

    doMonitor(conextDomain, clientId, secret, personId);
  }

  public void doMonitor(String conextDomain, String clientId, String secret, String personId) {
    try {
      ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
      details.setAccessTokenUri("https://authz." + conextDomain + "/oauth/token");
      details.setClientId(clientId);
      details.setClientSecret(secret);
      details.setScope(Collections.singletonList("groups"));

      OAuth2RestTemplate template = new OAuth2RestTemplate(details);

      List groups = template.getForObject("https://voot." + conextDomain + "/internal/groups/{userId}", List.class, personId);
      assertFalse(groups.isEmpty());

      // Non-existent user
      groups = template.getForObject("https://voot." + conextDomain + "/internal/groups/{userId}", List.class, nonExistingPersonId);
      assertTrue(groups.isEmpty());

    } catch (Throwable e) {
      LOG.error("Exception while running, will exit(2)", e);
      System.exit(2);
    }
  }
}
