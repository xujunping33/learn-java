package learn.java.dualsystem.payment;

import learn.java.dualsystem.payment.outbox.PaymentOutboxProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("learn.java.dualsystem.payment.mapper")
@EnableScheduling
@EnableConfigurationProperties(PaymentOutboxProperties.class)
public class DsPaymentServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(DsPaymentServiceApp.class, args);
    }
}

