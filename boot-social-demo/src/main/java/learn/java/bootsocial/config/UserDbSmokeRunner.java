package learn.java.bootsocial.config;

import learn.java.bootsocial.model.User;
import learn.java.bootsocial.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class UserDbSmokeRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDbSmokeRunner.class);

    @Bean
    @ConditionalOnProperty(name = "app.db-smoke", havingValue = "true")
    public CommandLineRunner dbSmoke(UserService userService) {
        return args -> {
            String username = "boot_u" + System.currentTimeMillis();
            User u = userService.register(username, "bootdemo123");
            log.info("db smoke ok: id={} username={}", u.getId(), u.getUsername());
        };
    }
}

