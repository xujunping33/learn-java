import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ThreadCommunicationDemo {
    public static void main(String[] args) {
        int n = 30;      // 生产/消费数量
        int capacity = 10; // 缓冲区上限

        BoundedBuffer buffer = new BoundedBuffer(capacity);

        Thread producer = new Thread(() -> {
            Random r = new Random();
            for (int i = 1; i <= n; i++) {
                int v = r.nextInt(1000);
                buffer.put(v);
                System.out.println("生产者 produced: " + v + " (count=" + i + ")");
            }
            System.out.println("生产者 done.");
        }, "Producer");

        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= n; i++) {
                int v = buffer.get();
                System.out.println("消费者 consumed: " + v + " (count=" + i + ")");
            }
            System.out.println("消费者 done.");
        }, "Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("主线程：所有任务结束。");
    }

    static class BoundedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        public void put(int value) {
            synchronized (this) {
                // 用 while 包裹 wait：防止虚假唤醒/条件不满足
                while (queue.size() >= capacity) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                queue.add(value);
                // 唤醒可能在等 get 的消费者
                notifyAll();
            }
        }

        public int get() {
            synchronized (this) {
                while (queue.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -1;
                    }
                }
                int v = queue.remove();
                // 唤醒可能在等 put 的生产者
                notifyAll();
                return v;
            }
        }
    }
}

