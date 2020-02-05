package monitoring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MonitoringApplicationTest {

    @Value("${security.user.name}")
    private String user;

    @Value("${security.user.password}")
    private String password;

    @LocalServerPort
    private int serverPort;

    @Test
    public void main() throws Exception {
        ResponseEntity<Map> responseEntity = new TestRestTemplate(user, password)
                .getForEntity("http://localhost:" + serverPort + "/health", Map.class);
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

}