package learn.java.dualsystem.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import learn.java.dualsystem.payment.mapper.PaymentMapper;
import learn.java.dualsystem.payment.mapper.PaymentOutboxMapper;
import learn.java.dualsystem.payment.model.Payment;
import learn.java.dualsystem.payment.model.PaymentOutbox;
import learn.java.dualsystem.payment.model.PaymentStatus;
import learn.java.dualsystem.payment.mq.PaymentSucceededMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceOutboxTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentOutboxMapper paymentOutboxMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void confirmInsertsOutboxWithPaymentSucceededJson() throws Exception {
        var init = new Payment();
        init.setId(5L);
        init.setOrderId(9L);
        init.setUserId(1L);
        init.setAmount(new BigDecimal("12.34"));
        init.setStatus(PaymentStatus.INIT);

        var afterPay = new Payment();
        afterPay.setId(5L);
        afterPay.setOrderId(9L);
        afterPay.setUserId(1L);
        afterPay.setAmount(new BigDecimal("12.34"));
        afterPay.setStatus(PaymentStatus.SUCCESS);

        when(paymentMapper.selectById(5L)).thenReturn(init, afterPay);
        when(paymentMapper.markSuccessIfInit(eq(5L), any(LocalDateTime.class))).thenReturn(1);

        var svc = new PaymentService(paymentMapper, paymentOutboxMapper, objectMapper);
        PaymentService.ConfirmResult r = svc.confirm(5L);
        assertThat(r).isEqualTo(PaymentService.ConfirmResult.CONFIRMED);

        ArgumentCaptor<PaymentOutbox> cap = ArgumentCaptor.forClass(PaymentOutbox.class);
        verify(paymentOutboxMapper).insert(cap.capture());
        PaymentOutbox row = cap.getValue();
        assertThat(row.getPaymentId()).isEqualTo(5L);
        assertThat(row.getStatus()).isEqualTo(PaymentOutbox.STATUS_PENDING);
        var msg = objectMapper.readValue(row.getPayloadJson(), PaymentSucceededMessage.class);
        assertThat(msg.paymentId()).isEqualTo(5L);
        assertThat(msg.orderId()).isEqualTo(9L);
        assertThat(msg.schemaVersion()).isEqualTo(1);
    }
}
