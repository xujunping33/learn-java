package learn.java.bootsocial.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import learn.java.bootsocial.observability.NotifyConsumeMetrics;
import learn.java.bootsocial.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 通知队列：单一监听入口，按 routingKey（或 JSON 是否含 {@code commentPreview}）反序列化。
 * <p>
 * 使用原始 {@link Message} 字节再 {@link ObjectMapper#readTree}：{@code @Payload JsonNode} 会与 Jackson 转换器的
 * TypeId/推断类型冲突（{@code JsonNode} 为抽象类型），易导致监听从不成功调用业务逻辑。
 * <p>
 * 亦避免同一队列多个监听方法依赖 Jackson {@code __TypeId__}；DLQ replay 后类型头常丢失会导致重放不落库。
 */
@Component
@Profile({"dev", "docker"})
public class MqConsumers {

    private static final Logger log = LoggerFactory.getLogger(MqConsumers.class);

    private final NotificationService notificationService;
    private final MqFailureSwitch mqFailureSwitch;
    private final NotifyConsumeMetrics notifyConsumeMetrics;
    private final ObjectMapper objectMapper;

    public MqConsumers(
            NotificationService notificationService,
            MqFailureSwitch mqFailureSwitch,
            NotifyConsumeMetrics notifyConsumeMetrics,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.mqFailureSwitch = mqFailureSwitch;
        this.notifyConsumeMetrics = notifyConsumeMetrics;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = MqConfiguration.TEST_QUEUE)
    public void onTestMessage(MqTestMessage msg) {
        log.info("mq_test_consume id={} occurredAt={} text={}", msg.id(), msg.occurredAt(), msg.text());
    }

    @RabbitListener(queues = MqConfiguration.NOTIFY_QUEUE)
    public void onNotifyEvent(
            Message amqpMessage,
            @Header(value = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {
        try {
            byte[] raw = amqpMessage.getBody();
            if (raw == null || raw.length == 0) {
                log.warn("notify_empty_body routingKey={}", routingKey);
                return;
            }
            JsonNode root = objectMapper.readTree(raw);
            String rk = routingKey == null ? "" : routingKey.trim();
            if (rk.isEmpty()) {
                rk = root.hasNonNull("commentPreview") ? MqConfiguration.RK_COMMENT_CREATED : MqConfiguration.RK_POST_LIKED;
            }
            switch (rk) {
                case MqConfiguration.RK_COMMENT_CREATED ->
                        handleCommentCreated(objectMapper.treeToValue(root, PostEvents.CommentCreated.class));
                case MqConfiguration.RK_POST_LIKED ->
                        handlePostLiked(objectMapper.treeToValue(root, PostEvents.PostLiked.class));
                default -> log.warn("notify_unknown routingKey={}", rk);
            }
        } catch (IOException ex) {
            notifyConsumeMetrics.recordFailure();
            log.warn("notify_deserialize_failed routingKey={}", routingKey, ex);
            throw new IllegalStateException("notify deserialize failed", ex);
        }
    }

    private void handleCommentCreated(PostEvents.CommentCreated evt) {
        if (evt.ownerId() == evt.actorId()) {
            return;
        }
        String dedupKey = DedupKeys.commentCreated(evt.commentId());
        try {
            if (mqFailureSwitch.isEnabled()
                    && evt.commentPreview() != null
                    && evt.commentPreview().contains("FAIL_CONSUME")) {
                throw new IllegalStateException("simulated consumer failure (CommentCreated)");
            }
            boolean inserted =
                    notificationService.insertIgnore(
                            evt.ownerId(), "COMMENT_CREATED", evt.commentId(), dedupKey, evt);
            if (!inserted) {
                log.info(
                        "notify_duplicate CommentCreated dedupKey={} eventId={} postId={} commentId={} actorId={}",
                        dedupKey,
                        evt.eventId(),
                        evt.postId(),
                        evt.commentId(),
                        evt.actorId());
            }
            log.info(
                    "notify_chain_consume CommentCreated OK eventId={} dedupKey={} postId={} commentId={} actorId={} ownerId={} inserted={}",
                    evt.eventId(),
                    dedupKey,
                    evt.postId(),
                    evt.commentId(),
                    evt.actorId(),
                    evt.ownerId(),
                    inserted);
            notifyConsumeMetrics.recordSuccess();
        } catch (RuntimeException ex) {
            notifyConsumeMetrics.recordFailure();
            log.warn(
                    "notify_chain_consume CommentCreated FAIL eventId={} dedupKey={} postId={} commentId={} actorId={} ownerId={}",
                    evt.eventId(),
                    dedupKey,
                    evt.postId(),
                    evt.commentId(),
                    evt.actorId(),
                    evt.ownerId(),
                    ex);
            throw ex;
        }
    }

    private void handlePostLiked(PostEvents.PostLiked evt) {
        if (evt.ownerId() == evt.actorId()) {
            return;
        }
        String dedupKey = DedupKeys.postLiked(evt.postId(), evt.actorId());
        try {
            if (mqFailureSwitch.isEnabled() && evt.postId() == 999_999L) {
                throw new IllegalStateException("simulated consumer failure (PostLiked)");
            }
            boolean inserted =
                    notificationService.insertIgnore(evt.ownerId(), "POST_LIKED", evt.postId(), dedupKey, evt);
            if (!inserted) {
                log.info(
                        "notify_duplicate PostLiked dedupKey={} eventId={} postId={} actorId={}",
                        dedupKey,
                        evt.eventId(),
                        evt.postId(),
                        evt.actorId());
            }
            log.info(
                    "notify_chain_consume PostLiked OK eventId={} dedupKey={} postId={} actorId={} ownerId={} inserted={}",
                    evt.eventId(),
                    dedupKey,
                    evt.postId(),
                    evt.actorId(),
                    evt.ownerId(),
                    inserted);
            notifyConsumeMetrics.recordSuccess();
        } catch (RuntimeException ex) {
            notifyConsumeMetrics.recordFailure();
            log.warn(
                    "notify_chain_consume PostLiked FAIL eventId={} dedupKey={} postId={} actorId={} ownerId={}",
                    evt.eventId(),
                    dedupKey,
                    evt.postId(),
                    evt.actorId(),
                    evt.ownerId(),
                    ex);
            throw ex;
        }
    }
}
