import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 轻量级教学用连接池：
 * - 初始化创建 N 个连接
 * - getConnection() 从队列取一个
 * - close() 不真正关闭，而是归还到池
 *
 * 说明：只用于学习/练习，不用于生产。
 */
public class SimplePooledDataSource {
    private final BlockingQueue<Connection> pool;

    public SimplePooledDataSource(int poolSize) throws Exception {
        if (poolSize < 1) throw new IllegalArgumentException("poolSize must be >= 1");
        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.add(Db.getConnection());
        }
    }

    public Connection getConnection() throws Exception {
        Connection raw = pool.take();
        return (Connection) Proxy.newProxyInstance(
                raw.getClass().getClassLoader(),
                new Class[]{Connection.class},
                new PooledConnectionHandler(raw, pool)
        );
    }

    private static class PooledConnectionHandler implements InvocationHandler {
        private final Connection raw;
        private final BlockingQueue<Connection> pool;
        private boolean returned = false;

        private PooledConnectionHandler(Connection raw, BlockingQueue<Connection> pool) {
            this.raw = raw;
            this.pool = pool;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                if (!returned) {
                    returned = true;
                    pool.put(raw);
                }
                return null;
            }
            if ("isClosed".equals(name)) {
                return returned;
            }
            return method.invoke(raw, args);
        }
    }
}

