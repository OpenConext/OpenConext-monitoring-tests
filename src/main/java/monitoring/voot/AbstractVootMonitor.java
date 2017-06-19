package monitoring.voot;

import monitoring.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractVootMonitor implements Monitor {

    private static final String nonExistingPersonId = "urn:collab:person:some-nonexisting-org:monitoring-user";

    private String authorizationURL;
    private String vootBaseUrl;
    private String clientId;
    private String secret;
    private String personId;

    protected AbstractVootMonitor(String authorizationURL,
                               String vootBaseUrl,
                               String clientId,
                               String secret,
                               String personId) {
        this.authorizationURL = authorizationURL;
        this.vootBaseUrl = vootBaseUrl;
        this.clientId = clientId;
        this.secret = secret;
        this.personId = personId;
    }

    @Override
    public void monitor() throws Exception {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setAccessTokenUri(authorizationURL);
        details.setClientId(clientId);
        details.setClientSecret(secret);
        details.setScope(Collections.singletonList("groups"));

        OAuth2RestTemplate template = new OAuth2RestTemplate(details);

        String url = vootBaseUrl + "/internal/groups/{userId}";

        List groups = template.getForObject(url, List.class, personId);
        assertFalse(personId + " must have group memberships", groups.isEmpty());

        groups = template.getForObject(url, List.class, nonExistingPersonId);
        assertTrue(nonExistingPersonId + " must not have memberships", groups.isEmpty());
    }
}
