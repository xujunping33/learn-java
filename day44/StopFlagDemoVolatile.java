public class StopFlagDemoVolatile {
    private static class Worker implements Runnable {
        private volatile boolean running = true;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            long count = 0;
            while (running) {
                count++;
                // 避免 JIT 把循环优化没了
                if ((count & 0xFFFFF) == 0) {
                    // 空转，不做任何同步操作
                }
            }
            System.out.println("Worker stopped. count=" + count);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== volatile 版本（通常能很快停下）===");
        Worker worker = new Worker();
        Thread t = new Thread(worker, "VolatileWorker");

        t.start();
        sleepQuietly(300);

        System.out.println("主线程：调用 stop()，running=false");
        worker.stop();

        joinQuietly(t, 2000);
        System.out.println("主线程：线程状态=" + t.getState());
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void joinQuietly(Thread t, long ms) {
        try {
            t.join(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (t.isAlive()) {
            System.out.println("主线程：超时仍未停止，interrupt 让其结束。");
            t.interrupt();
        }
    }
}

