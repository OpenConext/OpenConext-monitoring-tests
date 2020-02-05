package monitoring.voot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
