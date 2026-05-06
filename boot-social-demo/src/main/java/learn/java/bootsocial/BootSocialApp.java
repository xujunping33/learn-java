package learn.java.bootsocial;

import learn.java.bootsocial.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class BootSocialApp {
    public static void main(String[] args) {
        SpringApplication.run(BootSocialApp.class, args);
    }
}

