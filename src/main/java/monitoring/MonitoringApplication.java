package monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@SpringBootApplication(exclude = {
        TraceWebFilterAutoConfiguration.class,
        MetricFilterAutoConfiguration.class
})
public class MonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringApplication.class, args);
    }


    @Bean
    public HealthMvcEndpoint exposeDetailsHealthMvcEndpoint(HealthEndpoint delegate) {
        return new HealthMvcEndpoint(delegate) {

            @Override
            protected boolean exposeHealthDetails(HttpServletRequest request, Principal principal) {
                return true;
            }

        };
    }
}
