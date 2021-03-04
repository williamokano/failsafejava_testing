package dev.okano.failsafejava;

import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class FailsafeConfiguration {

    @Bean
    public RetryPolicy<XptoResponse> retryPolicy() {
        return new RetryPolicy<XptoResponse>()
                .handle(RuntimeException.class)
                .handleIf((response, throwable) -> response.getStatusCode() == 500)
                .withDelay(Duration.ofMillis(200));
    }

    @Bean
    public Timeout<XptoResponse> timeoutPolicy() {
        Timeout<XptoResponse> to = Timeout.of(Duration.ofSeconds(3));
        to.withCancel(true);
        return to;
    }

}
