import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * W35 Day240 — 生产者消费者（BlockingQueue 版）。
 *
 * <p>要点：阻塞队列自己做「满则等 / 空则等」；比 wait/notify 手写更不易错。生产里 thread pool + queue 是同类思想。
 */
public final class ProducerConsumerBlockingQueueDemo {

    private static final int CAP = 5;
    /** 毒丸： consumer 收到后退出（仅 demo，生产可用 shutdown、complete 等策略） */
    private static final Integer POISON = -1;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(CAP);
        int produceCount = 20;
        AtomicInteger sum = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(1);

        Thread producer =
                new Thread(
                        () -> {
                            try {
                                for (int i = 1; i <= produceCount; i++) {
                                    q.put(i);
                                    System.out.println("[P] put " + i + " size=" + q.size());
                                }
                                q.put(POISON);
                                System.out.println("[P] poison sent");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        },
                        "producer");

        Thread consumer =
                new Thread(
                        () -> {
                            try {
                                while (true) {
                                    Integer x = q.take();
                                    if (POISON.equals(x)) {
                                        break;
                                    }
                                    sum.addAndGet(x);
                                    System.out.println("[C] took " + x + " partialSum=" + sum.get());
                                    TimeUnit.MILLISECONDS.sleep(10);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                done.countDown();
                            }
                        },
                        "consumer");

        consumer.start();
        producer.start();

        done.await();
        producer.join();

        int expected = produceCount * (produceCount + 1) / 2;
        System.out.println("sum=" + sum.get() + " expected=" + expected + " ok=" + (sum.get() == expected));
    }

    private ProducerConsumerBlockingQueueDemo() {}
}
