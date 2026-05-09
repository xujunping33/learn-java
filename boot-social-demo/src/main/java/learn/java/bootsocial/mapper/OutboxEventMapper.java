package learn.java.bootsocial.mapper;

import java.time.LocalDateTime;
import java.util.List;

import learn.java.bootsocial.model.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboxEventMapper {
    int insert(OutboxEvent event);

    List<OutboxEvent> listPending(@Param("now") LocalDateTime now, @Param("limit") int limit);

    int markSending(@Param("id") long id);

    int markSent(@Param("id") long id, @Param("sentAt") LocalDateTime sentAt);

    int reschedule(
            @Param("id") long id,
            @Param("retryCount") int retryCount,
            @Param("nextRetryAt") LocalDateTime nextRetryAt,
            @Param("lastError") String lastError);
}

