package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.SequentialClockSignalDispatcher;
import root.tse.infrastructure.clock.ClockSignalPropagator;

import java.time.Clock;

@Configuration
public class ClockConfiguration {

    @Bean
    public ClockSignalPropagator clockSignalPropagator(Clock clock, ClockSignalDispatcher clockSignalDispatcher) {
        return new ClockSignalPropagator(clock, clockSignalDispatcher);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ClockSignalDispatcher clockSignalDispatcher() {
        return new SequentialClockSignalDispatcher();
    }
}
