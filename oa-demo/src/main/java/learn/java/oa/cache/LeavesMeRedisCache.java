package learn.java.oa.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * Day140：{@code GET /api/leaves/me} 旁路缓存（cache-aside）；写后删申请人名下所有该接口缓存键。
 *
 * <p>未启用或 Redis 不可用时，所有方法安全降级为「无缓存」。
 */
public final class LeavesMeRedisCache {

    private static final Logger LOG = Logger.getLogger(LeavesMeRedisCache.class.getName());

    private static final String KEY_PREFIX = "oa:v1:leaves:me:";
    private static final Object LOCK = new Object();

    private static volatile boolean initDone;
    private static volatile JedisPooled pool;
    private static volatile int ttlSeconds = 45;

    private LeavesMeRedisCache() {}

    /** 是否已启用并成功连接（懒加载，只初始化一次）。 */
    public static boolean isActive() {
        ensureConfigured();
        return pool != null;
    }

    public static String cacheKey(long userId, String status, String leaveType) {
        return KEY_PREFIX
                + userId
                + ":"
                + (status == null ? "" : status)
                + ":"
                + (leaveType == null ? "" : leaveType);
    }

    /** 命中则返回与 {@link learn.java.oa.api.Jsons#writeOk} 等价的 JSON 字符串。 */
    public static String getJsonOrNull(String key) {
        if (!isActive()) {
            return null;
        }
        try {
            return pool.get(key);
        } catch (JedisException e) {
            LOG.log(Level.WARNING, "redis get leaves/me " + key, e);
            return null;
        }
    }

    public static void setJson(String key, String jsonBody) {
        if (!isActive()) {
            return;
        }
        try {
            pool.setex(key, Math.max(1, ttlSeconds), jsonBody);
        } catch (JedisException e) {
            LOG.log(Level.WARNING, "redis setex leaves/me " + key, e);
        }
    }

    /** 删除该用户下所有 {@code leaves/me} 缓存变体（不同 query 各一把键）。 */
    public static void invalidateUser(long applicantUserId) {
        if (!isActive()) {
            return;
        }
        String pattern = KEY_PREFIX + applicantUserId + ":*";
        try {
            String cursor = "0";
            ScanParams params = new ScanParams().match(pattern).count(128);
            int deleted = 0;
            do {
                ScanResult<String> scan = pool.scan(cursor, params);
                for (String k : scan.getResult()) {
                    pool.del(k);
                    deleted++;
                }
                String next = scan.getCursor();
                cursor = next == null ? "0" : next;
            } while (!"0".equals(cursor));
            if (deleted > 0) {
                int keysDeleted = deleted;
                LOG.info(() -> "leaves/me cache INVALIDATE userId=" + applicantUserId + " keys=" + keysDeleted);
            }
        } catch (JedisException e) {
            LOG.log(Level.WARNING, "redis invalidate leaves/me user=" + applicantUserId, e);
        }
    }

    private static void ensureConfigured() {
        if (initDone) {
            return;
        }
        synchronized (LOCK) {
            if (initDone) {
                return;
            }
            initDone = true;
            Properties props = new Properties();
            try (InputStream in =
                    LeavesMeRedisCache.class.getClassLoader().getResourceAsStream("redis.properties")) {
                if (in == null) {
                    LOG.info("redis.properties 未放入 classpath，leaves/me 缓存关闭");
                    return;
                }
                props.load(in);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "读取 redis.properties 失败，leaves/me 缓存关闭", e);
                return;
            }
            if (!"true".equalsIgnoreCase(props.getProperty("redis.enabled", "false"))) {
                LOG.info("redis.enabled!=true，leaves/me 缓存关闭");
                return;
            }
            try {
                ttlSeconds =
                        Integer.parseInt(props.getProperty("redis.leavesMeTtlSeconds", "45").trim());
            } catch (NumberFormatException e) {
                ttlSeconds = 45;
            }
            String host = props.getProperty("redis.host", "127.0.0.1").trim();
            int port = Integer.parseInt(props.getProperty("redis.port", "6379").trim());
            String password = props.getProperty("redis.password", "");
            JedisPooled jedis =
                    (password == null || password.isBlank())
                            ? new JedisPooled(host, port)
                            : new JedisPooled(host, port, null, password);
            jedis.ping();
            pool = jedis;
            LOG.info(() -> "leaves/me Redis 旁路缓存已启用 host=" + host + " port=" + port + " ttlSeconds=" + ttlSeconds);
        }
    }
}
