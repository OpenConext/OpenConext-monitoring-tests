package monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public interface Monitor extends HealthIndicator {

    void monitor() throws Exception;

    default Health health() {
        try {
            monitor();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
