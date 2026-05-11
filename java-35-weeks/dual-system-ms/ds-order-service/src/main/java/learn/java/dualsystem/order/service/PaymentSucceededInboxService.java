package learn.java.dualsystem.order.service;

import learn.java.dualsystem.order.mapper.OrderMapper;
import learn.java.dualsystem.order.mapper.ProcessedPaymentEventMapper;
import learn.java.dualsystem.order.model.ProcessedPaymentEvent;
import learn.java.dualsystem.order.mq.PaymentSucceededPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSucceededInboxService {

    private final OrderMapper orderMapper;
    private final ProcessedPaymentEventMapper processedPaymentEventMapper;

    public PaymentSucceededInboxService(
            OrderMapper orderMapper, ProcessedPaymentEventMapper processedPaymentEventMapper) {
        this.orderMapper = orderMapper;
        this.processedPaymentEventMapper = processedPaymentEventMapper;
    }

    @Transactional
    public void handle(PaymentSucceededPayload event) {
        orderMapper.markPaid(event.orderId(), event.paymentId());
        var row = new ProcessedPaymentEvent();
        row.setEventId(event.eventId());
        row.setPaymentId(event.paymentId());
        row.setOrderId(event.orderId());
        processedPaymentEventMapper.insertIgnore(row);
    }
}
