package monitoring.voot;

import monitoring.Monitor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractVootMonitor implements Monitor {

    private static final String nonExistingPersonId = "urn:collab:person:some-nonexisting-org:monitoring-user";

    private final String authorizationURL;
    private final String vootBaseUrl;
    private final String clientId;
    private final String secret;
    private final String personId;

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
    public void monitor() throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        //pre-populate to enforce caching and allow for retry with the already obtained accessToken
        String accessToken = fetchAccessToken(restTemplate);
        Thread.sleep(2500);
        doMonitor(true, 1, restTemplate, accessToken);
    }

    private String fetchAccessToken(RestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, secret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "openid groups");

        ResponseEntity<Map> response = restTemplate.postForEntity(authorizationURL, new HttpEntity<>(body, headers), Map.class);
        return (String) response.getBody().get("access_token");
    }

    private void doMonitor(boolean retry, int count, RestTemplate restTemplate, String accessToken) {
        String url = vootBaseUrl + "/internal/groups/{userId}";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            List groups = restTemplate.exchange(url, HttpMethod.GET, entity, List.class, personId).getBody();
            assertFalse(personId + " must have group memberships", groups.isEmpty());

            groups = restTemplate.exchange(url, HttpMethod.GET, entity, List.class, nonExistingPersonId).getBody();
            assertTrue(nonExistingPersonId + " must not have memberships", groups.isEmpty());
        } catch (RuntimeException e) {
            if (retry) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
                this.doMonitor(count < 5, count + 1, restTemplate, accessToken);
            } else {
                throw e;
            }

        }
    }
}
