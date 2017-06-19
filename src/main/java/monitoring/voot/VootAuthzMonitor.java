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

@Component
public class VootAuthzMonitor extends AbstractVootMonitor {

    public VootAuthzMonitor(@Value("${voot.authz.authorization_url}") String authorizationURL,
                            @Value("${voot.authz.voot_base_url}") String vootBaseUrl,
                            @Value("${voot.authz.client_id}") String clientId,
                            @Value("${voot.authz.secret}") String secret,
                            @Value("${voot.authz.person_id}") String personId) {
        super(authorizationURL, vootBaseUrl, clientId, secret, personId);
    }

}
