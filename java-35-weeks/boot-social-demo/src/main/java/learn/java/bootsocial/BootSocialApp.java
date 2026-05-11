package learn.java.bootsocial;

import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, MinioProperties.class})
@EnableScheduling
public class BootSocialApp {
    public static void main(String[] args) {
        SpringApplication.run(BootSocialApp.class, args);
    }
}

