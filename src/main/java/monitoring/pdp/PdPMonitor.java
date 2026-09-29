package monitoring.pdp;

import monitoring.Monitor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Component
public class PdPMonitor implements Monitor {

    private String pdpUserName;
    private String pdpPassword;
    private String idpEntityId;
    private String spEntityId;
    private String pdpBaseUrl;
    private final RestTemplate restTemplate;

    public PdPMonitor(@Value("${pdp.user}") String pdpUserName,
                      @Value("${pdp.password}") String pdpPassword,
                      @Value("${pdp.idp_entity_id}") String idpEntityId,
                      @Value("${pdp.sp_entity_id}") String spEntityId,
                      @Value("${pdp.base_url}") String pdpBaseUrl) {
        this.pdpUserName = pdpUserName;
        this.pdpPassword = pdpPassword;
        this.idpEntityId = idpEntityId;
        this.spEntityId = spEntityId;
        this.pdpBaseUrl = pdpBaseUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(pdpUserName, pdpPassword));
    }

    @Override
    public void monitor() throws Exception {
        String body;
        try (InputStream in = new ClassPathResource("pdp/request.json").getInputStream()) {
            body = IOUtils.toString(in);
        }
        body = body.replaceAll("@@IDP_ENTITY_ID@@", idpEntityId);
        body = body.replaceAll("@@SP_ENTITY_ID@@", spEntityId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(pdpBaseUrl + "/pdp/api/decide/policy", HttpMethod.POST, httpEntity, String.class);

        assertEquals("The response status must be 200", 200, response.getStatusCode().value());
        assertTrue("The body must contain 'urn:oasis:names:tc:xacml:1.0:status:ok'", response.getBody().contains("urn:oasis:names:tc:xacml:1.0:status:ok"));
        assertTrue("The body must contain 'NotApplicable'", response.getBody().contains("NotApplicable"));
    }

}
