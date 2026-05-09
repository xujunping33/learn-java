package learn.java.dualsystem.order.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dual.rabbit.payment-succeeded")
public class PaymentMqProperties {

    private String exchange = "dual.payment.topic";
    private String routingKey = "payment.succeeded";
    private String queue = "dual.order.payment-succeeded";

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
