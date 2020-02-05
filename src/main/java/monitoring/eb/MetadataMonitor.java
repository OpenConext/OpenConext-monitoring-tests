package monitoring.eb;

import monitoring.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Component
public class MetadataMonitor implements Monitor {

    private RestTemplate restTemplate;
    private String engineBlockIdpMetatadaUrl;
    private String engineBlockSpMetatadaUrl;
    private Pattern pattern = Pattern.compile("validUntil=\"(.*?)\"");
    private DateTimeFormatter dateTimeFormatter = ISO_INSTANT.withZone(systemDefault());

    public MetadataMonitor(@Value("${eb.metadata_sp_url}") String engineBlockIdpMetatadaUrl,
                           @Value("${eb.metadata_idp_url}") String engineBlockSpMetatadaUrl) {
        this.engineBlockIdpMetatadaUrl = engineBlockIdpMetatadaUrl;
        this.engineBlockSpMetatadaUrl = engineBlockSpMetatadaUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void monitor() throws Exception {
        metadata(restTemplate, this.engineBlockIdpMetatadaUrl);
        metadata(restTemplate, this.engineBlockSpMetatadaUrl);
    }

    private void metadata(RestTemplate restTemplate, String url) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        assertEquals("EngineBlock IDP metadata", HttpStatus.OK, responseEntity.getStatusCode());

        ZonedDateTime halfDayFromNow = ZonedDateTime.now(systemDefault()).plusHours(12);
        assertTrue("validUntil of the metadata should be at least 12 hrs in future",
                validUntil(responseEntity.getBody()).isAfter(halfDayFromNow));
    }

    private ZonedDateTime validUntil(String metaData) {
        Matcher matcher = pattern.matcher(metaData);
        matcher.find();

        String validUntil = matcher.group(1);
        return ZonedDateTime.parse(validUntil, dateTimeFormatter);
    }

}
