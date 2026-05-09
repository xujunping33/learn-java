package learn.java.dualsystem.payment.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dual.rabbit.payment-succeeded")
public class PaymentMqProperties {

    /** Topic exchange for payment domain events */
    private String exchange = "dual.payment.topic";

    /** Routing key for successful payment */
    private String routingKey = "payment.succeeded";

    /** Queue bound in payment-service for admin visibility (order-service will consume same RK in Day234) */
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
