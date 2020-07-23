package info.matsumana.psystrike.config;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

import info.matsumana.psystrike.service.ReverseProxyService;

@Configuration
public class ArmeriaServerConfig {

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator() {
        return builder -> builder.accessLogWriter(AccessLogWriter.combined(), false);
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(ReverseProxyService service) {
        return serverBuilder -> serverBuilder
                .annotatedService()
                .decorator(LoggingService.builder()
                                         .logger(LoggerFactory.getLogger(service.getClass()))
                                         .newDecorator())
                .build(service);
    }
}
