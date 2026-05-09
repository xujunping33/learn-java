package learn.java.bootsocial.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import learn.java.bootsocial.mapper.OutboxEventMapper;
import learn.java.bootsocial.model.OutboxEvent;
import learn.java.bootsocial.mq.DedupKeys;
import learn.java.bootsocial.mq.PostEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "docker"})
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventMapper outboxEventMapper, ObjectMapper objectMapper) {
        this.outboxEventMapper = outboxEventMapper;
        this.objectMapper = objectMapper;
    }

    public void enqueue(String exchangeName, String routingKey, Object payload) {
        if (payload == null) {
            return;
        }
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("outbox serialize failed", e);
        }
        OutboxEvent evt = new OutboxEvent();
        evt.setExchangeName(exchangeName);
        evt.setRoutingKey(routingKey);
        evt.setPayloadType(payload.getClass().getName());
        evt.setPayloadJson(payloadJson);
        evt.setStatus("PENDING");
        evt.setRetryCount(0);
        evt.setNextRetryAt(LocalDateTime.now());
        evt.setLastError(null);
        int rows = outboxEventMapper.insert(evt);
        if (payload instanceof PostEvents.CommentCreated c) {
            log.info(
                    "notify_chain_outbox ENQUEUE eventId={} postId={} commentId={} actorId={} ownerId={} dedupKey={} outboxId={} rk={}",
                    c.eventId(),
                    c.postId(),
                    c.commentId(),
                    c.actorId(),
                    c.ownerId(),
                    DedupKeys.commentCreated(c.commentId()),
                    evt.getId(),
                    routingKey);
        } else if (payload instanceof PostEvents.PostLiked l) {
            log.info(
                    "notify_chain_outbox ENQUEUE eventId={} postId={} actorId={} ownerId={} dedupKey={} outboxId={} rk={}",
                    l.eventId(),
                    l.postId(),
                    l.actorId(),
                    l.ownerId(),
                    DedupKeys.postLiked(l.postId(), l.actorId()),
                    evt.getId(),
                    routingKey);
        } else {
            log.info(
                    "outbox_enqueued rows={} id={} exchange={} rk={} payloadType={}",
                    rows,
                    evt.getId(),
                    exchangeName,
                    routingKey,
                    evt.getPayloadType());
        }
    }
}

