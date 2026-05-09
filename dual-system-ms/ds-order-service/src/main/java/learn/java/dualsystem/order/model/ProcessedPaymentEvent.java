package learn.java.dualsystem.order.model;

public class ProcessedPaymentEvent {
    private Long id;
    private String eventId;
    private Long paymentId;
    private Long orderId;

    public ProcessedPaymentEvent() {}

    public ProcessedPaymentEvent(Long id, String eventId, Long paymentId, Long orderId) {
        this.id = id;
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
