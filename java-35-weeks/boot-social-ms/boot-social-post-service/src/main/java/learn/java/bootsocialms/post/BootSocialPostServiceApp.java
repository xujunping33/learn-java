package learn.java.bootsocialms.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BootSocialPostServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(BootSocialPostServiceApp.class, args);
    }
}

