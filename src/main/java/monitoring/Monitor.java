package monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public interface Monitor extends HealthIndicator {

    Logger LOG = LoggerFactory.getLogger(Monitor.class);

    void monitor() throws Exception;

    default Health health() {
        try {
            monitor();
            return Health.up().build();
        } catch (Throwable e) {
            LOG.error("Exception in health " + getClass(), e);
            return Health.down().withDetail(
                    String.format("Error in monitor %s", getClass()),
                    e.getMessage()
            ).build();
        }
    }
}
