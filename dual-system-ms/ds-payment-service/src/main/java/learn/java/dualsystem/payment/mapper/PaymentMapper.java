package learn.java.dualsystem.payment.mapper;

import learn.java.dualsystem.payment.model.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper {

    int insert(Payment payment);

    Payment selectById(@Param("id") long id);

    int markSuccessIfInit(@Param("id") long id, @Param("paidAt") java.time.LocalDateTime paidAt);

    int markEventPublished(@Param("id") long id, @Param("at") java.time.LocalDateTime at);
}
