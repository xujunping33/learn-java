package learn.java.dualsystem.order.service;

import java.math.BigDecimal;
import java.util.Optional;
import learn.java.dualsystem.order.mapper.OrderMapper;
import learn.java.dualsystem.order.model.Order;
import learn.java.dualsystem.order.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Transactional
    public Order create(long userId, BigDecimal amount) {
        var order = new Order();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus(OrderStatus.CREATED);
        orderMapper.insert(order);
        return orderMapper.selectById(order.getId());
    }

    public Optional<Order> getById(long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    @Transactional
    public boolean markPaid(long orderId, long paymentId) {
        return orderMapper.markPaid(orderId, paymentId) > 0;
    }
}
