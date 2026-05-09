public class StopFlagDemoNoVolatile {
    private static class Worker implements Runnable {
        // 不加 volatile：可能存在“看不到更新”的问题（未必每次都复现）
        private boolean running = true;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            long count = 0;
            while (running && !Thread.currentThread().isInterrupted()) {
                count++;
                if ((count & 0xFFFFF) == 0) {
                    // 空转占位：避免完全空循环被优化
                }
            }
            System.out.println("Worker stopped. count=" + count + ", interrupted="
                    + Thread.currentThread().isInterrupted());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 非 volatile 版本（可能停得慢/不立刻停）===");
        Worker worker = new Worker();
        Thread t = new Thread(worker, "NoVolatileWorker");

        t.start();
        sleepQuietly(300);

        System.out.println("主线程：调用 stop()，running=false（可能不会立刻生效）");
        worker.stop();

        joinQuietly(t, 1500);
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
            System.out.println("主线程：超时仍未停止，使用 interrupt 强制结束。");
            t.interrupt();
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

