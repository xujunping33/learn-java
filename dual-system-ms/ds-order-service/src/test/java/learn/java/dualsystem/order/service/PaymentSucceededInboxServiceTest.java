package learn.java.dualsystem.order.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import learn.java.dualsystem.order.mapper.OrderMapper;
import learn.java.dualsystem.order.mapper.ProcessedPaymentEventMapper;
import learn.java.dualsystem.order.mq.PaymentSucceededPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentSucceededInboxServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProcessedPaymentEventMapper processedPaymentEventMapper;

    @InjectMocks
    private PaymentSucceededInboxService inboxService;

    @Test
    void duplicateDeliveryStillCallsMarkPaidAndIdempotentInsert() {
        var event = new PaymentSucceededPayload("evt-1", "evt-1", 9, 3, 1, "10.00", "2026-01-01T00:00:00Z", 1);

        when(orderMapper.markPaid(3, 9)).thenReturn(1);
        when(processedPaymentEventMapper.insertIgnore(any())).thenReturn(1);

        inboxService.handle(event);

        when(orderMapper.markPaid(3, 9)).thenReturn(0);
        when(processedPaymentEventMapper.insertIgnore(any())).thenReturn(0);

        inboxService.handle(event);

        verify(orderMapper, times(2)).markPaid(3, 9);
        verify(processedPaymentEventMapper, times(2)).insertIgnore(any());
    }
}
