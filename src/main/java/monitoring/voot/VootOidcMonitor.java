package monitoring.voot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VootOidcMonitor extends AbstractVootMonitor {

    public VootOidcMonitor(@Value("${voot.oidc.authorization_url}") String authorizationURL,
                           @Value("${voot.oidc.voot_base_url}") String vootBaseUrl,
                           @Value("${voot.oidc.client_id}") String clientId,
                           @Value("${voot.oidc.secret}") String secret,
                           @Value("${voot.oidc.person_id}") String personId) {
        super(authorizationURL, vootBaseUrl, clientId, secret, personId);
    }

}
