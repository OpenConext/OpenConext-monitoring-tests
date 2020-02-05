package monitoring.oidcng;

import monitoring.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@Component
public class OidcNGMonitor implements Monitor {

    private String resourceServerId;
    private String resourceServerSecret;
    private String baseURL;
    private String clientId;
    private String secret;

    public OidcNGMonitor(@Value("${oidcng.base_url}") String baseURL,
                         @Value("${oidcng.client_id}") String clientId,
                         @Value("${oidcng.secret}") String secret,
                         @Value("${oidcng.resource_server_id}") String resourceServerId,
                         @Value("${oidcng.resource_server_secret}") String resourceServerSecret) {
        this.baseURL = baseURL;
        this.clientId = clientId;
        this.secret = secret;
        this.resourceServerId = resourceServerId;
        this.resourceServerSecret = resourceServerSecret;
    }

    @Override
    public void monitor() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = new TestRestTemplate(clientId, secret).postForEntity(baseURL + "/token", request, Map.class);
        String accessToken = (String) response.getBody().get("access_token");

        request = new HttpEntity<>(headers);
        response = new TestRestTemplate(resourceServerId, resourceServerSecret).postForEntity(baseURL + "/introspect?token=" + accessToken, request, Map.class);

        assertEquals(true, response.getBody().get("active"));
    }
}
