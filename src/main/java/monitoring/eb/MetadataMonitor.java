package monitoring.eb;

import monitoring.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Component
public class MetadataMonitor implements Monitor {

    private String engineBlockMetatadaBaseUrl;
    private Pattern pattern = Pattern.compile("validUntil=\"(.*?)\"");

    public MetadataMonitor(@Value("${eb.metadata_base_url}") String engineBlockMetatadaBaseUrl) {
        this.engineBlockMetatadaBaseUrl = engineBlockMetatadaBaseUrl;
    }

    @Override
    public void monitor() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        metadata(restTemplate, "/authentication/idp/metadata");
        metadata(restTemplate, "/authentication/sp/metadata");
    }

    private void metadata(RestTemplate restTemplate, String path) {
        String url = engineBlockMetatadaBaseUrl + path;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        assertEquals("EngineBlock IDP metadata", HttpStatus.OK, responseEntity.getStatusCode());

        ZonedDateTime halfDayFromNow = ZonedDateTime.now(ZoneId.systemDefault()).plusHours(12);
        assertTrue("validUntil of the metadata should be at least 12 hrs in future",
            validUntil(responseEntity.getBody()).isAfter(halfDayFromNow));
    }

    private ZonedDateTime validUntil(String metaData) {
        Matcher matcher = pattern.matcher(metaData);
        matcher.find();

        String validUntil = matcher.group(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
        return ZonedDateTime.parse(validUntil, dateTimeFormatter);
    }

}
