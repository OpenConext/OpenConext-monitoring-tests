package nl.surfnet.coin.monitoring;

import nl.surfnet.coin.api.client.OpenConextOAuthClientImpl;
import nl.surfnet.coin.api.client.domain.Group;
import nl.surfnet.coin.api.client.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Run person call to API, using Client Credentials
 *
 * Usage:
 * <pre>
 * ApiMonitor.main [API base URL] [Client ID] [Client secret] [person ID to test]
 * </pre>
 */
public class ApiMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(ApiMonitor.class);

  private static final String nonExistingPersonId = "urn:collab:some-nonexisting-org:monitoring-user";

  public static void main(String[] args) throws Exception {

    try {
      LOG.debug("Running API monitor. Input parameters: {}", Arrays.asList(args));

      assertEquals("Should have 4 parameters: API base URL, client ID, secret, personId to test", 4, args.length);
      String apiBaseUrl = args[0];
      String clientId = args[1];
      String secret = args[2];
      String personId = args[3];


      OpenConextOAuthClientImpl client = new OpenConextOAuthClientImpl();

      client.setConsumerKey(clientId);
      client.setConsumerSecret(secret);
      client.setEndpointBaseUrl(apiBaseUrl);

      // OnBehalfOf is null: this yields a client-credentials-call to API.

      // Existing user.
      Person person = client.getPerson(personId, null);
      assertNotNull(person);
      assertEquals("Geert van der Ploeg", person.getDisplayName());

      // Non-existent user
      try {
        client.getPerson(nonExistingPersonId, null);
        fail("Person-call with non-existent user should throw exception");
      } catch (RuntimeException e) {
        assertTrue("Response of person-call with non-existent user should be a 404", e.getMessage().contains("Error response: 404"));
      }

      // Groups of existing user
      List<Group> groups = client.getGroups(personId, null);
      assertNotNull("Groups-call for person " + personId + " should not be null", groups);

      // Groups of non-existing user
      try {
        client.getGroups(nonExistingPersonId, null);
        fail("Groups-call with non-existent user should throw exception");
      } catch (RuntimeException e) {
        assertTrue("Response of groups-call with non-existent user should be a 404", e.getMessage().contains("Error response: 404"));
      }
    } catch (Throwable e) {
      LOG.error("Exception while running, will exit(2)", e);
      System.exit(2);
    }
  }
}
