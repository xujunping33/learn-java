package learn.java.dualsystem.order.mapper;

import learn.java.dualsystem.order.model.ProcessedPaymentEvent;

public interface ProcessedPaymentEventMapper {

    int insertIgnore(ProcessedPaymentEvent row);
}
