package learn.java.redis.jedisdemo;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Day 139：连接池 {@link JedisPooled} + try-with-resources；
 * String（SET/GET）、Hash（HSET/HGET）、对象序列化为 JSON 字符串存 Redis。
 */
public final class App {

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws IOException {
        RedisProps p = RedisProps.load();
        try (JedisPooled jedis = buildPool(p)) {
            demoString(jedis);
            demoHash(jedis);
            demoJsonString(jedis);
        } catch (JedisConnectionException e) {
            System.err.println("连不上 Redis。请先启动服务，见 redis/redis-notes.md");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static JedisPooled buildPool(RedisProps p) {
        if (p.password == null || p.password.isBlank()) {
            return new JedisPooled(p.host, p.port);
        }
        return new JedisPooled(p.host, p.port, null, p.password);
    }

    private static void demoString(JedisPooled jedis) {
        jedis.set("demo:string:greeting", "hello-redis");
        String v = jedis.get("demo:string:greeting");
        System.out.println("SET/GET string => " + v);
        jedis.del("demo:string:greeting");
    }

    private static void demoHash(JedisPooled jedis) {
        String key = "demo:hash:user:1";
        jedis.hset(key, Map.of("name", "Alice", "dept", "Engineering"));
        String name = jedis.hget(key, "name");
        String dept = jedis.hget(key, "dept");
        System.out.println("HSET/HGET => name=" + name + ", dept=" + dept);
        jedis.del(key);
    }

    private static void demoJsonString(JedisPooled jedis) {
        String key = "demo:json:profile:42";
        Profile profile = new Profile(42, "Bob", "mgr");
        jedis.set(key, GSON.toJson(profile));
        String json = jedis.get(key);
        Profile roundTrip = GSON.fromJson(json, Profile.class);
        System.out.println("JSON string SET/GET => " + roundTrip);
        jedis.del(key);
    }

    record Profile(int id, String displayName, String role) {}

    private static final class RedisProps {
        final String host;
        final int port;
        final String password;

        RedisProps(String host, int port, String password) {
            this.host = host;
            this.port = port;
            this.password = password;
        }

        static RedisProps load() throws IOException {
            Properties props = new Properties();
            try (InputStream in = App.class.getClassLoader().getResourceAsStream("redis.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
            String host = props.getProperty("redis.host", "127.0.0.1");
            int port = Integer.parseInt(props.getProperty("redis.port", "6379"));
            String password = props.getProperty("redis.password", "");
            return new RedisProps(host, port, password);
        }
    }
}
