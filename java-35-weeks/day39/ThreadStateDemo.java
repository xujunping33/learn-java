public class ThreadStateDemo {
    public static void main(String[] args) {
        Thread worker = new Thread(() -> {
            System.out.println("worker: run 开始");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("worker: run 结束");
        }, "Worker");

        System.out.println("主线程：创建 worker 后状态=" + worker.getState());
        worker.start();
        System.out.println("主线程：start 后状态=" + worker.getState());

        // 让子线程跑一会儿，然后观察状态
        sleepQuietly(100);
        System.out.println("主线程：稍后状态=" + worker.getState());

        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("主线程：join 后状态=" + worker.getState());
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

