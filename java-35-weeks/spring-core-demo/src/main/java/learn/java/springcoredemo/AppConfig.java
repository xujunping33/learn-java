package learn.java.springcoredemo;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import learn.java.springcoredemo.config.DataSourceConfig;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Import(DataSourceConfig.class)
@ComponentScan({
        "learn.java.springcoredemo.service",
        "learn.java.springcoredemo.repository",
        "learn.java.springcoredemo.aop",
        "learn.java.springcoredemo.tx",
        "learn.java.springcoredemo.audit",
        "learn.java.springcoredemo.batch",
})
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public UuidGenerator uuidGenerator(Clock clock) {
        return new UuidGenerator(clock);
    }
}
