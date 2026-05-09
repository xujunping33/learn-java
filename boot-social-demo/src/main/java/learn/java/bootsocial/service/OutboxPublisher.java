package learn.java.bootsocial.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.mapper.OutboxEventMapper;
import learn.java.bootsocial.model.OutboxEvent;
import learn.java.bootsocial.mq.DedupKeys;
import learn.java.bootsocial.mq.PostEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "docker"})
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventMapper outboxEventMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public OutboxPublisher(
            OutboxEventMapper outboxEventMapper,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            AppProperties appProperties) {
        this.outboxEventMapper = outboxEventMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void publishPending() {
        if (!appProperties.getOutbox().isPublisherEnabled()) {
            return;
        }
        int batchSize = appProperties.getOutbox().getBatchSize();
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEvent> batch = outboxEventMapper.listPending(now, batchSize);
        for (OutboxEvent evt : batch) {
            publishOne(evt);
        }
    }

    private void publishOne(OutboxEvent evt) {
        if (evt.getId() == null) {
            return;
        }
        long id = evt.getId();
        int locked = outboxEventMapper.markSending(id);
        if (locked <= 0) {
            return;
        }
        try {
            Object payload = deserializePayload(evt.getPayloadType(), evt.getPayloadJson());
            rabbitTemplate.convertAndSend(evt.getExchangeName(), evt.getRoutingKey(), payload);
            outboxEventMapper.markSent(id, LocalDateTime.now());
            if (payload instanceof PostEvents.CommentCreated c) {
                log.info(
                        "notify_chain_publish MQ_SENT eventId={} postId={} commentId={} actorId={} ownerId={} dedupKey={} outboxId={} rk={}",
                        c.eventId(),
                        c.postId(),
                        c.commentId(),
                        c.actorId(),
                        c.ownerId(),
                        DedupKeys.commentCreated(c.commentId()),
                        id,
                        evt.getRoutingKey());
            } else if (payload instanceof PostEvents.PostLiked l) {
                log.info(
                        "notify_chain_publish MQ_SENT eventId={} postId={} actorId={} ownerId={} dedupKey={} outboxId={} rk={}",
                        l.eventId(),
                        l.postId(),
                        l.actorId(),
                        l.ownerId(),
                        DedupKeys.postLiked(l.postId(), l.actorId()),
                        id,
                        evt.getRoutingKey());
            } else {
                log.info("outbox_sent id={} rk={} type={}", id, evt.getRoutingKey(), evt.getPayloadType());
            }
        } catch (Exception ex) {
            int retryCount = evt.getRetryCount() == null ? 0 : evt.getRetryCount();
            int nextRetryCount = retryCount + 1;
            LocalDateTime nextRetryAt = LocalDateTime.now().plus(retryDelay(nextRetryCount));
            String err = truncate(ex.getClass().getSimpleName() + ": " + safeMessage(ex), 512);
            outboxEventMapper.reschedule(id, nextRetryCount, nextRetryAt, err);
            log.warn("outbox_send_failed id={} rk={} retryCount={} nextRetryAt={}", id, evt.getRoutingKey(), nextRetryCount, nextRetryAt, ex);
        }
    }

    private Object deserializePayload(String payloadType, String payloadJson) throws Exception {
        if (payloadType == null || payloadType.isBlank()) {
            return payloadJson;
        }
        Class<?> clazz = Class.forName(payloadType);
        return objectMapper.readValue(payloadJson, clazz);
    }

    private static Duration retryDelay(int retryCount) {
        return switch (retryCount) {
            case 1 -> Duration.ofSeconds(5);
            case 2 -> Duration.ofSeconds(30);
            case 3 -> Duration.ofMinutes(2);
            case 4 -> Duration.ofMinutes(10);
            default -> Duration.ofHours(1);
        };
    }

    private static String safeMessage(Throwable ex) {
        String msg = ex.getMessage();
        return msg == null ? "" : msg;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen);
    }
}

