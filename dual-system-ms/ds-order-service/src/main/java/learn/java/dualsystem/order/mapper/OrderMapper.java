package learn.java.dualsystem.order.mapper;

import learn.java.dualsystem.order.model.Order;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {

    int insert(Order order);

    Order selectById(@Param("id") long id);

    int markPaid(@Param("orderId") long orderId, @Param("paymentId") long paymentId);
}
