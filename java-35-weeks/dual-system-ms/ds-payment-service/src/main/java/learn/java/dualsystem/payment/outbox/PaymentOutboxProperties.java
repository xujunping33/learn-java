package learn.java.dualsystem.payment.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.outbox")
public class PaymentOutboxProperties {

    /** Fixed delay between dispatch ticks (ms). */
    private long dispatchIntervalMs = 250;

    private int batchSize = 50;

    /** After this many failed publish attempts, row becomes FAILED (manual replay / ops). */
    private int maxRetries = 120;

    public long getDispatchIntervalMs() {
        return dispatchIntervalMs;
    }

    public void setDispatchIntervalMs(long dispatchIntervalMs) {
        this.dispatchIntervalMs = dispatchIntervalMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}
