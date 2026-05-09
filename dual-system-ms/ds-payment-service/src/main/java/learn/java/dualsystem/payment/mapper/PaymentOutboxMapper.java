package learn.java.dualsystem.payment.mapper;

import java.util.List;
import learn.java.dualsystem.payment.model.PaymentOutbox;
import org.apache.ibatis.annotations.Param;

public interface PaymentOutboxMapper {

    int insert(PaymentOutbox row);

    List<PaymentOutbox> selectPending(@Param("limit") int limit);

    int updateSentIfPending(@Param("id") long id);

    int incrementRetryAndMaybeFail(@Param("id") long id, @Param("maxRetries") int maxRetries);
}
