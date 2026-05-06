package learn.java.bootsocial.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile({"dev", "docker"})
public class RedisSmokeRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisSmokeRunner.class);

    @Bean
    @ConditionalOnProperty(name = "app.redis-smoke", havingValue = "true")
    public CommandLineRunner redisSmoke(StringRedisTemplate rt) {
        return args -> {
            String key = "w27:redis:smoke";
            String value = "ok_" + System.currentTimeMillis();
            rt.opsForValue().set(key, value, Duration.ofSeconds(30));
            String got = rt.opsForValue().get(key);
            log.info("redis smoke ok: key={} value={}", key, got);
        };
    }
}

