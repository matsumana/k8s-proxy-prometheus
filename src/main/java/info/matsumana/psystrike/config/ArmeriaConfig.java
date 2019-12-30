package info.matsumana.psystrike.config;

import static com.linecorp.armeria.common.logging.LogLevel.DEBUG;
import static com.linecorp.armeria.common.logging.LogLevel.WARN;
import static info.matsumana.psystrike.service.ReverseProxyService.CLIENT_IDLE_TIMEOUT_MINUTES;

import java.time.Duration;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.common.metric.PrometheusMeterRegistries;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

import info.matsumana.psystrike.service.ReverseProxyService;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@Configuration
public class ArmeriaConfig {

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        // Use BetterPrometheusNamingConvention
        return PrometheusMeterRegistries.newRegistry();
    }

    @Bean
    public ClientFactory clientFactory(PrometheusMeterRegistry registry) {
        return ClientFactory.builder()
                            .meterRegistry(registry)
                            .idleTimeout(Duration.ofMinutes(CLIENT_IDLE_TIMEOUT_MINUTES))
                            .build();
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return builder -> builder.accessLogWriter(AccessLogWriter.combined(), false);
    }

    @Bean
    public AnnotatedServiceRegistrationBean reverseProxyServiceRegistrationBean(ReverseProxyService service) {
        return new AnnotatedServiceRegistrationBean()
                .setServiceName(service.getClass().getSimpleName())
                .setService(service)
                .setDecorators(LoggingService.builder()
                                             .logger(LoggerFactory.getLogger(service.getClass()))
                                             .requestLogLevel(DEBUG)
                                             .successfulResponseLogLevel(DEBUG)
                                             .failureResponseLogLevel(WARN)
                                             .newDecorator());
    }
}
