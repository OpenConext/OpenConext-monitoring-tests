package monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public interface Monitor extends HealthIndicator {

    Logger LOG = LoggerFactory.getLogger(Monitor.class);

    void monitor() throws Exception;

    default Health health() {
        try {
            monitor();
            return Health.up().build();
        } catch (Exception e) {
            LOG.error("Exception in health " + getClass(), e);
            return Health.down().withException(e).build();
        }
    }
}
