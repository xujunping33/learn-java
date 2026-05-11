package learn.java.dualsystem.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("learn.java.dualsystem.order.mapper")
@EnableRabbit
public class DsOrderServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(DsOrderServiceApp.class, args);
    }
}

