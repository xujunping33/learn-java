package learn.java.bootsocial.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import learn.java.bootsocial.mapper.NotificationMapper;
import learn.java.bootsocial.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "docker"})
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationMapper notificationMapper, ObjectMapper objectMapper) {
        this.notificationMapper = notificationMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 幂等插入通知：依赖 {@code notifications.dedup_key UNIQUE}；{@code dedupKey} 规则见 {@link
     * learn.java.bootsocial.mq.DedupKeys}。
     */
    public boolean insertIgnore(long userId, String type, long refId, String dedupKey, Object payloadObj) {
        String payload = null;
        if (payloadObj != null) {
            try {
                payload = objectMapper.writeValueAsString(payloadObj);
            } catch (JsonProcessingException ex) {
                log.warn("notification payload json failed, store null. type={} refId={} dedupKey={}", type, refId, dedupKey, ex);
            }
        }

        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setRefId(refId);
        n.setPayload(payload);
        n.setDedupKey(dedupKey);
        int rows = notificationMapper.insertIgnore(n);
        return rows > 0;
    }
}

